/*
 * ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * --------------------------------------------------------------------------------- *
 * Anidea for Aqara Vibration
 * ==========================
 * Version:	 20.05.27.00
 *
 * This device handler is a reworking of the 'Xiaomi Aqara Vibration Sensor' DTH by
 * 'bspranger' that adapts it for the 'new' environment. It has been stripped of the 'tiles', 
 * custom attributes, most of its preferences, and much of the logging.
 */
 
metadata
{
	definition ( name: 'Anidea for Aqara Vibration', namespace: 'orangebucket', author: 'Graham Johnson' )
	{
		// Vibration is reported as acceleration (for consistency with the 'new' app).
		capability "Acceleration Sensor"   	
        // Tilt is reported as motion.
		capability 'Motion Sensor'
        // Drop is reported as a button press.
		capability 'Button'
        // Defined positions are reported as open and closed.
		capability 'Contact Sensor'
        //
 		capability 'Three Axis' 
        
		capability 'Battery'

		capability 'Health Check'
		capability 'Sensor'

		attribute 'accelSensitivity', 'string'

		attribute 'tiltAngle', 'string'
		attribute 'activityLevel', 'string'

		fingerprint endpointId: '01', profileId: '0104', deviceId: '000A', inClusters: '0000,0003,0019,0101', outClusters: '0000,0004,0003,0005,0019,0101', manufacturer: 'LUMI', model: 'lumi.vibration.aq1', deviceJoinName: 'Lumi Aqara Sensor'

		command 'changeSensitivity'
		command 'setopen'
		command 'setclosed'
	}

	preferences
    {
		input "vibrationreset", "number", title: "", description: "Number of seconds (default = 65)", range: "1..7200"
	}
}

// installed() is called when the device is paired, and when the device is updated in the IDE.
def installed()
{	
	logger( 'installed', 'info', '' )
    
    // Health Check is undocumented but lots of ST DTHs create a 'checkInterval' event in this way.
    // Aqara sensors seem to send a battery report every 50-60 minutes, so allow for missing one and then 
    // add a bit of slack on top.
    sendEvent( name: 'checkInterval', value: 2 * 60 * 60 + 10 * 60, displayed: false, data: [ protocol: 'zigbee', hubHardwareId: device.hub.hardwareID ] )
 
 	// Use pushed_6x for vibration, and down_6x as a dummy value for initialisation purposes.
    sendEvent( name: 'numberOfButtons',       value: 1,                                         displayed: false )
    sendEvent( name: 'supportedButtonValues', value: [ 'pushed_6x', 'down_6x' ].encodeAsJSON(), displayed: false )

	// The SmartThings handlers seem keen on initialising the attributes and doing so seems to
    // prevent the 'new' app displaying 'Getting status' on tiles pending the attributes being set.
    sendEvent( name: 'acceleration', value: 'inactive',  displayed: false )
    sendEvent( name: 'motion',       value: 'inactive',  displayed: false )
    sendEvent( name: 'button',       value: 'down_6x',   displayed: false ) 
    sendEvent( name: 'contact',      value: 'closed',    displayed: false )
    sendEvent( name: 'threeAxis',    value: [ 0, 0, 0 ], displayed: false )
}

// updated() seems to be called after installed() when the device is first installed, but not when
// it is updated in the IDE without there having been any actual changes.  It runs whenever settings
// are updated in the mobile app. It often used to be seen running twice in quick succession so was
// debounced in many handlers.
def updated()
{
	logger( 'updated', 'info', '' )
}

def logger( method, level = 'debug', message = '' )
{
	log."${level}" "$device.displayName [$device.name] [${method}] ${message}"
}

def ping()
{
	logger( 'ping', 'info', '' )
}

// Create map of values to be used for vibration, tilt, or drop event
Map mapSensorEvent( value )
{
	logger( 'sensorevent', 'info', value )
    
	def seconds = ( value == 1 || value == 4 ) ? ( vibrationreset ? vibrationreset : 65 ) : 2
    
	def eventname =  [ "", "acceleration", "motion", "button", "acceleration", "motion" ]
	def eventtype =  [ "", "active", 	   "active", "pushed", "inactive",     "inactive" ]
    
	def eventmessage = [ '', 'Vibration (acceleration)', 'Tilt (motion)', 'Drop (button)', 'Reset vibration', 'Reset motion' ]
    
	if ( value == 0 )
		return
	else if ( value == 1 )
    {
		runIn( seconds, clearvibration )
		state.vibrationactive = 1
	}
	else if ( value == 2 )
    {
		runIn( seconds, cleartilt )
    }
	else if ( value == 3 )
    {
		runIn( seconds, cleardrop )
    }

	return [
		name: eventname[value],
		value: eventtype[value],
		descriptionText: eventmessage[value]
	]
}

// Parse incoming device messages to generate events
def parse( String description )
{
	displayDebugLog(": Parsing '${description}'")
	def result = [:]

	// Send message data to appropriate parsing function based on the type of report
	if (description?.startsWith("read attr - raw: ")) {
		result = parseReadAttrMessage(description)
	} else if (description?.startsWith('catchall:')) {
		result = parseCatchAllMessage(description)
	}
	if (result != [:]) {
		displayDebugLog(": Creating event $result")
		return createEvent(result)
	} else {
		displayDebugLog(": Unable to parse unrecognized message")
		return [:]
	}
}

// Check catchall for battery voltage data to pass to battery() for conversion to percentage report
private Map parseCatchAllMessage(String description)
{
	Map resultMap = [:]
	def catchall = zigbee.parse(description)
	displayDebugLog(": $catchall")

	if (catchall.clusterId == 0x0000) {
		def MsgLength = catchall.data.size()
		// Xiaomi CatchAll does not have identifiers, first UINT16 is Battery
		if ((catchall.data.get(0) == 0x01 || catchall.data.get(0) == 0x02) && (catchall.data.get(1) == 0xFF)) {
			for (int i = 4; i < (MsgLength-3); i++) {
				if (catchall.data.get(i) == 0x21) { // check the data ID and data type
					// next two bytes are the battery voltage
					resultMap = battery((catchall.data.get(i+2)<<8) + catchall.data.get(i+1))
					break
				}
			}
		}
	}
	return resultMap
}

// Parse read attr - raw messages (includes all sensor event messages and reset button press, and )
private Map parseReadAttrMessage(String description) {
	def cluster = description.split(",").find {it.split(":")[0].trim() == "cluster"}?.split(":")[1].trim()
	def attrId = description.split(",").find {it.split(":")[0].trim() == "attrId"}?.split(":")[1].trim()
	def value = description.split(",").find {it.split(":")[0].trim() == "value"}?.split(":")[1].trim()
	def eventType
	Map resultMap = [:]

	if (cluster == "0101") {
		// Handles vibration (value 01), tilt (value 02), and drop (value 03) event messages
		if (attrId == "0055") {
			if (value?.endsWith('0002')) {
				eventType = 2
				parseTiltAngle(value[0..3])
			} else {
				eventType = Integer.parseInt(value,16)
			}
			resultMap = mapSensorEvent(eventType)
		}
		// Handles XYZ Accelerometer values
		else if (attrId == "0508") {
			short x = (short)Integer.parseInt(value[8..11],16)
			short y = (short)Integer.parseInt(value[4..7],16)
			short z = (short)Integer.parseInt(value[0..3],16)
			float Psi = Math.round(Math.atan(x/Math.sqrt(z*z+y*y))*1800/Math.PI)/10
			float Phi = Math.round(Math.atan(y/Math.sqrt(x*x+z*z))*1800/Math.PI)/10
			float Theta = Math.round(Math.atan(z/Math.sqrt(x*x+y*y))*1800/Math.PI)/10
			def descText = ": Calculated angles are Psi = ${Psi}°, Phi = ${Phi}°, Theta = ${Theta}° "
			displayDebugLog(": Raw accelerometer XYZ axis values = $x, $y, $z")
			displayDebugLog(descText)
			resultMap = [
				name: 'threeAxis',
				value: [Psi, Phi, Theta],
				linkText: getLinkText(device),
				isStateChange: true,
				descriptionText: "$device.displayName$descText",
			]
			if ( !state.closedx || !state.openx )
				displayInfoLog(": Open/Closed position is unknown because Open and/or Closed positions have not been set")
			else {
				def float cX = state.closedx
				def float cY = state.closedy
				def float cZ = state.closedz
				def float oX = state.openx
				def float oY = state.openy
				def float oZ = state.openz
				def float e = 10.0 // Sets range for margin of error
				def ocPosition = "unknown"
                
				if ((Psi < cX + e) && (Psi > cX - e) && (Phi < cY + e) && (Phi > cY - e) && (Theta < cZ + e) && (Theta > cZ - e))
					ocPosition = "closed"
				else if ((Psi < oX + e) && (Psi > oX - e) && (Phi < oY + e) && (Phi > oY - e) && (Theta < oZ + e) && (Theta > oZ - e))
					ocPosition = "open"
				else
					displayDebugLog(": The current calculated angle position does not match either stored open/closed positions")
                    
                // Only send a change event when have any confidence in it.
				if ( ocPosition != 'unknown' ) sendEvent( name: 'contact', value: ocPosition )
			}
		}
		// Handles Recent Activity level value messages
		else if (attrId == "0505") {
			def level = Integer.parseInt(value[0..3],16)
			def descText = ": Recent activity level reported at $level"
			displayInfoLog(descText)
			resultMap = [
				name: 'activityLevel',
				value: level,
				descriptionText: "$device.displayName$descText",
			]
		}
	}
	else if (cluster == "0000" && attrId == "0005")	{
		displayInfoLog(": reset button short press detected")
		def modelName = ""
		// Parsing the model
		for (int i = 0; i < value.length(); i+=2) {
			def str = value.substring(i, i+2);
			def NextChar = (char)Integer.parseInt(str, 16);
				modelName = modelName + NextChar
		}
		displayDebugLog(" reported model name:${modelName}")
	}
	return resultMap
}

// Handles tilt angle change message and posts event to update UI tile display
private parseTiltAngle(value) {
	def angle = Integer.parseInt(value,16)
	def descText = ": tilt angle changed by $angle°"
	sendEvent(
		name: 'tiltAngle',
		value: angle,
		// unit: "°",  // Need to check whether this works or is needed at all
		descriptionText : "$device.displayName$descText",
		isStateChange:true,
		displayed: true
	)
	displayInfoLog(descText)
}

Map battery( raw )
{
    // Experience shows that a new battery in an Aqara sensor reads about 3.2V, and they need
	// changing when you get down to about 2.7V. It really isn't worth messing around with 
	// preferences to fine tune this.

	def rawvolts = raw / 1000
    
    logger( 'battery', 'info', "$rawvolts V" )
        
	def minvolts = 2.7
	def maxvolts = 3.2
	def percent = Math.min( 100, Math.round( 100.0 * ( rawvolts - minvolts ) / ( maxvolts - minvolts ) ) ) 

	// Battery events are sent with the 'isStateChange: true' flag to make sure there are regular
    // propagated events available for Health Check to monitor (if that is what it needs).
	return [ name: 'battery', value: percent, isStateChange: true ]
}

def clearvibration()
{
	def result = [:]
	result = mapSensorEvent(4)
	state.vibrationactive = 0
	displayDebugLog(": Sending event $result")
	sendEvent( result )
}

def cleartilt()
{
	def result = [:]
	result = mapSensorEvent(5)
	displayDebugLog(": Sending event $result")
	sendEvent(result)
}

def cleardrop()
{
}

def setclosed()
{
	logger( 'setclosed', 'info', '' )
    
    def threeaxis = device.currentState( 'threeAxis' )

    try
    {
		state.closedx = threeaxis.xyzValue.x
		state.closedy = threeaxis.xyzValue.y
		state.closedz = threeaxis.xyzValue.z
        
        logger( 'setclosed', 'debug', "Closed position set to [ $state.closedx, $state.closedy, $state.closedz ]" )
        
		sendEvent( name: 'contact', value: 'closed', isStateChanged: true )
	}
    catch( e )
    {
    	logger( 'setclosed', 'debug', e )
    }
}

def setopen()
{
	logger( 'setopen', 'info', '' )
    
    def threeaxis = device.currentState( 'threeAxis' )

    try
    {
		state.openx = threeaxis.xyzValue.x
		state.openy = threeaxis.xyzValue.y
		state.openz = threeaxis.xyzValue.z
        
        logger( 'setopen', 'debug', "Open position set to [ $state.openx, $state.openy, $state.openz ]" )
        
		sendEvent( name: 'contact', value: 'open', isStateChanged: true )
	}
    catch( e )
    {
    	logger( 'setopen', 'debug', e )
    }
}

def changeSensitivity() {
	state.sensitivity = (state.sensitivity < 3) ? state.sensitivity + 1 : 1
	def attrValue = [0, 0x15, 0x0B, 0x01]
	def levelText = ["", "Low", "Medium", "High"]
	def descText = ": Sensitivity level set to ${levelText[state.sensitivity]}"
	zigbee.writeAttribute(0x0000, 0xFF0D, 0x20, attrValue[state.sensitivity], [mfgCode: 0x115F])
	zigbee.readAttribute(0x0000, 0xFF0D, [mfgCode: 0x115F])
	displayInfoLog(descText)
}

def displayDebugLog(String message) {
	logger( 'displayDebugLog', 'debug', message )
}

def displayInfoLog(String message) {
	logger( 'displayDebugLog', 'info', message )
}

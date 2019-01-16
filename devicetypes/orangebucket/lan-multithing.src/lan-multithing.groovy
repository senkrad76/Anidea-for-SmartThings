/*
 * ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby granted, provided that the copyright notice below
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH 
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER 
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 * ---------------------------------------------------------------------------------
 *
 * LAN MultiThing
 * ==============
 * The LAN MultiThing is a device handler for a remote device on the local network
 * that implements a number of actuator and sensor capabilities, and can also
 * act as a bridge to other devices. No specific remote device implementation is 
 * required, but the development implementation uses the AutoRemote WiFi Service
 * as a server front end to Tasker.
 *
 * Actuator commands for the parent and child devices are sent using HTTP GET to 
 * a server on the local network using a path and expecting a response that are 
 * compatible with the AutoRemote WiFi Service. The commands use the AutoApps 
 * command format.
 *
 * The remote device uses HTTP POST to send JSON format messages to the hub which
 * are either processed by the parent or passed on to a specified child device
 * handler. These messages may include sensor attributes, state variables and the
 * current list of child devices.
 *
 * Author:				Graham Johnson (orangebucket)
 *
 * Version:				1.1.3	(16/01/2019) 
 *
 * Comments:
 *
 * None.
 *
 * Changes:
 *
 * 1.1.3.       (16/01/2019)    Make switch the 'main' tile and tweak the mobile app UI.  
 * 1.1.2		(30/10/2018)	Live Logging was randomly dropping many of the log entries
 *								created when adding and creating child devices so they've
 *								been replaced by a summary log statement. The installed()
 *								method was calling updated() but the word is updated()
 *								gets called anyway. Creation and deletion of child devices
 *								has been moved to updated() as when called from configure()
 *								subsequent calls to parse() failed on getChildDevices().
 *								Parse of child devices also generated more logs than could 
 *								be processed.
 * 1.1.1		(15/10/2018)	Ongoing work following check-in. Add Speech Recognition
 *								as a capability and child device.
 * 1.1.0		(15/10/2018)	Further work on working with child devices.
 *				(14/10/2018)	Support multiple child device handlers. Handle attribute
 *								changes for ETA children.
 *				(13/10/2018)	Acts as a composite device, bridging individual child
 *								devices.
 * 1.0.0		(12/10/2018)	Rename the app with a more generic name and reset the
 *								version number. Do logging via a logger() method.
 *
 * Please be aware that this file is created in the SmartThings Groovy IDE and it may
 * format differently when viewed outside that environment.
 */

preferences
{
   	input name: "ip", type: "text", title: "IP Address", description: "e.g. 192.168.1.2", required: true
    input name: "port", type: "text", title: "Port", description: "e.g. 8000", required: true
    input name: "mac", type: "text", title: "MAC Address (optional)", description: "e.g. aa:bb:cc:dd:ee:ff", required: false
}

metadata
{
	definition (name: "LAN MultiThing", namespace: "orangebucket", author: "Graham Johnson")
    {
		capability "Actuator"
        capability "Air Quality Sensor"
        capability "Alarm"
        capability "Audio Notification"
        capability "Battery"
        capability "Bridge"
        capability "Configuration"
        capability "Estimated Time Of Arrival"
        capability "Notification"
        capability "Power Source"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        capability "Speech Recognition"
        capability "Speech Synthesis"
		capability "Switch"
        capability "Temperature Measurement"
        capability "Tone"
        capability "Ultraviolet Index"
        
        // The Actuator and Sensor capabilities are shown as deprecated in the Capabilities
        // references, yet their use is also encouraged as best practice. The Actuator capability
		// just means the device has commands. The Sensor capability means it has attributes.
        
        // The Bridge capability is a similar deprecated 'tagging' capability. As the device
        // handler now forwards commands from virtual child devices with the intention of the
        // remote device farming them off to different speakers, for example, it is probably 
        // a bridge now.

        // Specific off commands for alarm and switch as they both have an 'off' command.
    	command "alarmoff"
   	 	command "switchoff"
            
    	// Commands for child devices.
    	command "childspeak"
    }
    
	// One day I will investigate this.
	simulator
    {
	}

	// UI.
    //
    // White (#ffffff) is the standard for 'off'-like states.
    // Blue (#00a0dc) is the standard for 'on'-like states.
    // Orange (#e86d13) is the standard for devices requiring attention.
    // Grey (#cccccc) is the standard for inactive or offline devices.
    //
    // Orange is used for the alarm 'Siren', 'Strobe' and 'Both' states as that is what is meant by
    // a device requiring attention.
    //
    // The standards suggest that transitional states should use the colour for the transitioned to state.
    // This seems such a baffling choice that it has been ignored. There also doesn't seem to be any conventions
    // for tiles used for test and reset purposes.
    //
    // Yellow (#c0c000) is being used for transitional states to highlight possible technical issues.
    // Purple (#800080) is being used for test tiles for notification, speech and tone.
    // Red (#ff0000) is being used by tiles that reset the alarm and switch to off.
    //
	tiles
    {
    	// Start with a tile for the alarm status of the device. Either turns both alarms on, or turns both off.
        // Transitional states are shown while waiting for a response from the remote device.
        standardTile("alarm", "device.alarm", width: 1, height: 1, canChangeIcon: true) 
        {
            state "off", label:'Off', action:'alarm.both', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff", nextState: "bothon"         
            state "bothon", label: '-> Both', icon: "st.alarm.alarm.alarm", backgroundColor: "#c0c000"
            state "both", label: 'Both', action:'alarmoff', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13", nextState: "bothoff"
            state "siren", label: 'Siren', action:'alarmoff', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13", nextState: "bothoff"
            state "strobe", label: 'Strobe', action:'alarmoff', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13", nextState: "bothoff"              
            state "bothoff", label: '-> Off', icon: "st.alarm.alarm.alarm", backgroundColor: "#c0c000"
        }
         
        // This tile either adds the siren alarm, or turns both alarms off.
        // Transitional states are shown while waiting for a response from the remote device.
        standardTile("siren", "device.alarm", width: 1, height: 1) 
        {
            state "off", label:'Off', action:'alarm.siren', icon:"st.Electronics.electronics13", backgroundColor:"#ffffff", nextState: "sirenon"
          	state "strobe", label:'Off', action:'alarm.both', icon:"st.Electronics.electronics13", backgroundColor:"#ffffff", nextState: "bothon"
            state "sirenon", label: '-> Siren', icon:"st.Electronics.electronics13", backgroundColor:"#c0c000"
            state "bothon", label: '-> Both', icon:"st.Electronics.electronics13", backgroundColor:"#c0c000"
            state "siren", label: 'Siren', action:'alarmoff', icon:"st.Electronics.electronics13", backgroundColor:"#e86d13", nextState: "bothoff"
            state "both", label: 'Both', action:'alarmoff', icon:"st.Electronics.electronics13", backgroundColor:"#e86d13", nextState: "bothoff"
            state "bothoff", label: '-> Off', icon:"st.Electronics.electronics13", backgroundColor:"#c0c000"
      	}        
        
        // This tile either adds the strobe alarm, or turns both alarms off.  
        // Transitional states are shown while waiting for a response from the remote device.
        standardTile("strobe", "device.alarm", width: 1, height: 1) 
        {
            state "off", label:'Off', action:'alarm.strobe', icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState: "strobeon"
            state "siren", label:'Off', action:'alarm.both', icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState: "bothon"
            state "strobeon", label: '-> Strobe', icon:"st.Lighting.light13", backgroundColor:"#c0c000"
            state "bothon", label: '-> Both', icon:"st.Lighting.light13", backgroundColor:"#c0c000"
            state "strobe", label: 'Strobe', action:'alarmoff', icon:"st.Lighting.light13", backgroundColor:"#e86d13", nextState: "bothoff"
            state "both", label: 'Both', action:'alarmoff', icon:"st.Lighting.light13", backgroundColor:"#e86d13", nextState: "bothoff"
            state "bothoff", label: '-> Off', icon:"st.Lighting.light13", backgroundColor:"#c0c000"
        }

        // This tile will reset the alarm to off regardless of current status.
		standardTile("alarmreset", "device.alarm", width: 1, height: 1)
        {
			state "reset", label: 'Reset', action: "alarmoff", icon: "st.alarm.alarm.alarm", backgroundColor: "#ff0000", defaultState: true
		}
        
        // This tile sends a sample notification.
        standardTile("notification", "device.notification", width: 1, height: 1)
        {
            state "notify", label:'Notify', action:"notification.deviceNotification", icon:"st.Office.office13", backgroundColor:"#800080", defaultState: true
        }
        
        // This tile sends a sample text to be spoken. Speech is a dummy attribute.
        standardTile("speechSynthesis", "device.speech", width: 1, height: 1)
        {
            state "speech", label:'Speak', action:'Speech Synthesis.speak', icon:"st.Entertainment.entertainment3", backgroundColor:"#800080", defaultState: true
        }
        
		// Switch showing transitional states while awaiting device response.
		standardTile("switch", "device.switch", width: 1, height: 1)
        {
			state "off", label: "Off", action: "switch.on",  icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turnon"        
            state "turnon", label: "-> On", icon: "st.switches.switch.on", backgroundColor: "#c0c000"
			state "on", label: "On", action: "switchoff", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turnoff"            
			state "turnoff", label: "-> Off", icon: "st.switches.switch.off", backgroundColor: "#c0c000"
        }
                
        // This tile will reset the switch to off regardless of current status.
		standardTile("switchreset", "device.switch", width: 1, height: 1)
        {
			state "reset", label: 'Reset', action: "switchoff", icon: "st.switches.switch.off", backgroundColor: "#ff0000", defaultState: true
		}
        
        // This tile sens the beep command.
        standardTile("tone", "device.tone", width: 1, height: 1)
        {
            state "tone", label:'Tone', action:"tone.beep", icon:"st.alarm.beep.beep", backgroundColor: "#800080", defaultState: true
        }
        
        // This tile calls the configure command. The attribute 'configuration' is a dummy one.
        standardTile("configuration", "device.configuration", width: 1, height: 1)
        {
            state "configuration", label:'Configure', action:"configuration.configure", icon:"st.Office.office15", backgroundColor: "#800080", defaultState: true
        }
        
        valueTile("airquality", "device.airQuality", decoration: "flat", width: 1, height:1)
        {
        	state "airQuality", label:'Air Quality ${currentValue}'
    	}
        
        valueTile("battery", "device.battery", decoration: "flat", width: 1, height:1)
        {
        	state "battery", label:'Battery Level ${currentValue}%'
    	}  

        valueTile("eta", "device.eta", decoration: "flat", width: 2, height:1)
        {
        	state "eta", label:'ETA ${currentValue}'
    	} 
        
        valueTile("humidity", "device.humidity", decoration: "flat", width: 1, height:1)
        {
        	state "humidity", label:'Humidity ${currentValue}%'
    	} 
        
        valueTile("temperature", "device.temperature", decoration: "flat", width: 1, height:1)
        {
        	state "temperature", label:'Temp ${currentValue} C'
    	} 
        
        valueTile("uvindex", "device.ultravioletIndex", decoration: "flat", width: 1, height:1)
        {
        	state "ultravioletIndex", label:'UV ${currentValue}'
    	} 

		// Use the alarm as the main tile.
        main "switch"
        // Sort the tiles suitably.
        details (["switch", "alarmreset", "switchreset", "alarm", "siren", "strobe", "notification", "speechSynthesis", "tone",
        	      "configuration", "airquality", "battery", "eta", "humidity", "temperature", "uvindex"])
	}
}

def installed()
{
	logger("installed")
    
    // Used to call updated() here but the word is it will get called anyway.
}


// The updated() command is called when preferences are saved. It often seems
// to be called twice so an attempt is made to only let it run once in a five
// second period.
def updated()
{
	if (state.lastupdated && now() < state.lastupdated + 5000)
    {        
        logger("updated", "debug", "Skipped as ran recently")
 
 		return
    }
        
 	state.lastupdated = now()
    
    // Adding and deleting children seems to be performed best here. Deleting
    // children from configure() caused subsequent calls to getChildDevices in
    // parse to fail.
    addchildren()
    deletechildren()

	logger("updated")
    
	unschedule()
 
	runIn(2, setdni)
}

// In order for the hub to send responses to the 'parse()' method in a DTH it seems
// the device network ID needs to be either the MAC address, or the IP address and
// port in hex notation. Generally the MAC address is encouraged as IP addresses
// can change.
//
// The MAC address will give the potential for the device handler to receive incoming
// messages. If the MAC address is specified in the preferences it will be used.
def setdni()
{
	def address = settings.ip
	def port = settings.port
    def mac = settings.mac
	def hex = ""

	if ( !settings.mac )
    {
		def octets = address.tokenize('.')
        
    	octets.each
    	{
			hex = hex + Integer.toHexString(it as Integer).toUpperCase().padLeft(2, '0')
		}

		hex = hex + ":" + Integer.toHexString(port as Integer).toUpperCase().padLeft(4, '0')
    }
    else hex = mac.replaceAll("[^a-fA-F0-9]", "").toUpperCase()

    if (device.getDeviceNetworkId() != hex)
    {
    	logger("setdni", "info", "${address}:${port} ${hex}")
  	  
		device.setDeviceNetworkId(hex)
    }
    else logger("setdni", "debug", "(not needed)")
}

// Have own logging routine.
def logger(method, level = "debug", message ="")
{
	log."${level}" "$device.displayName [$device.name] [${method}] ${message}"
}

import groovy.json.JsonSlurper 

def parse(description)
{
	def msg = parseLanMessage(description)
    
 	// There should be a record of any state change requests in the state map.
    if ( state[msg.requestId] )
    {
    	def st = state[msg.requestId].split('=:=')
        def stcap = st[0]
        def stval = st[1]
        
        // A state change event is only triggered when a response to the request has been received. 
		// This doesn't mean it has 'worked', only that the remote device has received the request.
		// However it is a lot better than simply sending state change events in the commands.
          	
    	def stateevent = createEvent(name: stcap, value: stval)
        
        // This entry in the map is no longer required.
        state.remove(msg.requestId)
        
		logger("parse", "debug", "Set state of ${stcap} to ${stval}")

		// Let ST fire off the event.
        return stateevent
    }
    else
    {
		if (msg.body)
       	{
      		def jsonSlurper = new JsonSlurper()
      		def body = jsonSlurper.parseText(msg.body)
			def children = getChildDevices()
            
			if (body.devicename)
            {
        		children.each
                {
                	child ->
                    
                    if (child.displayName == body.devicename)
                    {
                    	logger("parse", "debug", "Message passed to $child.displayName")
                    	child.parse(description)
                    }   
                }
            }
			else if (body.result == "OK")
        	{
        		logger("parse", "debug", "No state change event required")
        	}
        	else
            {
        		logger("parse", "info", "Ping received")
           
                body.attribute.each
				{
                	myname, myvalue ->
                                    
                	if (myname == "eta")
                	{
                		try
                    	{
                    		def mytime = Date.parse("yyyy-MM-dd'T'HH:mm:ssX", myvalue).format("HH:mm", location.getTimeZone())
                        
                    		logger("parse", "debug", "attribute $myname $myvalue (${mytime})")
                    
                    		sendEvent(name: myname, value: myvalue, isStateChange: true)
                    	}
                    	catch(Exception e)
                    	{
                    		// The returned value is not an ISO8601 date.
                    		logger("parse", "debug", "attribute $myname (value invalid)")

                            sendEvent(name: myname, value: null, isStateChange: true)
                    	}
                	}
					else if (myname == "temperature")
					{
                 		logger("parse", "debug", "attribute ${myname} $myvalue.value $myvalue.unit")
                        
                		sendEvent(name: myname, value: myvalue.value, unit: myvalue.unit, isStateChange: true)
                    }
                    else
               		{
                    	logger("parse", "debug", "attribute ${myname} ${myvalue}")     
 
 						sendEvent(name: myname, value: myvalue, isStateChange: true)
                    }
                }
                
                body.state.each
				{
                	myname, myvalue ->
                                    
                	if (myname.contains("date"))
                	{
                		try
                    	{
                    		def mytime = Date.parse("yyyy-MM-dd'T'HH:mm:ssX", myvalue).format("HH:mm", location.getTimeZone())
                        
                    		logger("parse", "debug", "state $myname $myvalue (${mytime})")
                    
                    		state."${myname}" = myvalue
                    	}
                    	catch(Exception e)
                    	{
                    		// The returned value is not an ISO8601 date.
                    		logger("parse", "debug", "state $myname (value invalid)")

                            state."${myname}" = myvalue
                    	}
                	}
                    else
               		{
                		logger("parse", "info", "state ${myname} ${myvalue}")    
 
 						state."${myname}" = myvalue
                    }
                }
                           
                if (body.devices) 
            	{
                 	def childlist = []
                    
                    def numberofchildren = 0

					body.devices.each
					{
         				numberofchildren++       		
                    
                    	childlist += [name: it.name, type: it.type]
                	}

					state.childdevicelist = childlist
                    
                    logger("parse", "info", "${numberofchildren} child devices received")
                    
                    // logger("parse", "trace", "$childlist")
                }
          	}
        }
        else
        {
        	// Body is empty.
            logger("parse", "warn", "Ping received with empty body")
        }
    }
}

// Build and return a hubaction.
//
// devicename	Device name (this device or child).
// cap			Capability id.
// capcomm		Command or empty string.
// capfree 		Free text or empty string (default).	
// capextra		Extra args or empty string (default).
// commandstate	True (default) if command should trigger a state change.
def buildhubaction(devicename, cap, capcomm, capfree = '', capextra = '', commandstate = true)
{    
	// Hex isn't required but the variable name has stuck.
    def hex = settings.ip + ":" + settings.port
    // hex = "C0A80112:0719"
    
    // The capfree parameter may have a command on the front. Extract it if so.
    def tempcap = capfree.split('=:=')
    if (tempcap.length > 1)
    {
    	// Don't allow capcomm or capfree to end up empty.
    	capcomm = tempcap[0] ?: capcomm
    	capfree = tempcap[1] ?: capcomm
    }
    
    // Don't allow capfree to be empty.
    if (!capfree) capfree = capcomm
    
    // URL encoding is probably a bit redundant and AutoRemote doesn't seem to do any
    // decoding so it would break things if the whole query string was encoded. So
    // just encode the variable that might have relatively free text in it.
    def enccapfree = URLEncoder.encode(capfree, 'UTF-8');
    
    // AutoApps Commands aren't strictly compatible with URLs because of spaces so
    // remove them from the device names. This does make things a bit messier at
    // the remote end unfortunately.
    def devicenamestrip = devicename.displayName.replaceAll("[^a-zA-Z0-9]+","")
    def dth = device.name.replaceAll("[^a-zA-Z0-9]+","")
	
	def hubaction = new physicalgraph.device.HubAction(
        method	: "GET",
        path	: "/sendmessage",
 		query	:	[ "message": "${dth}${devicenamestrip}=:=${cap}=:=${capcomm}=:=${enccapfree}=:=${capextra}" ],          	
        headers	:
            [
            	"HOST": "${hex}",
      		]
	)
    
    // Save any state change associated with this request.
    if (commandstate) state[hubaction.requestId] = "${cap}=:=${capcomm}"

	logger("buildhubaction", "debug", "${dth}${devicenamestrip} ${cap} ${capcomm} ${hex}")
    
	return hubaction
}

//
// Alarm
//
// off() command is with Switch commands.

def both()
{
	return buildhubaction(device, 'alarm', 'both')
}

def siren()
{
	return buildhubaction(device, 'alarm', 'siren')
}

def strobe()
{
	return buildhubaction(device, 'alarm', 'strobe')
}

// Custom command to turn alarm off.
def alarmoff()
{
    // ST will run the HubAction for us.
    return buildhubaction(device, 'alarm', 'off')
}

//
// Audio Notification
//
// The SmartThings device capabilities documention shows three commands for
// Audio Notification, all of which just have a URL and an optional level.
// The Speaker Companion smartapp hasn't read the documentation and calls 
// commands that don't exist and throws in a duration before the level.
// This device handler implements the commands as documented but accepts
// the undocumented usage.
//

def playTrack(uri, level = null)
{
	logger("playTrack")
    
    return buildhubaction(device, 'audioNotification', 'playTrack', uri, level, false)
}

def playTrackAndResume(uri, level = null, anotherlevel = null)
{
	if (otherVolume) level = anotherlevel
        
	logger("playTrackandResume")
        
    return buildhubaction(device, 'audioNotification', 'playTrackAndResume', uri, level, false)
}

def playTrackAndRestore(uri, level = null, anotherlevel = null)
{
 	if (otherVolume) level = anotherlevel

	logger("playTrackandRestore")
    
    return buildhubaction(device, 'audioNotification', 'playTrackAndRestore', uri, level, false)
}

//
// Configuration
//
// The Configuration capability is intended for configuring an actual device rather than
// setting up the device handler. It could usefully be used to have the remote device
// send an up to date list of children.
//

// The configure() command often, but not always, seems to be called twice. An attempt
// is made to prevent it running again within five seconds but sometimes the commands
// are so close that they can even end up with the same timestamp.
def configure()
{
	if (state.lastconfigure && now() < state.lastconfigure + 5000)
    {   
        logger("configure", "debug", "Skipped as ran recently")

 		return
    }
        
    state.lastconfigure = now()
 
 	logger("configure", "debug", "$state.lastconfigure")
    
    // addchildren()
    // deletechildren()
   
    // ST will run the HubAction for us.
    return buildhubaction(device, 'configuration', 'configure', '', '', false)
}

//
// Notification
//

def deviceNotification(notificationtext)
{
    if (!notificationtext?.trim()) notificationtext = device.name
   
	// ST will run the HubAction for us.
    return buildhubaction(device, 'notification', 'deviceNotification', notificationtext, '', false)
}

//
// Speech Synthesis
//

def speak(words)
{
    if (!words?.trim()) words = device.name
   
	// ST will run the HubAction for us.
    return buildhubaction(device, 'speechSynthesis', 'speak', words, '', false)
}

//
// Switch
//

def on()
{
    // ST will run the HubAction for us.
    return buildhubaction(device, 'switch', 'on')
}

def off()
{   
	// This command can be called for the alarm or the switch.
    
    // Default is the switch.
    def cap = "switch"

	// If the alarm is activated turn it off.
    if (device.currentValue('alarm') != "off") cap = "alarm"
    
    // ST will run the HubAction for us.
    return buildhubaction(device, cap, 'off')
}

// Custom command to turn switch off.
def switchoff()
{
    // ST will run the HubAction for us.
    return buildhubaction(device, 'switch', 'off')
}

def beep()
{
	// ST will run the HubAction for us.
    return buildhubaction(device, 'tone', 'beep', '', '', false)
}

//
// Play with the children.
//

def addchildren()
{
	// logger("addchildren", "trace", state.childdevicelist)

	def children = getChildDevices() 
        
    def existingchildren = children.size
    def requiredchildren = state.childdevicelist.size
    def addedchildren = 0
    
    state.childdevicelist.each
    {
    	newdevice ->

		def dni = newdevice.name.replaceAll("[^a-zA-Z0-9]+","")
        def needed = true
       
        children.each
        {
        	existingchild ->

        	if (dni == existingchild.deviceNetworkId) needed = false
        }
        
        if (needed)
        {
        	addedchildren++
            
            addChildDevice("orangebucket", newdevice.type, dni, null, [isComponent: false, completedSetup: true, label: newdevice.name])

			logger("addchildren", "debug", "Created ${newdevice.name} ${newdevice.type}")
        }
    }
        
    logger("addchildren", "info", "${existingchildren} existing, ${requiredchildren} required, ${addedchildren} attempted additions")      
}


def deletechildren()
{
	// logger("deletechildren", "trace", state.childdevicelist)

	def children = getChildDevices()
    
    def existingchildren = children.size
    def requiredchildren = state.childdevicelist.size
    def deletedchildren = 0
 	def faileddeletions = 0
    
    children.each
    {
    	existingchild -> 
               
    	def needed = false
        
        state.childdevicelist.each
        {
        	newdevice ->
            
            def dni = newdevice.name.replaceAll("[^a-zA-Z0-9]+","")

        	if (dni == existingchild.deviceNetworkId) needed = true
        }
        
        if (!needed)
        {
 			try
            {
				deletedchildren++
                
                deleteChildDevice(existingchild.deviceNetworkId)
                
                logger("deletechildren", "debug", "Deleted ${existingchild}")
            }
			catch (e)
            {
				faileddeletions++
                
                logger("deletechildren", "error", "Error deleting ${existingchild} $e")
			}
        }
    }
    
    logger("deletechildren", "info", "${existingchildren} existing, ${requiredchildren} required, ${faileddeletions}/${deletedchildren} failed deletions")     
}

def childspeak(childdevice, words)
{
    if (!words?.trim()) words = childdevice.name
   
   	logger("childspeak", "debug", "${childdevice} ${words}")
    
	// ST will run the HubAction for us.
    return buildhubaction(childdevice, 'speechSynthesis', 'speak', words, '', false)
}

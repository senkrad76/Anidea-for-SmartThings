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
 * Anidea for Aqara Motion
 * =======================
 * Version:	 20.02.24.00
 *
 * This device handler is a reworking of the 'Xiaomi Aqara Motion' DTH by 'bspranger' that
 * adapts it for the 'new' environment. It has been stripped of the 'tiles', custom attributes,
 * most of its preferences, and much of the logging. The Health Check has been switched to
 * be untracked rather than implementing a 'checkinterval' as it isn't clear it was implemented
 * correctly. There wasn't a 'ping()', for example, but should there be?
 */

metadata {
    definition (name: "Anidea for Aqara Motion", namespace: "orangebucket", author: "Graham Johnson", ocfDeviceType: "x.com.st.d.sensor.motion") {
        capability "Motion Sensor"
        capability "Illuminance Measurement"
        capability "Battery"
        capability "Sensor"
        capability "Health Check"

        fingerprint endpointId: "01", profileId: "0104", deviceId: "0107", inClusters: "0000,FFFF,0406,0400,0500,0001,0003", outClusters: "0000,0019", manufacturer: "LUMI", model: "lumi.sensor_motion.aq2", deviceJoinName: "Aqara Motion Sensor"
        fingerprint profileId: "0104", deviceId: "0104", inClusters: "0000, 0400, 0406, FFFF", outClusters: "0000, 0019", manufacturer: "LUMI", model: "lumi.sensor_motion", deviceJoinName: "Aqara Motion Sensor"
    }

	preferences {
		//Reset to No Motion Config
		input description: "This setting only changes how long MOTION DETECTED is reported in SmartThings. The sensor hardware always remains blind to motion for 60 seconds after any activity.", type: "paragraph", element: "paragraph", title: "MOTION RESET"
		input "motionreset", "number", title: "", description: "Enter number of seconds (default = 60)", range: "1..7200"
	}	
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "${device.displayName} parsing: $description"

    Map map = [:]
	
	// Send message data to appropriate parsing function based on the type of report	
    if (description?.startsWith('illuminance:')) {
        map = parseIlluminance(description)
    }
    else if (description?.startsWith('read attr -')) {
        map = parseReportAttributeMessage(description)
    }
    else if (description?.startsWith('catchall:')) {
        map = parseCatchAllMessage(description)
    }

    def result = map ? createEvent(map) : null

    return result
}

// Parse illuminance report
private Map parseIlluminance(String description) {
    def lux = ((description - "illuminance: ").trim()) as int

    def result = [
        name: 'illuminance',
        value: lux,
        unit: "lux",
        isStateChange: true
    ]
    return result
}
// Parse motion active report or model name message on reset button press
private Map parseReportAttributeMessage(String description) {
    def cluster = description.split(",").find {it.split(":")[0].trim() == "cluster"}?.split(":")[1].trim()
    def attrId = description.split(",").find {it.split(":")[0].trim() == "attrId"}?.split(":")[1].trim()
    def value = description.split(",").find {it.split(":")[0].trim() == "value"}?.split(":")[1].trim()

    Map resultMap = [:]

	// The sensor only sends a motion detected message so the reset to no motion is performed in code
    if (cluster == "0406" & value == "01") {
		log.debug "${device.displayName} detected motion"
		def seconds = motionreset ? motionreset : 120
		resultMap = [
			name: 'motion',
			value: 'active',
			descriptionText: "${device.displayName} detected motion"
		]
		runIn(seconds, stopMotion)
	}
	else if (cluster == "0000" && attrId == "0005") {
        def modelName = ""
        // Parsing the model
        for (int i = 0; i < value.length(); i+=2) {
            def str = value.substring(i, i+2);
            def NextChar = (char)Integer.parseInt(str, 16);
            modelName = modelName + NextChar
        }
        log.debug "${device.displayName} reported: cluster: ${cluster}, attrId: ${attrId}, model:${modelName}"
    }
    return resultMap
}

// Check catchall for battery voltage data to pass to getBatteryResult for conversion to percentage report
private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def catchall = zigbee.parse(description)
	log.debug catchall

	if (catchall.clusterId == 0x0000) {
		def MsgLength = catchall.data.size()
		// Xiaomi CatchAll does not have identifiers, first UINT16 is Battery
		if ((catchall.data.get(0) == 0x01 || catchall.data.get(0) == 0x02) && (catchall.data.get(1) == 0xFF)) {
			for (int i = 4; i < (MsgLength-3); i++) {
				if (catchall.data.get(i) == 0x21) { // check the data ID and data type
					// next two bytes are the battery voltage
					resultMap = getBatteryResult((catchall.data.get(i+2)<<8) + catchall.data.get(i+1))
					break
				}
			}
		}
	}
	return resultMap
}

// Convert raw 4 digit integer voltage value into percentage based on minVolts/maxVolts range
private Map getBatteryResult(rawValue) {
    // raw voltage is normally supplied as a 4 digit integer that needs to be divided by 1000
    // but in the case the final zero is dropped then divide by 100 to get actual voltage value 
    def rawVolts = rawValue / 1000
	def minVolts = 2.7
    def maxVolts = 3.2
    
    def pct = (rawVolts - minVolts) / (maxVolts - minVolts)
    def roundedPct = Math.min(100, Math.round(pct * 100))

    def result = [
        name: 'battery',
        value: roundedPct,
        unit: "%"
    ]

    return result
}

// If currently in 'active' motion detected state, stopMotion() resets to 'inactive' state and displays 'no motion'
def stopMotion() {
	if (device.currentState('motion')?.value == "active") {
		def seconds = motionreset ? motionreset : 60
		sendEvent(name:"motion", value:"inactive")
		log.debug "${device.displayName} reset to no motion after ${seconds} seconds"
	}
} 

// installed() runs just after a sensor is paired using the "Add a Thing" method in the SmartThings mobile app
def installed() {
	// This basically tells Device Health to assume the button is online unless the hub if offline.
    sendEvent( name: "DeviceWatch-Enroll", value: JsonOutput.toJson( [protocol: "zigbee", scheme:"untracked"] ), displayed: false )
}

// configure() runs after installed() when a sensor is paired
def configure() {
}

// updated() will run twice every time user presses save in preference settings page
def updated() {
}

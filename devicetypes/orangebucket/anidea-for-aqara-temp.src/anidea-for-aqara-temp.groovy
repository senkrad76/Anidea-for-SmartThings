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
 * Anidea for Aqara Temp
 * =====================
 * Version:	 20.02.28.00
 *
 * This device handler is a reworking of the 'Xiaomi Aqara Temperature Humidity Sensor' DTH by
 * 'bspranger' that adapts it for the 'new' environment. It has been stripped of the 'tiles', 
 * custom attributes, most of its preferences, and much of the logging. The Health Check has been
 * copied from the IKEA motion sensor handler and modified. An atmosphericPressure attribute has
 * been added as despite the shedload of other attributes, the pressure wasn't one of them.
 */
 
 metadata
{
	definition ( name: 'Anidea for Aqara Temp', namespace: 'orangebucket', author: 'Graham Johnson',
				 vid: 'anidea-aqara-temp', mnmn: '0AQ5' )
	{
            capability 'Temperature Measurement'
            capability "Relative Humidity Measurement"
            capability "Battery"
            capability "Health Check"
            capability "Sensor"

			attribute 'atmosphericPressure', 'number'

			fingerprint profileId: "0104", deviceId: "5F01", inClusters: "0000, 0003, FFFF, 0402, 0403, 0405", outClusters: "0000, 0004, FFFF", manufacturer: "LUMI", model: "lumi.weather", deviceJoinName: "Aqara Temperature Sensor"
	}

	preferences
    {
		input "tempOffset", "decimal", title:"Temperature Offset", description:"Adjust temperature by this many degrees", range:"*..*"
		input "humidOffset", "number", title:"Humidity Offset", description:"Adjust humidity by this many percent", range: "*..*"
		input "pressOffset", "number", title:"Pressure Offset", description:"Adjust pressure by this many units", range: "*..*"
	}
}

// Parse incoming device messages to generate events
def parse(String description)
{
    log.debug "${device.displayName}: Parsing description: ${description}"

	// getEvent automatically retrieves temp and humidity in correct unit as integer
	Map map = zigbee.getEvent(description)

	// Send message data to appropriate parsing function based on the type of report
	if (map.name == "temperature") {
        def temp = parseTemperature(description)
		map.value = displayTempInteger ? (int) temp : temp
		map.descriptionText = "${device.displayName} temperature is ${map.value}°${temperatureScale}"
		map.translatable = true
	} else if (map.name == "humidity") {
		map.value = humidOffset ? (int) map.value + (int) humidOffset : (int) map.value
	} else if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	} else if (description?.startsWith('read attr - raw:')) {
		map = parseReadAttr(description)
	} else {
		log.debug "${device.displayName}: was unable to parse ${description}"
	}

	if (map) {
		log.debug "${device.displayName}: Parse returned ${map}"
		return createEvent(map)
	} else
		return [:]
}

// Calculate temperature with 0.1 precision in C or F unit as set by hub location settings
private parseTemperature( String description )
{
	def temp = ((description - "temperature: ").trim()) as Float
	def offset = tempOffset ? tempOffset : 0
	temp = (temp > 100) ? (100 - temp) : temp
    temp = (temperatureScale == "F") ? ((temp * 1.8) + 32) + offset : temp + offset
	return temp.round(1)
}

// Check catchall for battery voltage data to pass to getBatteryResult for conversion to percentage report
private Map parseCatchAllMessage( String description )
{
	Map resultMap = [:]
	def catchall = zigbee.parse(description)
	log.debug catchall

	if (catchall.clusterId == 0x0000)
    {
		def length = catchall.data.size()
		// Original Xiaomi CatchAll does not have identifiers, first UINT16 is Battery
		if ((catchall.data.get(0) == 0x01 || catchall.data.get(0) == 0x02) && (catchall.data.get(1) == 0xFF))
        {
			for (int i = 4; i < ( length - 3 ); i++)
            {
				if ( catchall.data.get(i) == 0x21 )
                { // check the data ID and data type
					// next two bytes are the battery voltage
					resultMap = getBatteryResult( ( catchall.data.get( i + 2 ) << 8 ) + catchall.data.get( i + 1 ) )
					break
				}
			}
		}
	}
	return resultMap
}

// Parse pressure report or battery report on reset button press
private Map parseReadAttr(String description)
{
	Map resultMap = [:]

	def cluster = description.split(",").find {it.split(":")[0].trim() == "cluster"}?.split(":")[1].trim()
	def attrId = description.split(",").find {it.split(":")[0].trim() == "attrId"}?.split(":")[1].trim()
	def value = description.split(",").find {it.split(":")[0].trim() == "value"}?.split(":")[1].trim()

	if ((cluster == "0403") && (attrId == "0000")) {
		def result = value[ 0..3 ]
		float pressureval = Integer.parseInt(result, 16)

		// mbar
		pressureval = (pressureval/10) as Float
		pressureval = pressureval.round( 1 );

        if (settings.pressOffset) {
		pressureval = (pressureval + settings.pressOffset)
		}

		pressureval = pressureval.round(2);

		resultMap = [ name: 'atmosphericPressure', value: pressureval, unit: 'mbar' ]
	} 
    else if (cluster == "0000" && attrId == "0005")  {
		// Not interested.
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

    return [ name: 'battery', value: roundedPct ]
}

// installed() runs just after a sensor is paired using the "Add a Thing" method in the SmartThings mobile app
def installed() {
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 10 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

// configure() runs after installed() when a sensor is paired
def configure() {
}

// updated() will run twice every time user presses save in preference settings page
def updated() {
}
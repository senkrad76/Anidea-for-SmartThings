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
 * Anidea for Aqara Temoerature
 * ============================
 * Version:	 20.03.05.01
 *
 * This device handler is a reworking of the 'Xiaomi Aqara Temperature Humidity Sensor' DTH by
 * 'bspranger' that adapts it for the 'new' environment. It has been stripped of the 'tiles', 
 * custom attributes, most of its preferences, and much of the logging. The Health Check has been
 * copied from the IKEA motion sensor handler and modified. The proposed Atmospheric Pressure
 * Measurement has been added (previously, not even a custom attribute was defined).
 */
 
metadata
{
	definition ( name: 'Anidea for Aqara Temperature', namespace: 'orangebucket', author: 'Graham Johnson',
    			 ocfDeviceType: 'oic.d.thermostat' )
	{
            capability 'Temperature Measurement'
            capability 'Relative Humidity Measurement'
            capability 'Atmospheric Pressure Measurement'
            capability 'Battery'
            capability 'Health Check'
            capability 'Sensor'

			fingerprint profileId: '0104', deviceId: '5F01', inClusters: '0000, 0003, FFFF, 0402, 0403, 0405', outClusters: '0000, 0004, FFFF', manufacturer: 'LUMI', model: 'lumi.weather', deviceJoinName: 'Aqara Temperature Sensor'
	}

	preferences
    {
		input 'pressoffset', 'number',  title: 'Atmospheric Pressure Offset',   description: 'Adjust pressure by this many units',      range: '*..*'
		input 'humidoffset', 'number',  title: 'Humidity Offset',    			description: 'Adjust humidity by this many percent',    range: '*..*'
        input 'tempoffset',  'decimal', title: 'Temperature Offset', 			description: 'Adjust temperature by this many degrees', range: '*..*'
	}
}

// installed() is called when the device is paired, and when the device is updated in the IDE.
def installed()
{	
	logger( 'installed', 'info', '' )
    
    // There may, or may not, be any technical reason for setting these attributes (it is something 
    // ST written handlers seeem to do a lot). However it is handy for seeing when readings from the
    // sensor are being received.
    sendEvent( name: 'temperature', 		value: 0, 		unit: 'C',		displayed: false )
    sendEvent( name: 'humidity', 			value: 100,		unit: '%',		displayed: false )
    sendEvent( name: 'atmosphericPressure', value: 1000, 	unit: 'mbar',	displayed: false )
    sendEvent( name: 'battery', 			value: 50, 		unit: '%',		displayed: false )
        
	// Try with a 2 hour 10 minute check interval.
    sendEvent( name: 'checkInterval', value: 2 * 60 * 60 + 10 * 60, displayed: false, data: [ protocol: 'zigbee', hubHardwareId: device.hub.hardwareID ] )
}

// updated() seems to be called after installed() when the handler is first installed, and when it is updated
// using the IDE, provided something has actually changed.  It runs whenever settings are updated. It was often 
// seen running twice in quick succession, so many developers chose to debounce it.
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

// Parse incoming device messages to generate events
def parse( String description )
{
    logger( 'parse', 'debug', description )

	// getEvent automatically retrieves temp and humidity in correct unit as integer
	Map map = zigbee.getEvent( description )

	// Send message data to appropriate parsing function based on the type of report
	if ( map.name == 'temperature' )
    {
    	// Get a (possibly converted) value with more precision.
        map.value = temperature( description )
        
        // Lose the unwanted bits.
        map.remove( 'descriptionText' )
        map.remove( 'translatable' )
        
        // The unit is already in the map so need to tweak if converted.
		map.unit = temperatureScale
	} 
    else if ( map.name == 'humidity' )
    {
		map.value = humidoffset ? (int) map.value + (int) humidoffset : (int) map.value
	}
    else if ( description?.startsWith( 'catchall:' ) )
    {
		map = catchall( description )
	}
    else if ( description?.startsWith( 'read attr - raw:' ) )
    {
		map = readattr( description )
	} else
    {
		// Not really interested.
	}

	logger( 'parse', 'info', map )
    
	return createEvent( map )
}

// This goes back to the original description as zigbee.getEvent() rounded it to an integer.
def temperature( String description )
{
	def temp = ( (description - "temperature: ").trim() ) as Float
	def offset = tempoffset ? tempoffset : 0
	temp = (temp > 100) ? (100 - temp) : temp
    
    temp = ( temperatureScale == "F" ) ? ( ( temp * 1.8) + 32 ) + offset : temp + offset
    
	return temp.round(1)
}

// Check catchall for battery voltage data to pass to getBatteryResult for conversion to percentage report
Map catchall( String description )
{
	logger( 'catchall', 'debug', description )

    Map result = [:]
	def catchall = zigbee.parse( description )

	if ( catchall.clusterId == 0x0000 )
    {
		def length = catchall.data.size()
		// Original Xiaomi CatchAll does not have identifiers, first UINT16 is Battery
		if ( (catchall.data.get( 0 ) == 0x01 || catchall.data.get( 0 ) == 0x02 ) && ( catchall.data.get( 1 ) == 0xFF ) )
        {
			for ( int i = 4; i < ( length - 3 ); i++ )
            {
				if ( catchall.data.get( i ) == 0x21 )
                { // check the data ID and data type
					// next two bytes are the battery voltage
					result = battery( ( catchall.data.get( i + 2 ) << 8 ) + catchall.data.get( i + 1 ) )
					break
				}
			}
		}
	}
	return result
}

// Parse pressure report or battery report on reset button press
Map readattr( String description )
{
	logger( 'readattr', 'debug', description )
    
    Map map = [:]

	def cluster = description.split( "," ).find { it.split( ":" )[ 0 ].trim() == "cluster" }?.split( ":" )[ 1 ].trim()
	def attrid  = description.split( "," ).find { it.split( ":" )[ 0 ].trim() == "attrId"  }?.split( ":" )[ 1 ].trim()
	def value   = description.split( "," ).find { it.split( ":" )[ 0 ].trim() == "value"   }?.split( ":" )[ 1 ].trim()

	if ( ( cluster == "0403" ) && ( attrid == "0000" ) )
    {
		def result = value[ 0..3 ]
		float pressureval = Integer.parseInt( result, 16 )

		// mbar
		pressureval = ( pressureval / 10 ) as Float
		pressureval = pressureval.round( 1 );

        if ( settings.pressoffset )
        {
			pressureval = ( pressureval + settings.pressoffset )
		}

		pressureval = pressureval.round( 2 );

		map = [ name: 'atmosphericPressure', value: pressureval, unit: 'mbar' ]
	} 
    else if (cluster == "0000" && attrid == "0005")  {
		// Not interested.
	}
    
	return map
}

Map battery( raw )
{
	// Experience shows that a new battery in an Aqara sensor reads about 3.2V, and they need
	// changing when you get down to about 2.7V. It really isn't worth messing around with 
	// preferences to fine tune this.

	def rawvolts = raw / 1000
    
	logger( 'battery', 'debug', "$rawvolts V" )
    
	def minvolts = 2.7
	def maxvolts = 3.2
	def percent = Math.min( 100, Math.round( 100.0 * ( rawvolts - minvolts ) / ( maxvolts - minvolts ) ) )
    
	return [ name: 'battery', value: percent, isStateChange: true ]
}
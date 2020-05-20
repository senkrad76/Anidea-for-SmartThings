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
 * Anidea for Virtual Temperature
 * ==============================
 * Version:	 20.05.20.01
 *
 * This handler basically implements the same functionality as the Simulated Temperature
 * Sensor but works with the new app.
 */
 
 metadata
 {
    definition( name: 'Anidea for Virtual Temperature', namespace: 'orangebucket', author: 'Graham Johnson',
    			ocfDeviceType: 'oic.d.thermostat' )
    {
        capability 'Temperature Measurement'
        // The Switch Level capability has been (ab)used to provide a temperature control in the mobile app.
        capability 'Switch Level'
        
        // As the handler has commands, it is an actuator as well as a sensor.
        capability 'Actuator'
        capability 'Sensor'
        capability 'Health Check'

		// These are the commands supported by the Simulated Temperature Sensor, and so
        // are being regarded as a de facto standard.
        command 'up'
        command 'down'
        command 'setTemperature', [ 'number' ]
    }
    
    preferences
    {
		input 'mintemp', 'number',  title: '0% temperature'
		input 'maxtemp', 'number',  title: '100% temperature'
	}
}

def installed()
{
	logger( 'installed', 'info', '' )

    sendEvent( name: 'DeviceWatch-Enroll', value: [ protocol: 'cloud', scheme:'untracked' ].encodeAsJson(), displayed: false )
    
    // Seed the attributes with values to keep the new app happy. Normally tend to set displayed: false on these
    // but it easier to just use the method as it ties them together. Use a reasonable room temperature as something
    // neutral.
    setTemperature( temperatureScale == 'F' ? 68 : 20 )
}

def updated()
{
    logger( 'updated', 'info', '' )
    
    if ( mintemp || mintemp == 0 ) logger( 'updated', 'debug', "0% value is $mintemp" )
    if ( maxtemp || maxtemp == 0 ) logger( 'updated', 'debug', "100% value is $maxtemp" )
    
    // Set the temperature again as the level may have been altered.
    setTemperature( device.currentValue( 'temperature' ) )
}

def logger( method, level = 'debug', message = '' )
{
	log."${level}" "$device.displayName [$device.name] [${method}] ${message}"
}

def setLevel( value, rate = null )
{
	logger( 'setLevel', 'info', "$value" )
    
    def zero    = mintemp ? mintemp : ( mintemp == 0 ? mintemp : -40 ) 
    def hundred = maxtemp ? maxtemp : ( maxtemp == 0 ? mintemp : ( temperatureScale == 'F' ? 302 : 150 ) )
    
    value = zero + ( value * ( hundred - zero ) / 100.0 )
    
    setTemperature( value )
}

// Parse incoming device messages to generate events
def parse( String description )
{
    logger( 'parse', 'info', '' )
}

def up()
{
	logger( 'up', 'info', '' )
    
    setTemperature( device.currentValue( 'temperature' ) + 1.0 )
}

def down()
{
	logger( 'down', 'info', '' )
    
    setTemperature( device.currentValue( 'temperature' ) - 1.0 )
}

def setTemperature( newtemp )
{
	logger( 'setTemperature', 'info', "$newtemp" )
    
    newtemp = ( (double) newtemp).round( 2 )
    
    def zero    = mintemp ? mintemp : ( mintemp == 0 ? mintemp : -40 ) 
    def hundred = maxtemp ? maxtemp : ( maxtemp == 0 ? mintemp : ( temperatureScale == 'F' ? 302 : 150 ) )
    
    def level = Math.round( ( ( newtemp - zero ) / ( hundred - zero ) ) * 100.0 )
    
    if ( level < 0 )
	{
    	logger( 'setTemperature', 'debug', 'Temperature was below minimum.' )
        
        level = 0
        newtemp = mintemp
    }
    
    if ( level > 100 )
    {
    	logger( 'setTemperature', 'debug', 'Temperature was above maximum.' )
        
        level   = 100
        newtemp = maxtemp
    }
    
    logger( 'setTemperature', 'debug', "temperature: $newtemp $temperatureScale level: $level %" )
    
    sendEvent( name: 'temperature', value: newtemp, unit: temperatureScale )
    sendEvent( name: 'level',       value: level,   unit: '%' )
}
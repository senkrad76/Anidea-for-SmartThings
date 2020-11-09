/*
 * ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * ---------------------------------------------------------------------------------
 *
 * Anidea for Virtual Humidity
 * ===========================
 * Version:	 20.10.21.01
 *
 * This handler implements a virtual Relative Humidity sensor, similar in structure
 * to Anidea for Virtual Temperature.
 */
 
 metadata
 {
    definition( name: 'Anidea for Virtual Humidity', namespace: 'orangebucket', author: 'Graham Johnson' )
    {
        capability 'Relative Humidity Measurement'
        // The Switch Level capability has been (ab)used to provide a humidity control in the mobile app.
        capability 'Switch Level'
        
        // As the handler has commands, it is an actuator as well as a sensor.
        capability 'Actuator'
        capability 'Sensor'
        capability 'Health Check'

	    // These are similar to the commands used by the Anidea for Virtual Temperature handler.
        command 'up'
        command 'down'
        command 'setHumidity', [ 'number' ]
    }
    
    preferences
    {
	}
}

def installed()
{
	logger( 'installed', 'info', '' )

    sendEvent( name: 'DeviceWatch-Enroll', value: [ protocol: 'cloud', scheme:'untracked' ].encodeAsJson(), displayed: false )
    
    // Seed the attributes with values to keep the new app happy. Normally tend to set displayed: false on these
    // but it easier to just use the method as it ties them together. Use a middling humidity as something
    // neutral.
    setHumidity( 50 )
}

def updated()
{
    logger( 'updated', 'info', '' )
}

def logger( method, level = 'debug', message = '' )
{
	log."${level}" "$device.displayName [$device.name] [${method}] ${message}"
}

def setLevel( value, rate = null )
{
	logger( 'setLevel', 'info', "$value" )
    
    setHumidity( value )
}

// Parse incoming device messages to generate events
def parse( String description )
{
    logger( 'parse', 'info', '' )
}

def up()
{
	logger( 'up', 'info', '' )
    
    setHumidity( device.currentValue( 'humidity' ) + 1.0 )
}

def down()
{
	logger( 'down', 'info', '' )
    
    setHumidity( device.currentValue( 'humidity' ) - 1.0 )
}

def setHumidity( newhum )
{
	logger( 'setHumidity', 'info', "$newhum" )
    
    newhum = ( (double) newhum).round( 2 )
    
    newhum = Math.round( newhum )
    
    if ( newhum < 0 )
	{
    	logger( 'setHumidity', 'debug', 'Humidity was below minimum.' )
        
        newhum = 0
    }
    
    if ( newhum > 100 )
    {
    	logger( 'setHumidity', 'debug', 'Humidity was above maximum.' )
        
        newhum  = 100
    }
    
    logger( 'setHumidity', 'debug', "humidity: $newhum %" )
    
    sendEvent( name: 'humidity', value: newhum, unit: '%' )
    sendEvent( name: 'level',    value: newhum, unit: '%' )
}
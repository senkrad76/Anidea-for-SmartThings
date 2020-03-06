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
 * Anidea for Aqara/Mijia Contact
 * ==============================
 * Version:	 20.03.06.04
 *
 * This device handler is a reworking of the 'Xiaomi' Door and Window Sensors DTHs by 'bspranger'
 * that combines and adapt them for the 'new' environment. It has been stripped of the 'tiles', custom 
 * attributes, all its preferences and most of the logging. Health Check support has been tidied.
 * The layout of braces and spacing in brackets has been adjusted for personal taste, along with any 
 * local use of camel case.
 *
 * Code has been ported for the MCCGQ01LM (Mijia) and MCCGQ11LM (Aqara).  Apart from the fingerprints,
 * the only difference was in how the same on/off event was process
 */

metadata
{
	definition( name: 'Anidea for Aqara/Mijia Contact', namespace: 'orangebucket', author: 'Graham Johnson' )
	{
   		capability 'Contact Sensor'
        capability 'Battery'
		capability 'Health Check'
		capability 'Sensor'

		// These commands may be used to set the contact status to a particular value, in the event the sensor
        // does not correctly report open/close events.  They should be used with caution as if the sensor
        // genuinely doesn't know it is closed, it cannot send an open report.
   		command "setopen"
   		command "setclosed"
   
		fingerprint endpointId: '01', profileId: '0104', deviceId: '0104', inClusters: '0000, 0003, FFFF, 0019', outClusters: '0000, 0004, 0003, 0006, 0008, 0005 0019', manufacturer: 'LUMI', model: 'lumi.sensor_magnet',     deviceJoinName: 'Lumi Mijia MCCGQ01LM'
   		fingerprint endpointId: "01", profileId: "0104", deviceId: "5F01", inClusters: "0000, 0003, FFFF, 0006", outClusters: "0000, 0004, FFFF", 						 manufacturer: "LUMI", model: "lumi.sensor_magnet.aq2", deviceJoinName: "Lumi Aqara MCCGQ11LM"
   }
   
   preferences
   {
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
    
    // In the absence of any information about how to make the sensor report its settings on demand,
    // fake some default values for the attributes. For binary attributes, use whichever one seems to
    // attract the most attention.
    sendEvent( name: 'contact',	value: 'open',			  displayed: false )
    sendEvent( name: 'battery', value: 50,	   unit: '%', displayed: false )   
    
    // Record that the contact state was set manually.
    state.manualcontact = true
    
    // Health Check is undocumented but lots of ST DTHs create a 'checkInterval' event in this way.
    // Aqara sensors seem to send a battery report every 50-60 minutes, so allow for missing one and then 
    // add a bit of slack on top.
    sendEvent( name: 'checkInterval', value: 2 * 60 * 60 + 10 * 60, displayed: false, data: [ protocol: 'zigbee', hubHardwareId: device.hub.hardwareID ] )
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

// Parse incoming device messages to generate events
def parse( String description )
{
    logger( 'parse', 'info', description )
    
    def map = [:]
    
    def event
    
    if (description?.startsWith( 'catchall:' ) )
    {
        map = catchall( description )
    }
    else if ( description?.startsWith( 'read attr - raw:' ) )
    {
        // Only seems to give the model, so really not interested.  
    }
    else if ( ( event = zigbee.getEvent( description ) ) )
    {
        map = [ name: 'contact', value: event.value == 'off' ? 'closed' : 'open' ] 
        
        // If the contact state has been overridden, set the isStateChange flag to true to
        // make sure the correct state propagates.
        if ( state.manualcontact )
        {
			map.isStateChange = true
            state.manualcontact = false
        }
    }

	logger( 'parse', 'debug', map )
    
    return createEvent( map );
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
    
	return [ name: 'battery', value: percent, isStateChange: true ]
}

// Check catchall for battery voltage data to pass to getBatteryResult for conversion to percentage report
Map catchall( String description )
{
	def map = [:]
	def parsed = zigbee.parse( description )
	log.debug catchall

	if ( parsed.clusterId == 0x0000 )
    {
		def length = parsed.data.size()
		// Xiaomi CatchAll does not have identifiers, first UINT16 is Battery
		if ( ( parsed.data.get( 0 ) == 0x01 || parsed.data.get( 0 ) == 0x02 ) && ( parsed.data.get( 1 ) == 0xFF ) )
        {
			for ( int i = 4; i < ( length-3 ); i++) {
				if ( parsed.data.get( i ) == 0x21) // check the data ID and data type
                {
					// next two bytes are the battery voltage
					map = battery( (parsed.data.get( i+2 ) << 8 ) + parsed.data.get( i+1 ) )
					break
				}
			}
		}
	}
    
	return map
}

def setopen()
{
	logger( 'setopen', 'info', '')
    
    state.manualcontact = true
    
	sendEvent( name: 'contact', value: 'open', descriptionText: 'Contact status has been set manually.' )
}

def setclosed()
{
	logger( 'setclosed', 'info', '')
    
    state.manualcontact = true
        
    sendEvent( name: 'contact', value: 'closed', descriptionText: 'Contact status has been set manually.' )
} 

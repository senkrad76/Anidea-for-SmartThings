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
 * Version:	 20.02.25.00
 *
 * This device handler is a reworking of the 'Xiaomi Aqara Motion' DTH by 'bspranger' that
 * adapts it for the 'new' environment. It has been stripped of the 'tiles', custom attributes,
 * most of its preferences, and much of the logging. The Health Check has been switched to
 * be untracked rather than implementing a 'checkinterval' as it isn't clear it was implemented
 * correctly. There wasn't a 'ping()', for example, but should there be?
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata
{
    definition( name: "Anidea for Aqara Motion", namespace: "orangebucket", author: "Graham Johnson", ocfDeviceType: "x.com.st.d.sensor.motion" )
    {
        capability "Motion Sensor"
        capability "Illuminance Measurement"
        // The 'Battery' capability is obviously useful.
        capability "Battery"
        // The 'Configuration' capability brings the configure() method into play.
        capability "Configuration"
        // The 'Health Check' support is copied from the IKEA button handler.
		capability "Health Check"
        // This has been deprecated for years but ActionTiles was once said to look for it, and certainly
        // webCoRE uses it when selecting devices.
		capability "Sensor"

		// These Zigbee fingerprints have been inherited, but have been reformatted to aid comparison.
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0107", inClusters: "0000, FFFF, 0406, 0400, 0500, 0001, 0003", outClusters: "0000, 0019", manufacturer: "LUMI", model: "lumi.sensor_motion.aq2", deviceJoinName: "Aqara Motion Sensor"
        fingerprint                   profileId: "0104", deviceId: "0104", inClusters: "0000, 0400, 0406, FFFF",                   outClusters: "0000, 0019", manufacturer: "LUMI", model: "lumi.sensor_motion",     deviceJoinName: "Aqara Motion Sensor"
    }

	preferences
    {
		input "motionreset", "number", title: "Motion Reset Period", description: "Enter number of seconds (default = 60)", range: "1..7200"
	}	
}


// installed() is called when the device is paired, and when the device is updated in the IDE.
def installed()
{	
	logger( 'installed', 'info', '' )
    
    // It might be that a "checkInterval" is more appropriate in the longer term, but it isn't clear
    // if that should have a ping() method that generates a response so it seems better not to bother.
sendEvent( name: 'checkInterval', value: null)
	// This basically tells Device Health to assume the button is online unless the hub if offline.
    sendEvent( name: 'DeviceWatch-Enroll', value: JsonOutput.toJson( [protocol: 'zigbee', scheme: 'untracked' ] ), displayed: false )
}

// updated() seems to be called after installed() when the handler is first installed, but not when
// it is updated in the IDE.  It runs whenever settings are updated in the mobile app. It is often 
// seen running twice in quick succession, so is often debounced.
def updated()
{
	logger( 'updated', 'info', '' )
}

// configure() seems to be intended for configuring the remote device, and like updated() is often called twice,
// sometimes even with the same timestamp. It seems to be called after installed(), but only when the 
// handler has the 'Configuration' capability. It isn't really needed in this handler.
def configure()
{
	logger( 'configure', 'info', '' )
}

def logger(method, level = "debug", message ="")
{
	log."${level}" "$device.displayName [$device.name] [${method}] ${message}"
}

// parse() is called when the hub receives a message from a device.
def parse( String description )
{
    logger( 'parse', 'debug', description )

    Map map = [:]
	
	// Send message data to appropriate parsing function based on the type of report	
    if ( description?.startsWith( 'illuminance:' ) )
    {
        map = illuminance( description )
    }
    else if ( description?.startsWith( 'read attr -' ) )
    {
        map = readattr( description )
    }
    else if (description?.startsWith( 'catchall:' ) )
    {
        map = catchall( description )
    }

	logger( 'parse', 'info', map )
    
    return createEvent( map )
}

// Parse illuminance report
Map illuminance( String description )
{    
	logger( 'illuminance', 'info', description )

	def lux = ( ( description - "illuminance: " ).trim() ) as int

	return [ name: 'illuminance', value: lux ]
}

// Parse motion active report or model name message on reset button press
Map readattr( String description )
{
	logger( 'readattr', 'info', '' )

    def cluster = description.split( "," ).find {it.split( ":" )[ 0 ].trim() == "cluster" }?.split( ":" )[ 1 ].trim()
    def attrod  = description.split( "," ).find {it.split( ":" )[ 0 ].trim() == "attrId"  }?.split( ":" )[ 1 ].trim()
    def value   = description.split( "," ).find {it.split( ":" )[ 0 ].trim() == "value"   }?.split( ":" )[ 1 ].trim()

    Map result = [:]

	// The sensor only sends a motion detected message so the reset to no motion is performed in code
    if ( cluster == "0406" & value == "01" )
    {       
		def seconds = motionreset ? motionreset : 60
		result = [ name: 'motion', value: 'active' ]
        
		runIn( seconds, resetmotion )
	}
	else if (cluster == "0000" && attrid == "0005")
    {
    	// Really not interested in this.
    }
    
    return result
}

// If currently in 'active' motion detected state, resetmotion() resets to 'inactive' state and displays 'no motion'
def resetmotion()
{
	logger( 'resetmotion', 'info', '' )
        
	if ( device.currentState( 'motion' )?.value == 'active' )
    {
		sendEvent( name: 'motion', value: 'inactive' )
	}
} 

// Check catchall for battery voltage data
Map catchall( String description )
{
    logger( 'catchall', 'info', catchall )
    
    Map result = [:]
	def catchall = zigbee.parse( description )

	if ( catchall.clusterId == 0x0000 )
    {
		def MsgLength = catchall.data.size()
		
        // Xiaomi CatchAll does not have identifiers, first UINT16 is Battery
		if ( ( catchall.data.get( 0 ) == 0x01 || catchall.data.get( 0 ) == 0x02 ) && ( catchall.data.get( 1 ) == 0xFF ) ) {
			for ( int i = 4; i < ( MsgLength - 3 ); i++)
            {
				if ( catchall.data.get( i ) == 0x21 )
                {
                	// check the data ID and data type
					// next two bytes are the battery voltage
					result = battery( ( catchall.data.get( i+2 ) << 8 ) + catchall.data.get( i+1 ) )
					break
				}
			}
		}
	}
	return result
}

Map battery( raw )
{
    // Experience shows that a new battery in an Aqara sensor reads about 3.2V, and they need
	// changing when you get down to about 2.7V. It really isn't worth messing around with 
	// preferences to fine tune this.

	def rawvolts = raw / 1000
    
    logger( 'battery', 'info', "${rawvolts} V" )
        
	def minvolts = 2.7
	def maxvolts = 3.2
	def percent = Math.min( 100, Math.round( 100.0 * ( rawvolts - minvolts ) / ( maxvolts - minvolts ) ) ) 
    
	return [ name: 'battery', value: percent ]
}

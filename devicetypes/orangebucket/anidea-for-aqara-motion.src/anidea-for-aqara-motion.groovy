/*
 * ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * ---------------------------------------------------------------------------------
 *
 * Anidea for Aqara Motion
 * =======================
 * Version:	 20.08.27.00
 *
 * This device handler is a reworking of the 'Xiaomi Aqara Motion' DTH by 'bspranger' that
 * adapts it for the 'new' environment. It has been stripped of the 'tiles', custom attributes,
 * most of its preferences, and much of the logging. The Health Check has been tidied up. The
 * handler supports 'active' and 'inactive' commands to override the status.
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata
{
    definition( name: 'Anidea for Aqara Motion', namespace: 'orangebucket', author: 'Graham Johnson', 
    			ocfDeviceType: 'x.com.st.d.sensor.motion', mnmn: 'SmartThingsCommunity', vid: '9f5d4cb5-ace7-3971-af7c-ceb695925add' )
    {
        //
        capability 'Motion Sensor'
        //
        capability 'Illuminance Measurement'
        // The 'Battery' capability is obviously useful.
        capability 'Battery'
        // The 'Health Check' support is based on the IKEA button handler.
		capability 'Health Check'
        // This has been deprecated for years but ActionTiles was once said to look for it, and certainly
        // webCoRE uses it when selecting devices.
		capability 'Sensor'
        
        // If the reset timer doesn't fire, as happened a lot in April 2020, the status can get stuck
        // on active. Therefore add commands to manually set the status, using the names from the
        // Simulated Motion Sensor handler.
        capability 'circlemusic21301.motionCommands'

		// These Zigbee fingerprints have been inherited, but have been reformatted to aid comparison.
        fingerprint endpointId: '01', profileId: '0104', deviceId: '0107', inClusters: '0000, FFFF, 0406, 0400, 0500, 0001, 0003', outClusters: '0000, 0019', manufacturer: 'LUMI', model: 'lumi.sensor_motion.aq2', deviceJoinName: 'Aqara Motion RTCGQ11LM'
        fingerprint                   profileId: "0104", deviceId: "0104", inClusters: "0000, 0400, 0406, FFFF",                   outClusters: '0000, 0019', manufacturer: 'LUMI', model: 'lumi.sensor_motion',     deviceJoinName: 'Aqara Motion Sensor'
    }

	preferences
    {
    	// The sensor sleeps for a minute after detecting motion and does not send an 'inactive' report.
        // The device handler will, by default, reset the motion state after sixty-five seconds to allow
        // a prompt new motion report to arrive first and restart the timer.
		input 'motionreset', 'number', title: 'Motion Reset Period', description: 'Enter number of seconds (default = 65)', range: '1..7200'
	}	
}

// installed() is called when the device is paired, and when the device is updated in the IDE.
def installed()
{	
	logger( 'installed', 'info', '' )
    
    // Set an initial twenty-four hour checkInterval for Health Check. Reduce it to two hours
    // and ten minutes when the first 50-60 min battery report arrives.
    sendEvent( name: 'checkInterval', value: 86400, displayed: false, data: [ protocol: 'zigbee', hubHardwareId: device.hub.hardwareID ] )
 
    // The SmartThings handlers seem keen on initialising the attributes, so ...
    sendEvent( name: 'motion', value: 'inactive', displayed: false )
    sendEvent( name: 'illuminance', value: 0, displayed: false )   
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
		def seconds = motionreset ? motionreset : 65
		result = [ name: 'motion', value: 'active' ]
        
		runIn( seconds, resetmotion )
	}
	else if (cluster == "0000" && attrid == "0005")
    {
    	// Really not interested in this.
    }
    
    return result
}

// resetmotion() is called by a timer to reset motion to the 'inactive' state.
def resetmotion()
{
	logger( 'resetmotion', 'info', '' )
        
	sendEvent( name: 'motion', value: 'inactive' )
} 

// Check catchall for battery voltage data
Map catchall( String description )
{    
    logger( 'catchall', 'info', '' )
    
    Map result = [:]
	def parsedcatchall = zigbee.parse( description )

	if ( parsedcatchall.clusterId == 0x0000 )
    {
		def length = parsedcatchall.data.size()
		
        // Xiaomi CatchAll does not have identifiers, first UINT16 is Battery
		if ( ( parsedcatchall.data.get( 0 ) == 0x01 || parsedcatchall.data.get( 0 ) == 0x02 ) && ( parsedcatchall.data.get( 1 ) == 0xFF ) )
        {
			for ( int i = 4; i < ( length - 3 ); i++ )
            {
				if ( parsedcatchall.data.get( i ) == 0x21 )
                {
                	// check the data ID and data type
					// next two bytes are the battery voltage
					result = battery( ( parsedcatchall.data.get( i + 2 ) << 8 ) + parsedcatchall.data.get( i + 1 ) )
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
    
    logger( 'battery', 'info', "$rawvolts V" )
        
	def minvolts = 2.7
	def maxvolts = 3.2
	def percent = Math.min( 100, Math.round( 100.0 * ( rawvolts - minvolts ) / ( maxvolts - minvolts ) ) ) 
 
 	// If checkInterval is still 24 hours, set a shorter one.
    if ( device.currentValue( 'checkInterval' ) == 86400 )
    {
        // Set checkInterval to two hours and ten minutes now a battery value has arrived.
    	sendEvent( name: 'checkInterval', value: 7800, data: [ protocol: 'zigbee', hubHardwareId: device.hub.hardwareID ] )
                       
        logger( 'battery', 'debug', 'checkInterval 7800 seconds' )
	}
 
	return [ name: 'battery', value: percent, isStateChange: true ]
}

// Manually set the motion attribute to active.  Don't call a reset timer.
def active()
{
	logger( 'active', 'info', '' )
        
	sendEvent( name: 'motion', value: 'active' )
}

// Manually set the motion attribute to inactive.
def inactive()
{
	logger( 'inactive', 'info', '' )
    
	sendEvent( name: 'motion', value: 'inactive' )
}
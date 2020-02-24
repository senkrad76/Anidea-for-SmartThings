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
 * Anidea for Aqara Button
 * =======================
 * Version:	 20.02.24.01
 *
 * This device handler is a reworking of the 'Xiaomi Aqara Button' DTH by 'bspranger' that
 * adapts it for the 'new' environment. It has been stripped of the 'tiles', custom attributes,
 * all its preferences and most of the logging. Incorrectly implemented health checking features
 * have been removed, and the new definition of the button attribute used instead of multiple 
 * buttons. The 'double' value is being used as a proxy for the unsupported hold release, and
 * 'pushed_6x' represents the button being shaken. The layout of braces and spacing in brackets
 * has been adjusted for personal taste, along with any local use of camel case.
 *
 * Code has been ported for the WXKG11LM (two versions), and the WXKG12LM. This handler has only
 * been tested with the WXKG11LM with what is said to be older firmware.
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata
{
	// The 'ocfDeviceType' comes from the IKEA button handler, as does the absence of a 'vid'.
	definition( name: "Anidea for Aqara Button", namespace: "orangebucket", author: "Graham Johnson", ocfDeviceType: "x.com.st.d.remotecontroller" )
    {
    	// The main capability is 'Button' as no other button capability has been documented in the new environment.
		capability "Button"
        // The 'Battery' capability is obviously useful.
        capability "Battery"
		// The 'Health Check' support is copied from the IKEA button handler.
		capability "Health Check"
        // This has been deprecated for years but ActionTiles was once said to look for it, and certainly
        // webCoRE uses it when selecting devices.
		capability "Sensor"

		// These Zigbee fingerprints have been inherited, but have been reformatted to aid comparison.

		// WXKG11LM (original revision)
		fingerprint deviceId: "5F01", inClusters: "0000,FFFF,0006",      outClusters: "0000,0004,FFFF", manufacturer: "LUMI", model: "lumi.sensor_switch.aq2", deviceJoinName: "Aqara Button WXKG11LM"
		// WXKG11LM (new revision)
		fingerprint deviceId: "5F01", inClusters: "0000,0012,0003",      outClusters: "0000",           manufacturer: "LUMI", model: "lumi.remote.b1acn01",    deviceJoinName: "Aqara Button WXKG11LM r2"
		// WXKG12LM
		fingerprint deviceId: "5F01", inClusters: "0000,0001,0006,0012", outClusters: "0000",           manufacturer: "LUMI", model: "lumi.sensor_switch.aq3", deviceJoinName: "Aqara Button WXKG12LM"
		fingerprint deviceId: "5F01", inClusters: "0000,0001,0006,0012", outClusters: "0000",           manufacturer: "LUMI", model: "lumi.sensor_swit",       deviceJoinName: "Aqara Button WXKG12LM"
	}

	preferences
    {
    	// No preferences are actually needed.
	}
}

// installed() is called when the device is paired, and when the device is updated in the IDE.
def installed()
{	
	logger( 'installed', 'debug', '' )
        
	// This basically tells Device Health to assume the button is online unless the hub if offline.
    sendEvent( name: "DeviceWatch-Enroll", value: JsonOutput.toJson( [protocol: "zigbee", scheme:"untracked"] ), displayed: false )

	def supportedbuttons = []
    
    // The 'pushed_6x' attribute value represents the button being shaken.
	// The 'double' attribute value is being used to represent the hold release.
    switch( device.getDataValue( "model" ) )
    {
    	case 'lumi.sensor_switch.aq2':	supportedbuttons = [ 'pushed', 'pushed_2x', 'pushed_3x', 'pushed_4x' ]
        								break
        case 'lumi.remote.b1acn01':		supportedbuttons = [ 'pushed', 'pushed_2x', 'held', 'double' ]
        								break
        default:						supportedbuttons = [ 'pushed', 'pushed_2x', 'pushed_6x', 'held', 'double' ]
        								break
    }
	sendEvent(name: "supportedButtonValues", value: supportedbuttons.encodeAsJSON(), displayed: false)
	sendEvent(name: "numberOfButtons", value: 1, displayed: false)
	sendEvent(name: "button", value: "pushed", displayed: false)
}

// updated() seems to be called after installed(), and also when the settings are updated via the mobile app.
// It often seems to be called twice in quick succession so many developers like to debounce it.
def updated()
{
	logger( 'updated', 'debug', '' )
}

// configure() seems to be intended for configuring the remote device, and like updated() is often called twice,
// sometimes even with the same timestamp. It isn't clear when it is called automatically, and if that depends
// on the Configure capability (which this handler doesn't have).
def configure()
{
	logger( 'updated', 'configure', '' )
}

def logger(method, level = "debug", message ="")
{
	log."${level}" "$device.displayName [$device.name] [${method}] ${message}"
}

// parse() is called when the hub receives a message from a device.
def parse( String description )
{
	logger( 'parse', 'debug', '' )
    
	def result = [:]

	if ( description?.startsWith( 'read attr - raw:' ) )
    {
		// The read attr messages look like this:
		// read attr - raw: 3D91010006100000100000001001, dni: 3D91, endpoint: 01, cluster: 0006, 
		//             size: 16, attrId: 0000, result: success, encoding: 10, value: 0110000000
        // That one appeared when a button was pressed but they are said to also appear when the
        // range test button is pressed (though the WXKG11LM, with older firmware, seems to send
        // out a catchall).
		result = readattr( description )
	}
    else if ( description?.startsWith( 'catchall:' ) )
    {
		// The catchall messages look something like this:
		// catchall: 0104 0000 01 01 0000 00 3D91 00 01 115F 0A 01 
		// 050042166C756D692E73656E736F725F7377697463682E61713201FF421A0121E50B0328170421A84305216B02062408000000000A21D7E2
        // It seems there is battery voltage information in there somewhere. That one came from the range test button on a
        // WXKG11LM.
		result = catchall( description )
	}
    
	return createEvent( result )
}

Map readattr(String description)
{
	// This method of extracting the data is inherited.
	def cluster  = description.split(",").find { it.split(":")[0].trim() == "cluster"}?.split(":")[1].trim()
	def attrid   = description.split(",").find { it.split(":")[0].trim() == "attrId" }?.split(":")[1].trim()
	def valuehex = description.split(",").find { it.split(":")[0].trim() == "value"  }?.split(":")[1].trim()
    
	Map result = [:]

	if ( cluster == "0006" )
    {
		// Process model WXKG11LM (original revision)
		result = buttons11( attrid, Integer.parseInt(valuehex[ 0..1 ], 16 ) )
    }
	else if ( cluster == "0012" )
	{
    	// Process model WXKG11LM (new revision) or WXKG12LM button messages
		result = buttons( Integer.parseInt( valuehex[ 2..3 ],16) )
    }
	else if (cluster == "0000" && attrid == "0005")
    {
    	// Process message containing battery voltage report

		def data = ""

		if ( valuehex.length() > 45 )
        {
			data = valuehex.split( "01FF" )[ 1 ]
			if ( data[4..7] == "0121" )
            {
				def voltage = ( Integer.parseInt( ( data[10..11] + data[8..9] ), 16 ) )
				result = battery( voltage )
			}
			data = ", data: ${valuehex.split( "01FF" )[ 1 ]}"
		}
	}
    
	return result
}

// Parse WXKG11LM (original revision) button message: press, double-click, triple-click, & quad-click
private buttons11( attrid, value )
{
	def click = [ 'pushed', 'pushed_2x', 'pushed_3x', 'pushed_4x' ]
	def result = [:]
    
	value = (attrid == "0000") ? 1 : value
    
	if (value <= 4) { result = [ name: 'button', value: click[ value - 1 ], isStateChange: true ] }
    
	return result
}

// Create map of values to be used for button events
Map buttons( value )
{
	// WXKG11LM (new revision) message values: 0: hold, 1 = push, 2 = double-click, 255 = release
	// WXKG12LM message values: 1 = push, 2 = double-click, 16 = hold, 17 = release, 18 = shaken
    
    // Treat the button being shaken as a sextuple press, and (ab)use double for the hold release as
    // there (understandably) isn't a released event.
	def click = [ 0: "held", 1: "pushed", 2: "pushed_2x", 16: "held", 17: "double", 18: "pushed_6x", 255: "double" ]
    
    return [ name: 'button', value: click[ value ], isStateChange: true ]
 }

// It seems the battery voltage data is included in catchall messages.
Map catchall( String description )
{
	Map result = [:]
	def catchall = zigbee.parseDescriptionAsMap( description )

	// Parse battery voltage data from catchall messages with payload value data larger than 10 bytes
	if ( (catchall.attrId == "0005" || catchall.attrId == "FF01") && catchall.value.size() > 20 )
    {
    	// Battery voltage value is sent as INT16 in two bytes, #6 & #7, in large-endian (reverse) order
		def batteryvoltage = catchall.data[ 7 ] + catchall.data[ 6 ]
		if ( catchall.additionalAttrs && catchall.additionalAttrs.attrId[ 0 ] == "ff01" )
			batteryvoltage = catchall.unparsedData[ 7 ] + catchall.unparsedData[ 6 ]
	
    	result = battery( Integer.parseInt( batteryvoltage, 16 ) )
	}

	return result
}

Map battery( raw )
{
	// Experience shows that a new battery in an Aqara sensor reads about 3.2V, and they need
	// changing when you get down to about 2.7V. It really isn't worth messing around with 
	// preferences to fine tune this.

	def rawvolts = raw / 1000
	def minvolts = 2.7
	def maxvolts = 3.2
	def percent = Math.min( 100, Math.round( 100.0 * ( rawvolts - minvolts ) / ( maxvolts - minvolts ) ) )
	def desc = "Battery ${rawvolts} V"

	logger( "battery" , "debug", desc )
    
	return [ name: "battery", value: percent, unit: "%" ]
}
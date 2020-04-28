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
 * Anidea for Virtual Button
 * =========================
 * Version:	 20.04.28.00
 *
 * This device handler implements a simple virtual button using the Button and
 * Momentary capabilities.
 */

metadata
{
	definition( name: 'Anidea for Virtual Button', namespace: 'orangebucket', author: 'Graham Johnson',
    			ocfDeviceType: 'x.com.st.d.remotecontroller' )
    {
    	//
		capability 'Button'
        capability 'Momentary'
		// 
		capability 'Health Check'
        //
        capability 'Actuator'
		capability 'Sensor'
        
	}

	preferences
    {
    	// No preferences are actually needed.
	}
}

// installed() is called when the device is created, and when the device is updated in the IDE.
def installed()
{	
	logger( 'installed', 'info', '' )

    // Health Check is undocumented but this seems to be the common way of creating an untracked
    // device that will appear online when the hub is up.
	sendEvent( name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false )

    // The 'down_6x' attribute value is being used to seed the button attribute. This is something ST
    // seem to do in their handlers, but using 'pushed' seems a bit silly with so many otherwise
    // unused values available.
    def supportedbuttons = [ 'pushed', 'down_6x' ]
    
	sendEvent( name: 'supportedButtonValues', value: supportedbuttons.encodeAsJSON(), displayed: false                      )
	sendEvent( name: 'numberOfButtons',       value: 1,                               displayed: false                      )
    sendEvent( name: 'button',                value: 'down_6x', 					  displayed: false, isStateChange: true )
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

// push() is the command for the Momentary capability. Make it press the button once.
def push()
{
	logger( 'push', 'info', '' )
    
    sendEvent( name: 'button', value: 'pushed', isStateChange: true )
}

// parse() is called when the hub receives a message from a device.
def parse( String description )
{
    logger( 'parse', 'debug', description )
    
	// Nothing should appear.
}
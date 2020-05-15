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
 * Anidea for Virtual Momentary
 * ============================
 * Version:	 20.05.15.00
 *
 * This device handler implements a momentary action Contact Sensor, Motion Sensor and Switch.
 * The capabilities are permanently in place but the momentary actions are controlled by
 * booleans in the settings and are not enabled by default.
 */

metadata
{
	definition( name: 'Anidea for Virtual Momentary', namespace: 'orangebucket', author: 'Graham Johnson' )
    {
    	//
        capability 'Momentary'
		//
        capability 'Contact Sensor'
        capability 'Motion Sensor'
        capability 'Switch'
        //
		capability 'Health Check'
        //
        capability 'Actuator'
		capability 'Sensor'
        
	}

	preferences
    {
        input name: 'momentarycontact', type: 'bool', title: 'Act as momentary Contact Sensor?', description: 'Enter boolean', required: true
        input name: 'momentarymotion',  type: 'bool', title: 'Act as momentary Motion Sensor?',  description: 'Enter boolean', required: true
        input name: 'momentaryswitch',  type: 'bool', title: 'Act as momentary Switch?',         description: 'Enter boolean', required: true
	}
}

// installed() is called when the device is created, and when the device is updated in the IDE.
def installed()
{	
	logger( 'installed', 'info', '' )

    // Health Check is undocumented but this seems to be the common way of creating an untracked
    // device that will appear online when the hub is up.
	sendEvent( name: 'DeviceWatch-Enroll', value: [protocol: 'cloud', scheme:'untracked'].encodeAsJson(), displayed: false )
    
    sendEvent( name: 'contact', value: 'closed',   displayed: false )
    sendEvent( name: 'motion',  value: 'inactive', displayed: false )
    sendEvent( name: 'switch',  value: 'off',      displayed: false )
}

// updated() seems to be called after installed() when the device is first installed, but not when
// it is updated in the IDE without there having been any actual changes.  It runs whenever settings
// are updated in the mobile app. It often used to be seen running twice in quick succession so was
// debounced in many handlers.
def updated()
{
	logger( 'updated', 'info', '' )

    logger( 'updated', 'debug', 'Momentary Contact Sensor ' + ( momentarycontact ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Momentary Motion Sensor '  + ( momentarymotion  ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Momentary Switch '         + ( momentaryswitch  ? 'enabled' : 'disabled' ) )
}

def logger( method, level = 'debug', message = '' )
{
	log."${level}" "$device.displayName [$device.name] [${method}] ${message}"
}

// push() is the command for the Momentary capability.
def push()
{
	logger( 'push', 'info', '' )
    
    // Change attributes to the active state.
    if ( momentarycontact ) sendEvent( name: 'contact', value: 'open'   )
    if ( momentarymotion  ) sendEvent( name: 'motion',  value: 'active' )
    if ( momentaryswitch  ) sendEvent( name: 'switch',  value: 'on'     )

	// Return attributes to inactive state.
	if ( momentarycontact ) sendEvent( name: 'contact', value: 'closed'   )
    if ( momentarymotion  ) sendEvent( name: 'motion',  value: 'inactive' )
    if ( momentaryswitch  ) sendEvent( name: 'switch',  value: 'off'      )
}

// parse() is called when the hub receives a message from a device.
def parse( String description )
{
    logger( 'parse', 'debug', description )
    
	// Nothing should appear.
}
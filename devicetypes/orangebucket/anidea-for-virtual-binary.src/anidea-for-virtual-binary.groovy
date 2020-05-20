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
 * Anidea for Virtual Binary
 * =========================
 * Version:	 20.05.15.00
 *
 * This device handler implements a virtual binary state device.
 */

metadata
{
	definition( name: 'Anidea for Virtual Binary', namespace: 'orangebucket', author: 'Graham Johnson' )
    {
        capability 'Contact Sensor'
        capability 'Motion Sensor'
        capability 'Occupancy Sensor'
        capability 'Presence Sensor'
        capability 'Switch'
        //
		capability 'Health Check'
        //
        capability 'Actuator'
		capability 'Sensor'
     
     	command 'open'
        command 'close'
        command 'active'
        command 'inactive'
        command 'occupied'
        command 'unoccupied'
        command 'arrived'
        command 'departed'
	}

	preferences
    {
        input name: 'virtualcontact',   type: 'bool', title: 'Act as virtual Contact Sensor?',   description: 'Enter boolean', required: true
        input name: 'virtualmotion',    type: 'bool', title: 'Act as virtual Motion Sensor?',    description: 'Enter boolean', required: true
        input name: 'virtualoccupancy', type: 'bool', title: 'Act as virtual Occupancy Sensor?', description: 'Enter boolean', required: true
        input name: 'virtualpresence',  type: 'bool', title: 'Act as virtual Presence Sensor?',  description: 'Enter boolean', required: true
        input name: 'virtualswitch',    type: 'bool', title: 'Act as virtual Switch?',           description: 'Enter boolean', required: true
	}
}

// installed() is called when the device is created, and when the device is updated in the IDE.
def installed()
{	
	logger( 'installed', 'info', '' )

    // Health Check is undocumented but this seems to be the common way of creating an untracked
    // device that will appear online when the hub is up.
	sendEvent( name: 'DeviceWatch-Enroll', value: [protocol: 'cloud', scheme:'untracked'].encodeAsJson(), displayed: false )
    
    sendEvent( name: 'contact',   value: 'closed',      displayed: false )
    sendEvent( name: 'motion',    value: 'inactive',    displayed: false )
    sendEvent( name: 'occupancy', value: 'unoccupied',  displayed: false )
    sendEvent( name: 'presence',  value: 'not present', displayed: false )
    sendEvent( name: 'switch',    value: 'off',         displayed: false )
}

// updated() seems to be called after installed() when the device is first installed, but not when
// it is updated in the IDE without there having been any actual changes.  It runs whenever settings
// are updated in the mobile app. It often used to be seen running twice in quick succession so was
// debounced in many handlers.
def updated()
{
	logger( 'updated', 'info', '' )

    logger( 'updated', 'debug', 'Virtual Contact Sensor '   + ( virtualcontact   ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Virtual Motion Sensor '    + ( virtualmotion    ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Virtual Occupancy Sensor ' + ( virtualoccupancy ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Virtual Presence Sensor '  + ( virtualpresence  ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Virtual Switch '           + ( virtualswitch    ? 'enabled' : 'disabled' ) )
}

def logger( method, level = 'debug', message = '' )
{
	log."${level}" "$device.displayName [$device.name] [${method}] ${message}"
}

def on()
{
	logger( 'on', 'info', '' )
    
    binaryactive()
}

def off()
{
	logger( 'off', 'info', '' )
    
    binaryinactive()
}

// parse() is called when the hub receives a message from a device.
def parse( String description )
{
    logger( 'parse', 'debug', description )
    
	// Nothing should appear.
}

def binaryactive()
{
	logger( 'binaryactive', 'info', '' )
    
    // Change attributes to the active state.
    if ( virtualcontact   ) sendEvent( name: 'contact',   value: 'open'     )
    if ( virtualmotion    ) sendEvent( name: 'motion',    value: 'active'   )
    if ( virtualoccupancy ) sendEvent( name: 'occupancy', value: 'occupied' )
    if ( virtualpresence  ) sendEvent( name: 'presence',  value: 'present'  )
    if ( virtualswitch    ) sendEvent( name: 'switch',    value: 'on'       )
}

def binaryinactive()
{
	logger( 'binaryinactive', 'info', '' )
	// Return attributes to inactive state.
	if ( virtualcontact   ) sendEvent( name: 'contact',   value: 'closed'      )
    if ( virtualmotion    ) sendEvent( name: 'motion',    value: 'inactive'    )
    if ( virtualoccupancy ) sendEvent( name: 'occupancy', value: 'unoccupied'  )
    if ( virtualpresence  ) sendEvent( name: 'presence',  value: 'not present' )
    if ( virtualswitch    ) sendEvent( name: 'switch',    value: 'off'         )
}

def open()
{
	logger( 'open', 'info', '' )
    
    binaryactive()
}

def close()
{
	logger( 'close', 'info', '' )
    
    binaryinactive()
}

def active()
{
	logger( 'active', 'info', '' )
    
    binaryactive()
}

def inactive()
{
	logger( 'inactive', 'info', '' )
    
    binaryinactive()
}

def occupied()
{
	logger( 'occupied', 'info', '' )
    
    binaryactive()
}

def unoccupied()
{
	logger( 'unoccupied', 'info', '' )
    
    binaryinactive()
}

def arrived()
{
	logger( 'arrived', 'info', '' )
    
    binaryactive()
}

def departed()
{
	logger( 'departed', 'info', '' )
    
    binaryinactive()
}
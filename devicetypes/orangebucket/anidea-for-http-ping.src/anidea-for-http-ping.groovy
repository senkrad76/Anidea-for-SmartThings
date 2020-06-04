/*
 * ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * ---------------------------------------------------------------------------------
 *
 * Anidea for HTTP Ping
 * ====================
 * Version: 20.06.04.01
 *
 * This device handler implements a number of binary sensors which are active when a
 * specified HTTP server on the hub's local network can be reached. Every fifteen 
 * minutes the status is checked and set to active if a response is received. If
 * no response is received within sixty seconds it is set to inactive.
 */

metadata
{
	definition ( name: 'Anidea for HTTP Ping', namespace: 'orangebucket', author: 'Graham Johnson' )
    {
        capability 'Contact Sensor'
        capability 'Motion Sensor'
        capability 'Occupancy Sensor'
        capability 'Presence Sensor'
        capability 'Switch'
        //
        capability 'Refresh'
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
    	input name: 'ip',      type: 'text', title: 'IP Address', description: 'e.g. 192.168.1.2', required: true
    	input name: 'port',    type: 'text', title: 'Port',       description: 'e.g. 8000',        required: true
        input name: 'urlpath', type: 'text', title: 'Path',       description: 'e.g. /some/thing', required: false

        input name: 'virtualcontact',   type: 'bool', title: 'Act as virtual Contact Sensor?',   description: 'Enter boolean', required: true
        input name: 'virtualmotion',    type: 'bool', title: 'Act as virtual Motion Sensor?',    description: 'Enter boolean', required: true
        input name: 'virtualoccupancy', type: 'bool', title: 'Act as virtual Occupancy Sensor?', description: 'Enter boolean', required: true
        input name: 'virtualpresence',  type: 'bool', title: 'Act as virtual Presence Sensor?',  description: 'Enter boolean', required: true
        input name: 'virtualswitch',    type: 'bool', title: 'Act as virtual Switch?',           description: 'Enter boolean', required: true
	}
}

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
    
    unschedule()
    runEvery15Minutes(polldevice)
}

def updated()
{
	logger( 'updated', 'info', '' )
      
    logger( 'updated', 'debug', 'Virtual Contact Sensor '   + ( virtualcontact   ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Virtual Motion Sensor '    + ( virtualmotion    ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Virtual Occupancy Sensor ' + ( virtualoccupancy ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Virtual Presence Sensor '  + ( virtualpresence  ? 'enabled' : 'disabled' ) )
    logger( 'updated', 'debug', 'Virtual Switch '           + ( virtualswitch    ? 'enabled' : 'disabled' ) )

	runIn(10, polldevice)

	runIn(1, setdni)
}

def setdni()
{
	logger( 'setdni', 'info', '' )
    
	def address = settings.ip
	def port = settings.port
	def octets = address.tokenize('.')
	def hex = ""

	octets.each
    {
		hex = hex + Integer.toHexString(it as Integer).toUpperCase().padLeft(2, '0')
	}

	hex = hex + ":" + Integer.toHexString(port as Integer).toUpperCase().padLeft(4, '0')

	if (device.getDeviceNetworkId() != hex)
    {
    	logger( 'setdni', 'debug',  "${settings.ip}:${settings.port} ${hex}" )
  	  
		device.setDeviceNetworkId(hex)
    }
    else logger( 'setdni', 'debug', '(not needed)' )
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

def refresh()
{    
    logger( 'refresh', 'info', '' )
    
    runIn(60, responsecheck)
    
    state.waitingforresponse = true
    
    return buildhubaction()
}

// If parse() is called that suggests the remote device is switched on and
// so the motion sensor is set to to active.
def parse(description)
{
    logger( 'parse', 'info', '' )
        
	def msg = parseLanMessage(description)

    state.waitingforresponse = false
    
    binaryactive()
}

// SmartThings will automatically run a HubAction returned by a command such as 
// refresh(). However if refresh() is called from the scheduler it would not be
// acting as a command. It is therefore necessary to use a separate method for 
// scheduling that calls sendHubCommand() manually.

// Wrapper for refresh() for scheduling.
def polldevice()
{    
    logger( 'polldevice', 'info', '' )
    
    sendHubCommand( refresh() )
}

def responsecheck()
{
	logger( 'responsecheck', 'info', '' )
    
    if (state.waitingforresponse)
    {   	
    	logger( 'responsecheck', 'debug', 'No response so assumed inactive' )
                
        binaryinactive()
    }
    else logger( 'responsecheck', 'debug', 'Response has been received.' )
}

def buildhubaction()
{
	logger( 'buildhubaction', 'info', '' )
    
    def hex = device.getDeviceNetworkId()

	def hubaction = new physicalgraph.device.HubAction(
        method	: 'GET',
        path	: urlpath ?: '/',
        headers	:
            [
            	"HOST": "${hex}",
      		]
	)
    
	return hubaction
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

def wet()
{
	logger( 'wet', 'info', '' )
    
    binaryactive()
}

def dry()
{
	logger( 'dry', 'info', '' )
    
    binaryinactive()
}
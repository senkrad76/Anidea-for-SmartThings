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
 * Anidea for HTTP Motion
 * ======================
 * Version: 20.04.29.00
 *
 * This device handler implements a virtual motion sensor which is active when a
 * specified HTTP server on the hub's local network can be reached. Every fifteen 
 * minutes the status is checked and set to active if a response is received. If
 * no response is received within 60 seconds it is set to inactive.
 *
 * The capabilities supported are:
 *
 *		Actuator (*)
 *		Motion Sensor
 *		Refresh
 *		Sensor (*)
 *
 * (*) The Actuator and Sensor capabilities are shown as deprecated in the Capabilities
 * references, yet their use is also encouraged as best practice. The Actuator capability
 * just means the device has commands. The Sensor capability means it has attributes.
 *
 * There are also custom commands to manually set the motion attribute, which are named
 * active() and inactive() to mimic the Simulated Motion Sensor device handler.
 *
 * Please be aware that this file is maintained in the SmartThings Groovy IDE and
 * it may format differently when viewed outside that environment.
 */

metadata
{
	definition ( name: 'Anidea for HTTP Motion', namespace: 'orangebucket', author: 'Graham Johnson' )
    {
		capability 'Actuator'
        capability 'Motion Sensor'
        capability 'Refresh'
        capability 'Sensor'
        
        command 'active'
        command 'inactive'
	}

	preferences
	{
    	input name: 'ip',   type: 'text', title: 'IP Address', description: 'e.g. 192.168.1.2', required: true
    	input name: 'port', type: 'text', title: 'Port',       description: 'e.g. 8000',        required: true
	}
}

def installed()
{
	logger( 'installed', 'info', '' )
    
	sendEvent( name: 'motion', value: 'inactive', displayed: false )
}

def updated()
{
	logger( 'updated', 'info', '' )

	// Schedule a call to polldevice() every fifteen minutes and also call it in
	// ten seconds time.
	unschedule()
    runEvery15Minutes(polldevice)
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

// If parse() is called that suggests the remote device is switched on and
// so the motion sensor is set to to active.
def parse(description)
{
    logger( 'parse', 'info', '' )
        
	def msg = parseLanMessage(description)

    state.waitingforresponse = false
    def stateevent = createEvent(name: 'motion', value: 'active')
    
    logger( 'parse', 'info', statevent )
    
    return stateevent
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

def refresh()
{    
    logger( 'refresh', 'info', '' )
    
    runIn(60, responsecheck)
    
    state.waitingforresponse = true
    
    return buildhubaction()
}

def responsecheck()
{
	logger( 'responsecheck', 'info', '' )
    
    if (state.waitingforresponse)
    {   	
    	logger( 'responsecheck', 'debug', 'No response so assumed inactive' )
                
        sendEvent(name: 'motion', value: 'inactive')
    }
    else logger( 'responsecheck', 'debug', 'Response has been received.' )
}

def buildhubaction()
{
	logger( 'buildhubaction', 'info', '' )
    
    def hex = device.getDeviceNetworkId()

	def hubaction = new physicalgraph.device.HubAction(
        method	: "GET",
        path	: "/",
        headers	:
            [
            	"HOST": "${hex}",
      		]
	)
    
	return hubaction
 }

def active()
{
	logger( 'active', 'info', '' )
    
    sendEvent( name: 'motion', value: 'active' )
}

def inactive()
{
	logger( 'inactive', 'info', '' )
    
    sendEvent( name: 'motion', value: 'inactive' )
}
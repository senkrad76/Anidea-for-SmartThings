/*
 * ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby granted, provided that the above copyright notice
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
 * AutoRemote WiFi Switch
 * ======================
 * A SmartThings Device Handler for a virtual switch that connects to devices
 * running the AutoRemote WiFi Service on the local LAN.
 *
 * Author:				Graham Johnson (orangebucket)
 *
 * Version:				1.2		(05/06/2018)
 *
 * Comments:			Need to look at what parameters from the AutoRemote
 *						Send Message Service also apply to WiFi.
 *						The state change event is only triggered when a response to the 
 *                      request has been received. This doesn't mean it has 'worked',
 *						only that the remote device has received the request.
 *
 * Changes:				1.2		(05/06/2018)	State change events handled in parse method.
 *						1.1		(03/06/2018)	Now do it somewhat more competently.
 *						1.0 	(02/06/2018)	Initial release.
 *
 * Please be aware that this file is created in the SmartThings Groovy IDE and it may
 * format differently when viewed outside that environment.
 */

preferences
{
		input "message_on",     "text", 	title: "Message for 'On'", 						required: true,
        description: "AutoRemote WiFi message"
		input "message_off",    "text", 	title: "Message for 'Off'", 					required: true,
        description: "AutoRemote WiFi message"
        // input "target",     	"text", 	title: "AutoRemote Target (Optional)",			required: false
        // input "sender",     	"text", 	title: "AutoRemote Sender (Optional)",			required: false
		// input "password",     	"password", title: "AutoRemote Password (Optional)",		required: false
		// input "ttl",     		"text", 	title: "AutoRemote Validity time (Optional)",	required: false
		// input "collapsekey",    "text", 	title: "AutoRemote Message Group (Optional)",	required: false
        // input "showdebug",		"bool",		title: "Debug Messages",						required: true
}

metadata
{
	definition (name: "AutoRemote WiFi Switch", namespace: "orangebucket", author: "Graham Johnson")
    {
		capability "Actuator"
		capability "Switch"
	}

	// Simulator.
	simulator
    {
	}

	// UI.
	tiles
    {
		standardTile("status",   "device.switch", width: 2, height: 2, canChangeIcon: true)
        {
			state "off",     label: "Off", action: "switch.on",  icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turnon"
			state "on",      label: "On",  action: "switch.off", icon: "st.switches.switch.on",  backgroundColor: "#00a0dc", nextState: "turnoff"
            
			state "turnoff", label: "Turning Off", icon: "st.switches.switch.off", backgroundColor: "#ff8000"
            state "turnon",  label: "Turning On",  icon: "st.switches.switch.on",  backgroundColor: "#ff8000"
        }
        
		standardTile("swon",  	"device.switch", width: 1, height: 1)
        {
			state "off", label: 'On',  action: "switch.on",  icon: "st.switches.switch.on",  backgroundColor: "#008000",
            defaultState: true
		}
        
		standardTile("swoff",	"device.switch", width: 1, height: 1)
        {
			state "on", label: 'Off', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#ff0000",
            defaultState: true
		}
        
		main "status"
        details (["status","swon","swoff"])
	}
}

def parse(description)
{
	def msg = parseLanMessage(description)

	// There should be a record of the request in the state map.
    if ( state[msg.requestId] )
    {
    	def stateevent = createEvent(name: "switch", value: state[msg.requestId])
        
        // This entry in the map is no longer required.
        state.remove(msg.requestId)
        
        // Let ST fire off the event.
        return stateevent
    } 
}

def buildhubaction(commstate, message)
{
	// In order for the hub to send responses to the 'parse()' method it seems the
    // device network ID needs to be either the MAC address or the IP address and
    // port in hex pair notation. It doesn't seem to be possible to override it
    // programmatically so it might as well be set once via the IDE rather than via
    // parameters.
	def hex = device.getDeviceNetworkId()
	
	def hubaction = new physicalgraph.device.HubAction(
        method	: "GET",
        path	: "/sendmessage",
 		query	:	[ "message": "${message}" ],          	
        headers	:
            [
            	"HOST": "${hex}",
      		]
	)
    
    // Save the state change associated with this request.
    state[hubaction.requestId] = commstate
    
    return hubaction
}

def on()
{
    // ST will run the HubAction for us.
    return buildhubaction('on', settings.message_on)
}

def off()
{
	// ST will run the HubAction for us.
    return buildhubaction('off', settings.message_off)
}
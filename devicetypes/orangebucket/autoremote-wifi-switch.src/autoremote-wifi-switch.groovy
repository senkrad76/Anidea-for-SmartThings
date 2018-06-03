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
 * Version:				1.1		(03/06/2018)
 *
 * Comments:			Need to look at what parameters from the AutoRemote
 *						Send Message Service also apply to WiFi.
 *
 * Changes:				1.1		(03/06/2018)	Now do it somewhat more competently.
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
			state "off", label: 'Off', action: "switch.on",  icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on",  label: 'On',  action: "switch.off", icon: "st.switches.switch.on",  backgroundColor: "#00a0dc"
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

    log.debug msg.headers
    log.debug msg.body
}

def buildhubaction(String message)
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
	);

    return hubaction
}

def on()
{
    // Tell ST the switch is on.
    sendEvent(name: "switch", value: "on")
    
    // ST will run the HubAction for us.
	return buildhubaction(settings.message_on)   
}

def off()
{
    // Tell ST the switch is off.
    sendEvent(name: "switch", value: "off")

	// ST will run the HubAction for us.
	return buildhubaction(settings.message_off)   
}
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
 * AutoRemote WiFi Thing
 * =====================
 * A SmartThings Device Handler for various 'things' that connects to devices
 * running the AutoRemote WiFi Service on the local LAN. Devices supported are:
 *
 *		Alarm
 *		Notification
 *		Switch
 *		Tone
 *
 * Author:				Graham Johnson (orangebucket)
 *
 * Version:				1.3		(01/10/2018)
 *
 * Comments:			Need to look at what parameters from the AutoRemote
 *						Send Message Service also apply to WiFi.
 *						The state change event is only triggered when a response to the 
 *                      request has been received. This doesn't mean it has 'worked',
 *						only that the remote device has received the request.
 *
 * Changes:				1.3		(01/10/2018)	Drop on/off message configuration and use
 *												AutoApps command format. Add Alarm, Tone and
 *												Notification capabilities.
 *						1.2		(05/06/2018)	State change events handled in parse method.
 *						1.1		(03/06/2018)	Now do it somewhat more competently.
 *						1.0 	(02/06/2018)	Initial release.
 *
 * Please be aware that this file is created in the SmartThings Groovy IDE and it may
 * format differently when viewed outside that environment.
 */

preferences
{
		// input "message_on",     "text", 	title: "Message for 'On'", 						required: true,
        // description: "AutoRemote WiFi message"
		// input "message_off",    "text", 	title: "Message for 'Off'", 					required: true,
        // description: "AutoRemote WiFi message"
        // input "target",     	"text", 	title: "AutoRemote Target (Optional)",			required: false
        // input "sender",     	"text", 	title: "AutoRemote Sender (Optional)",			required: false
		// input "password",     	"password", title: "AutoRemote Password (Optional)",		required: false
		// input "ttl",     		"text", 	title: "AutoRemote Validity time (Optional)",	required: false
		// input "collapsekey",    "text", 	title: "AutoRemote Message Group (Optional)",	required: false
        // input "showdebug",		"bool",		title: "Debug Messages",						required: true
}

metadata
{
	definition (name: "AutoRemote WiFi Thing", namespace: "orangebucket", author: "Graham Johnson")
    {
		capability "Actuator"
		capability "Switch"
        capability "Alarm"
        capability "Notification"
        capability "Tone"
	}
    
    // Specific off commands for alarm and switch as they both have an 'off' command.
    command "alarmoff"
    command "switchoff"

	// Simulator.
	simulator
    {
	}

	// UI.
	tiles
    {
        standardTile("alarm", 			"device.alarm", width: 1, height: 1) 
        {
            state "off", label:'Off', action:'alarm.siren', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
            state "siren", label: 'Siren', action:'alarmoff', icon:"st.alarm.alarm.alarm", backgroundColor:"#00a0dc"
        }

        standardTile("notification",	"device.notification", width: 1, height: 1)
        {
            state "default", label:'Notify', action:"notification.deviceNotification", icon:"st.Kids.kids1"
        }
        
		// Switch showing intermediate states while awaiting device response.
		standardTile("switch",   		"device.switch", width: 1, height: 1, canChangeIcon: true)
        {
			state "off",     label: "Off", action: "switch.on",  icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turnon"
			state "on",      label: "On",  action: "switchoff", icon: "st.switches.switch.on",  backgroundColor: "#00a0dc", nextState: "turnoff"
            
			state "turnoff", label: "Turning Off", icon: "st.switches.switch.off", backgroundColor: "#ff8000"
            state "turnon",  label: "Turning On",  icon: "st.switches.switch.on",  backgroundColor: "#ff8000"
        }
        
        // Try to force switch on.
		standardTile("swon",  			"device.switch", width: 1, height: 1)
        {
			state "off", label: 'On',  action: "switch.on",  icon: "st.switches.switch.on",  backgroundColor: "#008000",
            defaultState: true
		}
        
        // Try to force switch off.
		standardTile("swoff",			"device.switch", width: 1, height: 1)
        {
			state "on", label: 'Off', action: "switchoff", icon: "st.switches.switch.off", backgroundColor: "#ff0000",
            defaultState: true
		}
        
        standardTile("tone", 			"device.tone", width: 1, height: 1)
        {
            state "default", label:'Tone', action:"tone.beep", icon:"st.alarm.beep.beep"
        }
        
		main "alarm"
        details (["alarm", "notification", "switch", "swon", "swoff", "tone"])
	}
}

def parse(description)
{
	def msg = parseLanMessage(description)

	// There should be a record of any state change requests in the state map.
    if ( state[msg.requestId] )
    {
    	def st = state[msg.requestId].split('=:=')
        def stcap = st[0]
        def stval = st[1]
        
        log.debug stcap + " " + stval
    	
    	def stateevent = createEvent(name: stcap, value: stval)
        
        // This entry in the map is no longer required.
        state.remove(msg.requestId)
        
        // Let ST fire off the event.
        return stateevent
    } 
}

def buildhubaction(thing, thingcommand, commandstate)
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
 		query	:	[ "message": "autoremotewifithing=:=${thing}=:=${thingcommand}" ],          	
        headers	:
            [
            	"HOST": "${hex}",
      		]
	)
    
    // Save any state change associated with this request.
    if (commandstate) state[hubaction.requestId] = "${thing}=:=${thingcommand}"
    
    return hubaction
}

def siren()
{
	return buildhubaction('alarm', 'siren', true)
}

// Custom command to turn alarm off.
def alarmoff()
{
    // ST will run the HubAction for us.
    return buildhubaction('alarm', 'off', true)
}

def deviceNotification(notificationtext)
{
    if (!notificationtext?.trim()) notificationtext = "AutoRemote WiFi Thing"
   
	// ST will run the HubAction for us.
    return buildhubaction('notification', URLEncoder.encode(notificationtext, 'UTF-8'), false)
}

def on()
{
    // ST will run the HubAction for us.
    return buildhubaction('switch', 'on', true)
}

def off()
{   
	// This command can be called for the alarm or the switch.
    
    // Default is the switch.
    def thing = "switch"

	// If the alarm is activated turn it off.
    if (device.currentValue('alarm') != "off") thing = "alarm"
    
    // ST will run the HubAction for us.
    return buildhubaction(thing, 'off', true)
}

// Custom command to turn switch off.
def swoff()
{
    // ST will run the HubAction for us.
    return buildhubaction('switch', 'off', true)
}

def beep()
{
	// ST will run the HubAction for us.
    return buildhubaction('tone', 'beep', false)
}

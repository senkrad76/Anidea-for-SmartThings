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
 * HTTP Response Motion Sensor
 * ===========================
 * This device handler implements a virtual motion sensor which is active when a
 * specified HTTP server on the hub's local network can be reached. Every fifteen 
 * minutes the status is reset to inactive and then checked again so it should not
 * be used for automations that trigger immediately when a motion sensor changes to inactive.
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
 * Author:				Graham Johnson (orangebucket)
 *
 * Version:				0.1		(07/10/2018)
 *
 * Comments:			
 *
 * Changes:				0.1		(07/10/2018)
 *
 * Please be aware that this file is created in the SmartThings Groovy IDE and it may
 * format differently when viewed outside that environment.
 */

preferences
{
}

metadata
{
	definition (name: "HTTP Response Motion Sensor", namespace: "orangebucket", author: "Graham Johnson")
    {
		capability "Actuator"
        capability "Motion Sensor"
        capability "Refresh"
        capability "Sensor"
	}
    
	// One day I will investigate this.
	simulator
    {
	}

	// UI.
    //
    // White (#ffffff) is the standard for 'off'-like states.
    // Blue (#00a0dc) is the standard for 'on'-like states.
    // Orange (#e86d13) is the standard for devices requiring attention.
    // Grey (#cccccc) is the standard for inactive or offline devices.
    //
	tiles
    {
		standardTile("motion", "device.motion", width: 2, height: 2)
        {
			state "active", label:'active', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
			state "inactive", label:'inactive', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        }
        
 		standardTile("refresh", "device.motion", width: 1, height: 1)
        {
        	state "default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
		}
              
        main "motion"
        details (["motion", "refresh"])
	}
}

def installed()
{
	updated()
}

def updated()
{
	unschedule()
	runEvery15Minutes(polldevice)
	runIn(2, polldevice)
}

def parse(description)
{
	def msg = parseLanMessage(description)

    log.debug "${device}: parse"
        
    def stateevent = createEvent(name: 'motion', value: 'active')
        
    return stateevent
}

// SmartThings will automatically send a HubAction returned by a command. However
// refresh() isn't actually a command when run automagically every fifteen minutes.
// So simply wrap it in a method and do the sendHubCommand manually.
def polldevice()
{    
    log.debug "${device}: polldevice"
    
    sendHubCommand(refresh())
}

def refresh()
{    
    log.debug "${device}: refresh"
    
    return buildhubaction()
}

def buildhubaction()
{
	sendEvent(name: 'motion', value: 'inactive')

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

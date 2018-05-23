/*
 * AutoRemote Switch
 * =================
 * Virtual switch device handle that sends AutoRemote messages for 'on' and 'off' states.
 *
 * Author:				Graham Johnson
 * GitHub:				OrangeBucket
 * Namespace:			orangebucket
 *
 * Version:				1.0 (22/05/2018)
 *
 * Future plans:		Add notifications.	
 *
 * Changes:				1.0 (23/05/2018)		Initial release.
  */

preferences
{
		input "autoremote_key", "text", 	title: "AutoRemote Key",						required: true
		input "message_on",     "text", 	title: "Message for 'On'",  					required: true
		input "message_off",    "text", 	title: "Message for 'Off'", 					required: true
        input "target",     	"text", 	title: "Target (Optional)",						required: false
        input "sender",     	"text", 	title: "Sender (Optional)",						required: false
		input "password",     	"password", title: "Password (Optional)",					required: false
		input "ttl",     		"text", 	title: "Valdity time in seconds (Optional)",	required: false
		input "collapsekey",    "text", 	title: "Message Group (Optional)",				required: false
}

metadata
{
	definition (name: "AutoRemote Switch", namespace: "orangebucket", author: "Graham Johnson")
    {
		capability "Actuator"
		capability "Switch"
	}

	// Simulator
	simulator
    {
	}

	// UI
	tiles
    {
		standardTile("status",    "device.switch", width: 2, height: 2, canChangeIcon: true)
        {
			state "off", label: 'Off', action: "switch.on",  icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on",  label: 'On',  action: "switch.off", icon: "st.switches.switch.on",  backgroundColor: "#00a0dc"
		}
        
		standardTile("swon",  "device.switch", width: 1, height: 1, canChangeIcon: true)
        {
			state "off", label: 'On',  action: "switch.on",  icon: "st.switches.switch.on",  backgroundColor: "#008000",
            defaultState: true
		}
        
		standardTile("swoff", "device.switch", width: 1, height: 1, canChangeIcon: true)
        {
			state "on", label: 'Off', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#ff0000",
            defaultState: true
		}
        
		main "status"
        details (["status","swon","swoff"])
	}
}

def parse(String description)
{
	// log.debug(description)
}

def String url(String message)
{
	def url = "https://autoremotejoaomgcd.appspot.com/sendmessage?key="+"${settings.autoremote_key}"+"&message="+"${message}"
    
    if (settings.target != null && settings.target != "")
    {
    	url = url+"&target="+"${settings.target}"
    }
    
    if (settings.sender != null && settings.sender != "")
    {
    	url = url+"&sender="+"${settings.sender}"
    }
        
    if (settings.password != null && settings.password != "")
    {
    	url = url+"&password="+"${settings.password}"
    }
        
    if (settings.ttl != null && settings.ttl != "")
    {
    	url = url+"&ttl="+"${settings.ttl}"
    }
        
    if (settings.collapsekey != null && settings.collapsekey != "")
    {
    	url = url+"&collapsekey="+"${settings.collapsekey}"
    }
        
	return url
}

def on()
{
	def geturl = url(settings.message_on)
    
	// log.debug "URI: ${geturl}"

    // Tell ST the switch is on
    sendEvent(name: "switch", value: "on")
    
	try
    {
    	httpGet(geturl)
    } 
    catch(e)
    {
    	log.error "Error: $e"
	}
}

def off() {
	def geturl = url(settings.message_off)

	// log.debug "URI: ${geturl}"

    // Tell ST the switch is off
    sendEvent(name: "switch", value: "off")

    try
    {
    	httpGet(geturl)
    }
    catch(e)
    {
    	log.error "Error: $e"
	}
}
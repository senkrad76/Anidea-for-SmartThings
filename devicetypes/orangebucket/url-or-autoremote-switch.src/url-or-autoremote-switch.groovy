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
 * URL and AutoRemote Switch
 * =========================
 * A SmartThings Device Handler for a virtual switch that connects to URLs on 
 * entering the 'on' and 'off' states. The URLs supplied should be full URLs with
 * all necessary encoding, unless an AutoRemote Key is supplied in which case the
 * supplied 'on' and 'off' strings are URLencoded and used as the 'message' part
 * of an AutoRemote Message. The other options available on the AutoRemote 'personal
 * URL' are also available. Notifications are not supported.
 *
 * Author:				Graham Johnson (orangebucket)
 *
 * Version:				2.1 (31/05/2018)
 *
 * Comments:			There are no particular plans for enhancements. Thought was
 *						given to handling AutoRemote Notifications but there are an 
 *						awful lot of parameters to handle. There was an experiment
 *						with the canChangeBackground parameter enabled on the main 
 *						tile but it was found to interfere with canChangeIcon.
 *
 * Changes:				2.1 (31/05/2018)	Removed redundant canChangeIcon parameters.
 						2.0 (26/05/2018)	Support full URLs as an alternative.
 *						1.2 (25/05/2018)	Tidy up.
 *						1.1 (23/05/2018)	Cosmetic changes to source code.
 *						1.0 (23/05/2018)	Initial release.
 *
 * Please be aware that this file is created in the SmartThings Groovy IDE and it may
 * format differently when viewed outside that environment.
 */

preferences
{
		input "message_on",     "text", 	title: "URL or Message for 'On'", 				required: true,
        description: "Full URL or AutoRemote message"
		input "message_off",    "text", 	title: "URL or Message for 'Off'", 				required: true,
        description: "Full URL or AutoRemote message"
		input "autoremote_key", "text", 	title: "AutoRemote Key (Optional)",				required: false,
        description: "AutoRemote key, or leave empty"
        input "target",     	"text", 	title: "AutoRemote Target (Optional)",			required: false
        input "sender",     	"text", 	title: "AutoRemote Sender (Optional)",			required: false
		input "password",     	"password", title: "AutoRemote Password (Optional)",		required: false
		input "ttl",     		"text", 	title: "AutoRemote Validity time (Optional)",	required: false
		input "collapsekey",    "text", 	title: "AutoRemote Message Group (Optional)",	required: false
        input "showdebug",		"bool",		title: "Debug Messages",						required: true
}

metadata
{
	definition (name: "URL or AutoRemote Switch", namespace: "orangebucket", author: "Graham Johnson")
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

def parse(String message)
{
	// No messages are expected from the device because there isn't one.
}

def String buildurl(String message)
{
    if (!settings.autoremote_key) return message
      
    def url = "https://autoremotejoaomgcd.appspot.com/sendmessage?key=${settings.autoremote_key}&message=" + URLEncoder.encode(message, 'UTF-8')
   
    if (settings.target) 
    {
    	url = url + "&target=" + URLEncoder.encode("${settings.target}", 'UTF-8')
    }
    
    if (settings.sender)
    {
    	url = url + "&sender=" + URLEncoder.encode("${settings.sender}", 'UTF-8')
    }
        
    if (settings.password)
    {
    	url = url + "&password=" + URLEncoder.encode("${settings.password}", 'UTF-8')
    }
        
    if (settings.ttl)
    {
    	url = url + "&ttl=" + URLEncoder.encode("${settings.ttl}", 'UTF-8')
    }
        
    if (settings.collapsekey)
    {
    	url = url + "&collapsekey=" + URLEncoder.encode("${settings.collapsekey}", 'UTF-8')
    }
        
	return url
}

def on()
{
	def geturl = buildurl(settings.message_on)
    
	if (settings.showdebug) log.debug "URL: ${geturl}"

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

def off()
{
	def geturl = buildurl(settings.message_off)

	if (settings.showdebug) log.debug "URL: ${geturl}"

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
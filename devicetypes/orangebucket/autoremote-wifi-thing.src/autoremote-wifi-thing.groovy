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
 * AutoRemote WiFi Thing
 * =====================
 * A SmartThings Device Handler that connects to devices running the AutoRemote WiFi
 * Service on the local LAN and issues commands in the AutoApps command format. The
 * commands are just URLs so the remote devices can do what they like in response.
 *
 * The capabilities supported are:
 *
 *		Actuator (*)
 *		Alarm
 *		Notification
 * 		Sensor (*)
 *		Speech Synthesis
 *		Switch
 *		Tone
 *
 * (*) The Actuator and Sensor capabilities are shown as deprecated in the Capabilities
 * references, yet their use is also encouraged as best practice. The Actuator capability
 * just means the device has commands. The Sensor capability means it has attributes.
 *
 * Author:				Graham Johnson (orangebucket)
 *
 * Version:				2.0		(08/10/2018)
 *
 * Comments:			Need to look at what parameters from the AutoRemote
 *						Send Message Service also apply to WiFi.
 *						A state change event is only triggered when a response to the 
 *                      request has been received. This doesn't mean it has 'worked',
 *						only that the remote device has received the request. However it
 *						is a lot better than simply sending state change events in the
 *						commands.
 commands.
 *
 * Changes:				2.0		(08/10/2018)	Read IP and Port in preferences and set the
 *												DNI dynamically.
 *						1.9		(04/10/2018)	Experimenting with various things. All
 *												changes removed.
 *						1.8		(04/10/2018)	Correct the ID for Speech Synthesis. Change
 *												buildhubaction() parameters to make parsing
 *												commands easier.
 *						1.7		(03/10/2018)	Make command structure more regular.
 *						1.6		(02/10/2018)	Handle command in speech text.
 *						1.5		(02/10/2018)	Continuing tidy up as part of learning curve.
 *												Add more intermediate states.
 *						1.4		(01/10/2018)	Add Speech Synthesis. General tidy up and
 *												improvement of code.
 *						1.3		(01/10/2018)	Drop on/off message configuration and use
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
   	input name: "ip", type: "text", title: "IP Address", description: "e.g. 192.168.1.2", required: true
    input name: "port", type: "text", title: "Port", description: "e.g. 8000", required: true

	// These preferences were used by the cloud based AutoRemote messaging in a different DTH.
    // The password might be useful if it is honoured, the others probably aren't.
    
    // input "target", "text", title: "AutoRemote Target (Optional)", required: false
	// input "sender", "text", title: "AutoRemote Sender (Optional)", required: false
 	// input "password", "password", title: "AutoRemote Password (Optional)", required: false
	// input "ttl", "text", title: "AutoRemote Validity time (Optional)", required: false
	// input "collapsekey", "text", title: "AutoRemote Message Group (Optional)", required: false
    // input "showdebug", "bool", title: "Debug Messages", required: true
}

metadata
{
	definition (name: "AutoRemote WiFi Thing", namespace: "orangebucket", author: "Graham Johnson")
    {
		capability "Actuator"
        capability "Alarm"
        capability "Notification"
        capability "Sensor"
        capability "Speech Synthesis"
		capability "Switch"
        capability "Tone"
	}
    
    // Specific off commands for alarm and switch as they both have an 'off' command.
    command "alarmoff"
    command "switchoff"

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
    // Orange is used for the alarm 'Siren', 'Strobe' and 'Both' states as that is what is meant by
    // a device requiring attention.
    //
    // The standards suggest that transitional states should use the colour for the transitioned to state.
    // This seems such a baffling choice that it has been ignored. There also doesn't seem to be any conventions
    // for tiles used for test and reset purposes.
    //
    // Yellow (#c0c000) is being used for transitional states to highlight possible technical issues.
    // Purple (#800080) is being used for test tiles for notification, speech and tone.
    // Red (#ff0000) is being used by tiles that reset the alarm and switch to off.
    //
	tiles
    {
    	// Start with a tile for the alarm status of the device. Either turns both alarms on, or turns both off.
        // Transitional states are shown while waiting for a response from the remote device.
        standardTile("alarm", "device.alarm", width: 2, height: 2, canChangeIcon: true) 
        {
            state "off", label:'Off', action:'alarm.both', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff", nextState: "bothon"         
            state "bothon", label: '-> Both', icon: "st.alarm.alarm.alarm", backgroundColor: "#c0c000"
            state "both", label: 'Both', action:'alarmoff', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13", nextState: "bothoff"
            state "siren", label: 'Siren', action:'alarmoff', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13", nextState: "bothoff"
            state "strobe", label: 'Strobe', action:'alarmoff', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13", nextState: "bothoff"              
            state "bothoff", label: '-> Off', icon: "st.alarm.alarm.alarm", backgroundColor: "#c0c000"
        }
         
        // This tile either adds the siren alarm, or turns both alarms off.
        // Transitional states are shown while waiting for a response from the remote device.
        standardTile("siren", "device.alarm", width: 1, height: 1) 
        {
            state "off", label:'Off', action:'alarm.siren', icon:"st.Electronics.electronics13", backgroundColor:"#ffffff", nextState: "sirenon"
          	state "strobe", label:'Off', action:'alarm.both', icon:"st.Electronics.electronics13", backgroundColor:"#ffffff", nextState: "bothon"
            state "sirenon", label: '-> Siren', icon:"st.Electronics.electronics13", backgroundColor:"#c0c000"
            state "bothon", label: '-> Both', icon:"st.Electronics.electronics13", backgroundColor:"#c0c000"
            state "siren", label: 'Siren', action:'alarmoff', icon:"st.Electronics.electronics13", backgroundColor:"#e86d13", nextState: "bothoff"
            state "both", label: 'Both', action:'alarmoff', icon:"st.Electronics.electronics13", backgroundColor:"#e86d13", nextState: "bothoff"
            state "bothoff", label: '-> Off', icon:"st.Electronics.electronics13", backgroundColor:"#c0c000"
      	}        
        
        // This tile either adds the strobe alarm, or turns both alarms off.  
        // Transitional states are shown while waiting for a response from the remote device.
        standardTile("strobe", "device.alarm", width: 1, height: 1) 
        {
            state "off", label:'Off', action:'alarm.strobe', icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState: "strobeon"
            state "siren", label:'Siren', action:'alarm.both', icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState: "bothon"
            state "strobeon", label: '-> Siren', icon:"st.Lighting.light13", backgroundColor:"#c0c000"
            state "bothon", label: '-> Both', icon:"st.Lighting.light13", backgroundColor:"#c0c000"
            state "strobe", label: 'Strobe', action:'alarmoff', icon:"st.Lighting.light13", backgroundColor:"#e86d13", nextState: "bothoff"
            state "both", label: 'Both', action:'alarmoff', icon:"st.Lighting.light13", backgroundColor:"#e86d13", nextState: "bothoff"
            state "bothoff", label: '-> Off', icon:"st.Lighting.light13", backgroundColor:"#c0c000"
        }

        // This tile will reset the alarm to off regardless of current status.
		standardTile("alarmreset", "device.alarm", width: 1, height: 1)
        {
			state "reset", label: 'Reset', action: "alarmoff", icon: "st.alarm.alarm.alarm", backgroundColor: "#ff0000", defaultState: true
		}
        
        // This tile sends a sample notification.
        standardTile("notification", "device.notification", width: 1, height: 1)
        {
            state "notify", label:'Notify', action:"notification.deviceNotification", icon:"st.Office.office13", backgroundColor:"#800080", defaultState: true
        }
        
        // This tile sends a sample text to be spoken.
        standardTile("speechSynthesis", "device.speech", width: 1, height: 1)
        {
            state "speak", label:'Speak', action:'Speech Synthesis.speak', icon:"st.Entertainment.entertainment3", backgroundColor:"#800080", defaultState: true
        }
        
		// Switch showing transitional states while awaiting device response.
		standardTile("switch", "device.switch", width: 1, height: 1)
        {
			state "off", label: "Off", action: "switch.on",  icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turnon"        
            state "turnon", label: "-> On", icon: "st.switches.switch.on", backgroundColor: "#c0c000"
			state "on", label: "On", action: "switchoff", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turnoff"            
			state "turnoff", label: "-> Off", icon: "st.switches.switch.off", backgroundColor: "#c0c000"
        }
                
        // This tile will reset the switch to off regardless of current status.
		standardTile("switchreset", "device.switch", width: 1, height: 1)
        {
			state "reset", label: 'Reset', action: "switchoff", icon: "st.switches.switch.off", backgroundColor: "#ff0000", defaultState: true
		}
        
        standardTile("tone", "device.tone", width: 1, height: 1)
        {
            state "tone", label:'Tone', action:"tone.beep", icon:"st.alarm.beep.beep", backgroundColor: "#800080", defaultState: true
        }
        
        // Use the alarm as the main tile.
        main "alarm"
        // Sort the tiles suitably.
        details (["alarm", "siren", "strobe", "notification", "speechSynthesis", "tone", "switch", "alarmreset", "switchreset"])
	}
}

def installed()
{
	log.debug "${device}: installed"
    
    updated()
}

def updated()
{
	log.debug "${device}: updated"
 
	unschedule()
 
	runIn(2, setdni)
}

// In order for the hub to send responses to the 'parse()' method in a DTH it seems
// the device network ID needs to be either the MAC address, or the IP address and
// port in hex notation. Generally the MAC address is encouraged as IP addresses
// can change.
//
// Incoming messages generally wouldn't, and often can't, have the same source port
// number as the destination port used in outgoing messages. Therefore the MAC address
// would be required to allow incoming messages. Brief testing suggested this made 
// receiving responses at best slow and at worst unreliable. This was quite possibly
// due to flawed coding, or indeed flawed testing, but it hasn't been pursued.
//
// As it works and it saves entering or finding the MAC address, the hex IP port and
// address are used.
def setdni()
{
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
    	log.debug "${device}: setdni ${address}:${port} ${hex}"
  	  
		device.setDeviceNetworkId(hex)
    }
    else log.debug "${device}: setdni (not needed)"
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
          	
    	def stateevent = createEvent(name: stcap, value: stval)
        
        // This entry in the map is no longer required.
        state.remove(msg.requestId)
        
		log.debug "${device}: parse"

		// Let ST fire off the event.
        return stateevent
    }
    else log.debug "${device}: parse (unknown request)"
}

// Build and return a hubaction.
//
// cap			Capability id.
// capcomm		Command or empty string.
// capfree 		Free text or empty string.
// commandstate	True if command should trigger a state change.
def buildhubaction(cap, capcomm, capfree, commandstate = false)
{    
	// The DNI is IP:port in hex form.
	def hex = device.getDeviceNetworkId()
    
    // The capfree parameter may have a command on the front. Extract it if so.
    def tempcap = capfree.split('=:=')
    if (tempcap.length > 1)
    {
    	// Don't allow capcomm or capfree to end up empty.
    	capcomm = tempcap[0] ?: capcomm
    	capfree = tempcap[1] ?: capcomm
    }
    
    // Don't allow capfree to be empty.
    if (!capfree) capfree = capcomm
    
    // URL encoding is probably a bit redundant and AutoRemote doesn't seem to do any
    // decoding so it would break things if the whole query string was encoded. So
    // just encode the variable that might have relatively free text in it.
    def enccapfree = URLEncoder.encode(capfree, 'UTF-8');
	
	def hubaction = new physicalgraph.device.HubAction(
        method	: "GET",
        path	: "/sendmessage",
 		query	:	[ "message": "autoremotewifithing=:=${cap}=:=${capcomm}=:=${enccapfree}" ],          	
        headers	:
            [
            	"HOST": "${hex}",
      		]
	)
    
    // Save any state change associated with this request.
    if (commandstate) state[hubaction.requestId] = "${cap}=:=${capcomm}"

	return hubaction
}

def both()
{
	return buildhubaction('alarm', 'both', '', true)
}

def siren()
{
	return buildhubaction('alarm', 'siren', '', true)
}

def strobe()
{
	return buildhubaction('alarm', 'strobe', '', true)
}

// Custom command to turn alarm off.
def alarmoff()
{
    // ST will run the HubAction for us.
    return buildhubaction('alarm', 'off', '', true)
}

def deviceNotification(notificationtext)
{
    if (!notificationtext?.trim()) notificationtext = "AutoRemote WiFi Thing"
   
	// ST will run the HubAction for us.
    return buildhubaction('notification', 'deviceNotification', notificationtext, false)
}

def speak(words)
{
    if (!words?.trim()) words = "AutoRemote WiFi Thing"
   
	// ST will run the HubAction for us.
    return buildhubaction('speechSynthesis', 'speak', words, false)
}

def on()
{
    // ST will run the HubAction for us.
    return buildhubaction('switch', 'on', '', true)
}

def off()
{   
	// This command can be called for the alarm or the switch.
    
    // Default is the switch.
    def cap = "switch"

	// If the alarm is activated turn it off.
    if (device.currentValue('alarm') != "off") cap = "alarm"
    
    // ST will run the HubAction for us.
    return buildhubaction(cap, 'off', '', true)
}

// Custom command to turn switch off.
def switchoff()
{
    // ST will run the HubAction for us.
    return buildhubaction('switch', 'off', '', true)
}

def beep()
{
	// ST will run the HubAction for us.
    return buildhubaction('tone', 'beep', '', false)
}

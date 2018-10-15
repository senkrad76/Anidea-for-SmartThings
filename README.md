# Anidea-SmartThings &copy; Graham Johnson (orangebucket)
Assorted SmartThings bits and bobs.

## LAN MultiThing
This device handler implements the capabilities Alarm, Audio Notification (*), Configuration, Speech Synthesis, Switch and Tone by sending messages as HTTP GET messages in a format compatible with the AutoRemote WiFi Service and using AutoApps command format. There really is nothing magical about this and you can do absolutely anything you want with the commands at the other end. The author uses it to implement a replacement for LANnouncer using the AutoRemote WiFi Service to provide an HTTP server for Tasker, and then Tasker to act on the commands.

The device handler is also capable of receiving 'pings' from the remote device sent as HTTP Post requests to port 39500 of the hub in JSON format. Currently these can be used to set the attribute states for the Air Quality Sensor, Battery, Estimated Time Of Arrival, Relative Humidity, Temperature and Ultraviolet Index capabilities, and also to set other variables in the device state map.

The device handler can also act as a bridge for individual devices supported by the remote device. There are currently child device handlers for 'Audio' (Audio Notification capability) and 'ETA' (Estimated Time Of Arrival Capability). The child devices send commands by calling methods on the parent, while the parent will forward incoming messages addressed to the child to the child's parse() method.

*It is possible to query the commands and attributes supported by capabilities on the fly. The commands reported for Audio Notification are consistent with the reference documentation. However the Speaker Companion app (previously Speaker Notify With Sound) uses commands which are not part of the Audio Notification or the now deprecated Music Player capabilities, or indeed any at all. They are device specific commands, which is utterly ridiculous. The two commands which have the same names will accept, and ignore, the extra parameter they may be called with by Speaker Companion. The other command is not supported at the moment.*

The device is specified by IP Address and Port in the Preferences, and the MAC address may also be specified (with or without colons and in upper, lower or mixed case). If the MAC address is provided it will be used as the Device Network ID (DNI), otherwise the IP Address and Port are combined in a hex form as the DNI. You might prefer to give your AutoRemote device a fixed IP address using a manual IP or a reserved IP address in your DHCP server. If the MAC address is not provided the incoming 'pings' will not work.

*The device network ID has to be either the MAC address or the hex IP:Port in order for SmartThings to send responses to the parse() method of a device handler. The MAC address is generally preferred and allows the device handler to receive out of band requests from the remote device on port 39500 on the hub. With the hex IP:Port this doesn't work as the remote source port wouldn't be 1817 for these requests. Unfortunately if you have one device set up with the MAC address and another with the hex IP:Port the latter will not see the responses to its own requests. It would be nice if messages were forwarded based on IP:Port first and then MAC address but that isn't how it works.*

For capabilities that have a state, such as Alarm and Switch, the device handler waits for a response from the server on the device before setting the new state. This doesn't mean the command has worked, only that AutoRemote WiFi service has received it.

The HTTP GET requests are of the form <code>http://&lt;IP address on local LAN&gt;:&lt;Port&gt;/sendmessage?message=&lt;message&gt;</code> where the &lt;message&gt; is of the form <code>LANMultiThing=:=&lt;capability&gt;=:=&lt;command&gt;=:=&lt;free text&gt;=:=&lt;extra&gt;</code>. The device handler doesn't allow any empty strings to make it to the remote end, with the exception of &lt;extra&gt;, as Tasker doesn't really handle them elegantly.

If the free text used with the Notification or Speech Synthesis is of the form <code>&lt;command&gt;=:=&lt;free text&gt;</code> the &lt;command&gt; and &lt;free text&gt; will be extracted.

|capability|command/state|free text|extra||
|---|---|---|---|---|
|alarm|off|off|||
|alarm|siren|siren|||
|alarm|strobe|strobe|||
|alarm|both|both|||
|audioNotification|playTrack|&lt;uri&gt;|&lt;level&gt;||
|audioNotification|playTrackAndResume|&lt;uri&gt;|&lt;level&gt;||
|audioNotification|playTrackAndRestore|&lt;uri&gt;|&lt;level&gt;||
|configuration|configure|configure|||
|notification|deviceNotification|LAN MultiThing||Empty notification text replaced by dummy text.|
|notification|deviceNotification|&lt;free text&gt;||Notification without a valid command.|
|notification|&lt;command&gt;|&lt;free text&gt;||Notification with a valid command.|
|notification|&lt;command&gt;|deviceNotification||Notification only containing a command.
|speechSynthesis|speak|LAN MultiThing||Empty speech text replaced by dummy text.|
|speechSynthesis|speak|&lt;free text&gt;||Speech without a valid command.|
|speechSynthesis|&lt;command&gt;|&lt;free text&gt;||Speech with a valid command.|
|speechSynthesis|&lt;command&gt;|speak||Speech only containing a command.|
|switch|off|off|||
|switch|on|on|||
|tone|beep|beep|||

Incoming HTTP POST requests are sent to <code>http://<hub IP address>:39500/</code>, the content type is <code>application/json</code> and the data is of the form:
  
<pre>{
    "device":"Device Display Name",
    "attribute": {
        "attribute1 name":"attribute1 value",
        "attribute2 name":{"attribute2 field1 name":"attribute2 field1 value"}
    }
    "state": {
        "state1 name":"state1 value",
        "state2 name":"state2 value"
    }
}</pre>

The <code>"device":"Device Display Name",</code> entry is only used to address the messages to child devices, for example to set the child device attributes.

## HTTP Response Motion Sensor
A light in a room is switched automatically by a motion sensor at certain times of day. Very occasionally the room may also be occupied at those times and it would be a nuisance if the lights kept turning off because the occupants were watching the TV and not moving about. If it were possible to detect the TV is switched on then the automation could keep the lights on. Given the automation is working with a motion sensor it is likely to be able to handle a second one. Therefore a device handler which treats the TV being on as active motion would be rather handy.

This simple device handler does the job described above. Every fifteen minutes it resets its status to inactive and then attempts to connect an HTTP server on the IP address and port defined in the preferences. If the parse() command picks up the response the status is set to active. A refresh command can also be used to check the status out of band.

The device handler should not be used in automations that would respond immediately to a change of status to inactive as this may only be momentary while the state is being refreshed.

## URL or AutoRemote Switch
This device handler was created as a learning exercise. It implements a virtual switch that calls URLs for each of the 'on' and 'off' states. The URLs should be specified as full URLs with appropriate encoding. What makes it slightly more interesting is that, if the 'AutoRemote Key' is defined, the device handler will create URLs in the same format as the AutoRemote Send Message Service and the 'on' and 'off' options should be defined as AutoRemote messages instead.

The 'AutoRemote Key' can be found by going to your 'personal URL' (the URL is shown when you open up the AutoRemote app on Android). If you start typing something in the 'Message' field a URL will appear in a dialog box on the page. This URL is of the form <code>https://autoremotejoaomgcd.appspot.com/sendmessage?key=**&lt;key&gt;**&message ...</code> and <code>**&lt;key&gt;**</code> is the rather long bit that you need to copy into the device preferences.


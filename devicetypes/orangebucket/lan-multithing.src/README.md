# LAN MultiThing
This device handler implements the actuator capabilities Alarm, Audio Notification (see below), Configuration, Notification, Speech Synthesis, Switch and Tone by sending messages as HTTP GET messages in a format compatible with the AutoRemote WiFi Service and using AutoApps command format. There really is nothing magical about this and you can do absolutely anything you want with the commands at the other end. The author primarily uses it to implement a replacement for LANnouncer using the AutoRemote WiFi Service to provide an HTTP server for Tasker, and then Tasker to act on the commands.

The device handler is also capable of receiving 'pings' from the remote device sent as HTTP POST requests to port 39500 of the hub in JSON format. Currently these can be used to set the attribute states for the sensor capabilities Air Quality Sensor, Battery, Estimated Time Of Arrival, Motion Sensor, Power Source, Relative Humidity, Speech Recognition, Temperature and Ultraviolet Index, and also to set other variables in the device state map. Adding additional sensor capabilities requires adding the capability to the supported list, adding any UI tiles required, and possibly adding code to the parse() method for those capabilities that don't fit into the simple name and value model.

The device handler can also act as a bridge for individual devices supported by the remote device. There are currently child device handlers for the actuator capability Audio Notification ('Audio'), and the sensor capabilities Estimated Time Of Arrival ('ETA'), Motion Sensor and Speech Recognition ('STT'). These device handlers can be created and maintained independently of the parent device handler which doesn't need to know anything specific about them. The child devices send commands by calling methods on the parent, while the parent will forward incoming messages addressed to the child to the child's parse() method. The remote device should send a 'ping' with the list of child devices and these are then created (or deleted) when the updated() method is run (it is currently down to the user to do this, for example by saving the device preferences in the mobile app).

*It is possible to query the commands and attributes supported by capabilities on the fly. The commands reported for Audio Notification are consistent with the reference documentation. However the Speaker Companion app (previously Speaker Notify With Sound) uses commands which are not part of the Audio Notification or the now deprecated Music Player capabilities, or indeed any at all. They are device specific commands, which is utterly ridiculous. The two commands which have the same names will accept, and ignore, the extra parameter they may be called with by Speaker Companion. The other command is not supported at the moment.*

The device is specified by IP Address and Port in the Preferences, and the MAC address may also be specified (with or without colons and in upper, lower or mixed case). If the MAC address is provided it will be used as the Device Network ID (DNI), otherwise the IP Address and Port are combined in a hex form as the DNI. You might prefer to give your AutoRemote device a fixed IP address using a manual IP or a reserved IP address in your DHCP server. If the MAC address is not provided the incoming 'pings' will not work so a lot of functionality will be lost.

*The device network ID has to be either the MAC address or the hex IP:Port in order for SmartThings to send responses to the parse() method of a device handler. The MAC address is generally preferred and allows the device handler to receive out of band requests from the remote device on port 39500 on the hub. With the hex IP:Port this doesn't work as the remote source port would be completely different for these requests. Unfortunately if you have one device set up with the MAC address and another with the hex IP:Port the latter will not see the responses to its own requests. It would be nice if messages were forwarded based on IP:Port first and then MAC address but that isn't how it works.*

For capabilities that have a state, such as Alarm and Switch, the device handler waits for a response from the server on the device before setting the new state. This doesn't mean the command has worked, only that AutoRemote WiFi service has received it.

The HTTP GET requests are of the form `http://LAN IP ADDRESS:PORT/sendmessage?message=MESSAGE` where the MESSAGE is of the form `DEVICENAME=:=DEVICE DISPLAY NAME=:=CAPABILITY;=:=COMMAND=:=FREE TEXT=:=EXTRA`. The device handler doesn't allow any empty strings to make it to the remote end, with the exception of EXTRA, as Tasker doesn't really handle them elegantly. DEVICENAME is the 'name' property of the device with the spaces stripped out. So if you create your device with the name 'LAN MultiThing' it will be 'LANMultiThing'. DEVICE DISPLAY NAME is the 'displayName' property of the device, which may be different to the 'name' if you have defined it (it is called the 'label' in the IDE). DEVICE DISPLAY NAME, CAPABILITY, COMMAND, FREE TEXT and EXTRA are all URL encoded.

*The author likes to define the 'name' of a device, as shown in the IDE, as either the name of the device type handler (as in 'LAN MultiThing') or the specific make and model number of the device.*

If the free text used with the Notification or Speech Synthesis commands is of the form `COMMAND=:=FREE TEXT` the COMMAND and FREE TEXT will be extracted.

|CAPABILITY|COMMAND (State)|FREE TEXT|EXTRA||
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

Incoming HTTP POST requests are sent to `http://HUB IP ADDRESS:39500/`, the content type is `application/json`, and the data is of the form:
  
<pre>{
    "device":"Device Display Name",
    "attribute": {
        "attribute1 name":"attribute1 value",
        "attribute2 name":{"attribute2 field1 name":"attribute2 field1 value"}
    },
    "state": {
        "state1 name":"state1 value",
        "state2 name":"state2 value"
    },
    "devices": [
        {"name":"child1 name", "type":"child1 type"},
        {"name":"child2 name", "type":"child2 type"}
    ]
}</pre>

The `"device":"Device Display Name",` entry is only used to address the messages to child devices, for example to set the child device attributes. The currently available types of child devices are 'Audio', 'ETA' and 'STT'.

# Anidea-SmartThings &copy; Graham Johnson (orangebucket)
Assorted SmartThings bits and bobs.

## AutoRemote WiFi Thing
This device handler started out as an exercise in communicating over the local LAN using the hub, working with the AutoRemote WiFi Service. However it has developed into a serious tool that implements the capabilities Alarm, Notification, Speech Synthesis, Switch and Tone and sends messages in AutoApps command format to the AutoRemote WiFi Service running on port 1817 of AutoRemote devices. The commands are sent in the query part of an HTTP Get request so there isn't anything particularly magical about them and you can do absolutely anything you want with them at the other end. However the bottom line is that the author wanted to use Tasker on Android devices instead of using LANnouncer.

The device is specified by IP Address and Port in the Preferences and these are combined in a hex form as the Device Network ID. Ideally you will want to give your AutoRemote device a fixed IP address using a manual IP or a reserved IP address in your DHCP server.

*The device network ID has to be either the MAC address or the hex IP:Port in order for SmartThings to send responses to the parse() method of a device handler. The MAC address is generally preferred and if it were to be used the device handler could also easily receive out of band requests from the remote device on port 39500 on the hub. With the hex IP:Port this doesn't work as the remote source port wouldn't be 1817 for these requests. Testing using the MAC address as the device network ID was not a success as the responses to commands were being massively delayed, if indeed they arrived at all. This might have been an error in coding or testing but it wasn't pursued further. The IP address and port works and it saves entering or discovering the MAC address.*

For capabilities that have a state, such as Alarm and Switch, the device handler waits for a response from the server on the device before setting the new state. This doesn't mean the command has worked, only that AutoRemote WiFi service has received it.

The commands are of the form <code>autoremotewifithing=:=&lt;capability&gt;=:=&lt;command&gt;=:=&lt;free text&gt;</code>. The device handler doesn't allow any empty strings to make it to the remote end as Tasker doesn't really handle them elegantly.

If the free text used with the Notification or Speech Synthesis is of the form <code>&lt;command&gt;=:=&lt;free text&gt;</code> the &lt;command&gt; and &lt;free text&gt; will be extracted.

|capability|command/state|free text||
|---|---|---|---|
|alarm|off|off||
|alarm|siren|siren||
|alarm|strobe|strobe||
|alarm|both|both||
|notification|deviceNotification|AutoRemote WiFi Thing|Empty notification text replaced by dummy text.|
|notification|deviceNotification|&lt;free text&gt;|Notification without a valid command.|
|notification|&lt;command&gt;|&lt;free text&gt;|Notification with a valid command.|
|notification|&lt;command&gt;|deviceNotification|Notification only containing a command.
|speechSynthesis|speechSynthesis|AutoRemote WiFi Thing|Empty speech text replaced by dummy text.|
|speechSynthesis|speechSynthesis|&lt;free text&gt;|Speech without a valid command.|
|speechSynthesis|&lt;command&gt;|&lt;free text&gt;|Speech with a valid command.|
|speechSynthesis|&lt;command&gt;|speechSynthesis|Speech only containing a command.|
|switch|off|off|
|switch|on|on|
|tone|beep|beep|

## HTTP Response Motion Sensor
A light in a room is switched automatically by a motion sensor at certain times of day. Very occasionally the room may also be occupied at those times and it would be a nuisance if the lights kept turning off because the occupants were watching the TV and not moving about. If it were possible to detect the TV is switched on then the automation could keep the lights on. Given the automation is working with a motion sensor it is likely to be able to handle a second one. Therefore a device handler which treats the TV being on as active motion would be rather handy.

This simple device handler does the job described above. Every fifteen minutes it resets its status to inactive and then attempts to connect an HTTP server on the IP address and port defined in the preferences. If the parse() command picks up the response the status is set to active. A refresh command can also be used to check the status out of band.

The device handler should not be used in automations that would respond immediately to a change of status to inactive as this may only be momentary while the state is being refreshed.

## URL or AutoRemote Switch
This device handler was created as a learning exercise. It implements a virtual switch that calls URLs for each of the 'on' and 'off' states. The URLs should be specified as full URLs with appropriate encoding. What makes it slightly more interesting is that, if the 'AutoRemote Key' is defined, the device handler will create URLs in the same format as the AutoRemote Send Message Service and the 'on' and 'off' options should be defined as AutoRemote messages instead.

The 'AutoRemote Key' can be found by going to your 'personal URL' (the URL is shown when you open up the AutoRemote app on Android). If you start typing something in the 'Message' field a URL will appear in a dialog box on the page. This URL is of the form <code>https://autoremotejoaomgcd.appspot.com/sendmessage?key=**&lt;key&gt;**&message ...</code> and <code>**&lt;key&gt;**</code> is the rather long bit that you need to copy into the device preferences.


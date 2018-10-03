# Anidea-SmartThings &copy; Graham Johnson (orangebucket)
Assorted SmartThings bits and bobs.

## URL or AutoRemote Switch
This device handler was created as a learning exercise. It implements a virtual switch that calls URLs for each of the 'on' and 'off' states. The URLs should be specified as full URLs with appropriate encoding. What makes it slightly more interesting is that, if the 'AutoRemote Key' is defined, the device handler will create URLs in the same format as the AutoRemote Send Message Service and the 'on' and 'off' options should be defined as AutoRemote messages instead.

The 'AutoRemote Key' can be found by going to your 'personal URL' (the URL is shown when you open up the AutoRemote app on Android). If you start typing something in the 'Message' field a URL will appear in a dialog box on the page. This URL is of the form <code>https:<i></i>//autoremotejoaomgcd.appspot.com/sendmessage?key=**&lt;key&gt;**&message ...</code> and <code>**&lt;key&gt;**</code> is the rather long bit that you need to copy into the device preferences.

## AutoRemote WiFi Thing
This device handler started out as an exercise in communicating over the local LAN using the hub, working with the AutoRemote WiFi Service. However it has developed into a serious tool that implements the capabilities Alarm, Notification, Speech Synthesis, Switch and Tone and sends messages in AutoApps command format to the AutoRemote WiFi Service running on port 1817 of AutoRemote devices. The commands are sent in the query part of an HTTP Get request so there isn't anything particularly magical about them and you can do absolutely anything you want with them at the other end. However the bottom line is that the author wanted to use Tasker on Android devices instead of using LANnouncer.

The device is identified by using a hex version of <code>&lt;IP ADDRESS&gt;:&lt;PORT&gt;</code> as its network ID, so you will probably want to give your AutoRemote device a fixed IP address using a manual IP or a reserved IP address in your DHCP server.
  
An example of a network ID would be <code>C0A8010A:0719</code> for <code>192.168.1.10:1817</code>. Each component of the IP address is converted into two hex digits. The port will always be <code>0719</code> which is a straight conversion of <code>1817</code> into hex.

*Ideally the IP address would be one of the settings. That would work if a SmartApp were being used to dynamically create the devices and it seems it once worked from Device Handlers too. The device network ID has to be either the MAC address or the hex IP:Port in order for SmartThings to send responses to the parse() method.*

For capabilities that have a state, such as Alarm and Switch, the device handler waits for a response from the server on the device before setting the new state. This doesn't mean the command has worked, only that AutoRemote WiFi service has received it.

The commands are of the form <code>autoremotewifithing=:=&lt;capability&gt;=:=&lt;command&gt;=:=&lt;free text&gt;</code>.

The free text used with the Notification or Speech Synthesis can be of the form <code>&lt;command&gt;=:=&lt;free text&gt;</code>. In the absence of this &lt;command&gt; and &lt;free text&gt; are the same.

|capability|command|free text|
|---|---|---|
|alarm|off|*off*|
|alarm|siren|*siren*|
|alarm|strobe|*strobe*|
|alarm|both|*both*|
|notification|*&lt;free text&gt;*|&lt;free text&gt;|
|notification|&lt;command&gt;|&lt;free text&gt;|
|speak|*&lt;free text&gt;*|&lt;free text&gt;|
|speak|&lt;command&gt;|&lt;free text&gt;|
|switch|off|*off*|
|switch|on|*on*|
|tone|beep|*beep*|
l

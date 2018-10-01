# Anidea-SmartThings &copy; Graham Johnson (orangebucket)
Assorted SmartThings bits and bobs.

## URL or AutoRemote Switch
This is a device handler implementing a virtual switch that calls URLs for each of the 'on' and 'off' states. The URLs should be specified as full URLs with appropriate encoding. If the 'AutoRemote Key' is defined, the device handler will create URLs in the same format as the AutoRemote Send Message Service and the 'on' and 'off' options should be defined as AutoRemote messages instead.

The 'AutoRemote Key' can be found by going to your 'personal URL' (the URL is shown when you open up the AutoRemote app on Android). If you start typing something in the 'Message' field a URL will appear in a dialog box on the page. This URL is of the form <code>https:<i></i>//autoremotejoaomgcd.appspot.com/sendmessage?key=**&lt;key&gt;**&message ...</code> and <code>**&lt;key&gt;**</code> is the rather long bit that you need to copy into the device preferences.

## AutoRemote WiFi Thing
This is a device handler implementing the capabilities Alarm, Notification, Speech Synthesis, Switch and Tone which sends messages to AutoRemote in AutoApps command format, using the AutoRemote WiFi Service running on port 1817 of AutoRemote devices. The device is identified by using a hex version of <code>&lt;IP ADDRESS&gt;:&lt;PORT&gt;</code> as its network ID, so you will probably want to give your AutoRemote device a fixed IP address using a manual IP or a reserved IP address in the DHCP server.
  
An example of a network ID would be <code>C0A8010A:0719</code> for <code>192.168.1.10:1817</code>. Each component of the IP address is converted into two hex digits. The port will always be <code>0719</code> which is a straight conversion of <code>1817</code> into hex.

*Ideally the IP address would be one of the settings. That would work if a SmartApp were being used to dynamically create the devices and it seems it once worked from Device Handlers too. It seems that the device network ID has to be either the MAC address or the hex IP:Port in order for SmartThings to send responses to the parse() method. As it happens the responses are of no real interest and there are other ways of getting them if there were, but the intention is to make the code largely general purpose.*

For capabilities that have a state, such as Alarm and Switch, the device handler waits for a response from the server on the device before setting the new state. This doesn't mean the command has worked, only that AutoRemote has received it.

The commands are of the form <code>autoremotewifithing=:=&lt;capability&gt;=:=<&lt;command&gt;</code> (where the capabilities are 'alarm', 'notification', 'speech', 'switch' and 'tone'.

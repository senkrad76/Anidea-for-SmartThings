# Device Health and the Health Check Capability

In which I try and guess things about device health and the Health Check capability ...

## Device Health

The SmartThings ‘Classic’ app offered an option to enable and disable ‘Device Health’. It was often observed that having it switched on caused a lot of confusion 
in the app, with devices that were functioning fine being shown as offline, and even SmartThings themselves often suggested turning it off might be advisable.
The 'new' app doesn’t give this option and there isn’t any useful documentation available about it. The new app is generally more aggressive about the status of devices.

A similar lack of information exists for the Health Check capability, which seems to be the modern face of whatever is going on behind the scenes, and whatever 
is going on behind the scenes is evolving. There is a page about ‘Health Check’ in the ‘new’ documentation but it is characteristically vague and also mentions
the device status UNHEALTHY which isn’t mentioned anywhere else, and the ‘Health API’ which doesn’t make it to the SmartThings API reference.

It should first be noted that by far the most helpful status display has historically been that displayed in the Groovy IDE. This can be accessed using `device.getStatus()` 
in Groovy, which is how webCoRE populates `$status`. The status codes can be one of the following:

* ACTIVE (no longer in use from c. August 2020)
* INACTIVE (no longer in use from c. August 2020)
* ONLINE
* OFFLINE
* HUB_DISCONNECTED (status unclear from c. August 2020)

The ACTIVE and INACTIVE statuses were shown for devices that did not use the Health Check capability, including hubs, with ONLINE and OFFLINE being used by those that did.
The HUB_DISCONNECTED status should only have appeared on hub connected devices when the hub is offline, but for some reason devices could sometimes find 
themselves in that state and appear ‘offline’ to the new app.

SmartThings have announced a move to a hub based system of determining the health of hub connected devices. This is to be available from hub firmware
version 32, and hubs with earlier firmware will be using the status of the hub itself as a proxy for all connected devices. This would seem to have already taken affect by
August 2020 as ACTIVE status was no longer appearing in the IDE. It isn't clear what the situation is with HUB_DISCONNECTED.

## The Health Check Capability

The Health Check capability has four attributes:

* DeviceWatch-Enroll
* DeviceWatch-Status
* checkInterval
* healthStatus

The `DeviceWatch-Enroll` attribute now only seems to be used by devices that don’t regularly report their status, or otherwise aren’t busy enough to be
recognised as online. Specifically it is used to enroll them with 'Device Watch', whatever that might be, as 'untracked'. Generally this would be used 
for cloud devices or virtual / simulated devices. The attribute is in JSON format and the typical content would be `{ "protocol": "cloud", "scheme": "untracked" }`.
If you have a Zigbee device, such as a button, that isn’t used much and doesn’t report very often, then you will see `{ "protocol": "zigbee", "scheme": "untracked" }`.

A fuller example of the code to enroll a device as untracked is the following:
 
`sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)`

This might typically be called in the `installed()` method of a Groovy DTH.

The `DeviceWatch-DeviceStatus` and `healthStatus` attributes both have the permitted values of online or offline. They are both used by the device handlers
for simulated devices to simulate the online status. They also appear in the command mappings for an example cloud connector. There the change history 
shows that originally `healthStatus` was used, then it was changed to `DeviceWatch-DeviceStatus` at the beginning of 2019, and then two days later changed back again.
This tends to suggest that `healthStatus` is now the preferred attribute.

The final attribute, `checkInterval`, is used extensively by device handlers. It sets a number of seconds of device inactivity, after which the `ping()` command
method of the handler will be called to attempt to make the device respond. If that doesn’t help the device will be marked as offline. The interval is typically
set for twice the regular reporting frequency of the device, plus a little bit, to allow one report to be missed. The `checkInterval` events normally carry extra
data, specified as `data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]` or `data:[ protocol: "zwave", hubHardwareId: device.hub.hardwareID ]` but you
sometimes also see extra bits like `offlinePingable: "1"`. 

An example of code to set the checkInterval for 2 hours and ten minutes would be as follows.

`sendEvent( name: 'checkInterval', value: 2 * 60 * 60 + 10 * 60, displayed: false, data: [ protocol: 'zigbee', hubHardwareId: device.hub.hardwareID ] )`

The resulting integer could, of course, be used, but this form just makes it obvious where the number comes from. A reasonable place to include this code is in
the `installed()` method.

*When it comes to Aqara device handlers, the author likes to set an artificially high `checkInterval` to start with and then reduce the `checkInterval` when the
hourly battery reports start up.*

Sometimes when you chop and change device handlers, you can end up with both `checkInterval` and `DeviceWatch-Enroll` attributes being set. It is unclear
whether this is a problem or not. It would depend on whether whatever is going on behind the scenes ever resets what it knows about a device. If it is a
problem then it is a nuisance to resolve as as deleting unwanted attributes doesn’t seem to be possible without completely wiping the device and starting again.

*It also isn’t clear what counts as ‘activity’. My feeling is that it is events generated by the device handler that count, and those events
also have to be propagated, so setting `isStateChange: true` on events generated by regular reports might be appropriate.*

#### &copy; Graham Johnson (orangebucket)
---
# Device Health, the Health Check capability, and the Health API

In which I try and guess things about device health and the Health Check capability ...

The SmartThings 'Classic' app offered an option to enable and disable *Device Health*. It was often observed that having it switched on caused a lot of confusion 
in the app, with devices that were functioning fine being shown as offline, and even SmartThings themselves often suggested turning it off might be advisable.
The 'new' app doesn’t give this option and there isn’t any useful documentation available about it. The new app is generally more aggressive about the status of devices.

A similar lack of information exists for the *Health Check* capability, which seems to be the modern face of whatever is going on behind the scenes, and whatever 
is going on behind the scenes is evolving. There is a [page about ‘Health Check’ in the ‘new’ documentation](https://smartthings.developer.samsung.com/docs/devices/health.html) but it is characteristically vague and it also focuses more on the *Health API*.

It should first be noted that by far the most helpful status display has historically been that displayed in the Groovy IDE. This can be accessed using 
`device.getStatus()` in Groovy, which is how webCoRE populates `$status`. The status codes can be one of the following:

* `ACTIVE` (no longer in use from c. August 2020)
* `INACTIVE` (no longer in use from c. August 2020)
* `ONLINE`
* `OFFLINE`
* `HUB_DISCONNECTED` (status unclear from c. August 2020)

The `ACTIVE` and `INACTIVE` statuses were shown for devices that did not use the *Health Check* capability, including hubs, with `ONLINE` and `OFFLINE` being used by those that did.
The`HUB_DISCONNECTED` status should only have appeared on hub connected devices when the hub is offline, but for some reason devices could sometimes find 
themselves in that state and appear ‘offline’ to the new app.

SmartThings announced a move to a hub based system of determining the health of hub connected devices as the cloud based system was rather unreliable. It became available with the October 2020 release of hub firmware version 32, with hubs with earlier firmware using the status of the hub itself as a proxy for all connected devices. The latter would seem to have already taken affect by
August 2020 as `ACTIVE` status was no longer appearing in the IDE. It isn't clear what the situation is with `HUB_DISCONNECTED`.

The Health Check capability has four attributes:

* `DeviceWatch-Enroll`
* `DeviceWatch-Status`
* `checkInterval`
* `healthStatus`

The `DeviceWatch-Enroll` attribute now only seems to be used by devices that don’t regularly report their status, or otherwise aren’t busy enough to be
recognised as online. Specifically it is used to enroll them with 'Device Watch', whatever that might be, as `untracked`. Generally this would be used 
for cloud devices or virtual / simulated devices. The attribute is in JSON format and the typical content would be `{ "protocol": "cloud", "scheme": "untracked" }`.
If you have a Zigbee device, such as a button, that isn’t used much and doesn’t report very often, then you will see `{ "protocol": "zigbee", "scheme": "untracked" }`.

A fuller example of the code to enroll a device as untracked is the following:
 
`sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)`

This might typically be called in the `installed()` method of a Groovy DTH.

The `DeviceWatch-DeviceStatus` and `healthStatus` attributes both have the permitted values of `online` or `offline`. They are both used by the device handlers
for simulated devices to simulate the online status and `healthStatus` seems to feature in documentation and examples for cloud connected devices using the new integrations. It seems plausible that Device Watch might be a mechanism exclusively used by Groovy DTHs and so `DeviceWatch-DeviceStatus` is the attribute to use for devices enrolled as untracked, with `healthStatus` applying to new integrations. It really isn't clear though.

The final attribute, `checkInterval`, is used extensively by device handlers. It sets a number of seconds of device inactivity, after which the `ping()` command
method of the handler will be called to attempt to make the device respond. If that doesn’t help the device will be marked as offline. The interval is typically
set for twice the regular reporting frequency of the device, plus a little bit, to allow one report to be missed. The `checkInterval` events normally carry extra
data, specified as `data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]` or `data:[ protocol: "zwave", hubHardwareId: device.hub.hardwareID ]` but you
sometimes also see extra bits like `offlinePingable: "1"`. The `checkInterval` attribute doesn't seem to do anything without the additional data having been supplied.

An example of code to set the checkInterval for 2 hours and ten minutes would be as follows.

`sendEvent( name: 'checkInterval', value: 2 * 60 * 60 + 10 * 60, displayed: false, data: [ protocol: 'zigbee', hubHardwareId: device.hub.hardwareID ] )`

The resulting integer could, of course, be used, but this form just makes it obvious where the number comes from. A reasonable place to include this code is in
the `installed()` method.

*When it comes to Aqara device handlers, the author likes to set an artificially high `checkInterval` to start with and then reduce the `checkInterval` when the
hourly battery reports start up.*

Sometimes when you chop and change device handlers, you can end up with both `checkInterval` and `DeviceWatch-Enroll` attributes being set. It is unclear
whether this is a problem or not. It would depend on whether whatever is going on behind the scenes ever resets what it knows about a device. If it is a
problem then it is a nuisance to resolve as as deleting unwanted attributes doesn’t seem to be possible without completely wiping the device and starting again.

*It also isn’t clear what counts as ‘activity’. My feeling used to be that it was events generated by the device handler that counted, and those events
also had to be propagated, so setting `isStateChange: true` on events generated by regular reports would be appropriate. I am now wondering if the changes mean it might now be determined on device activity at the Zigbee and Z-Wave protocol level.*

The `healthStatus` and `DeviceWatch-DeviceStatus` attributes both seem to allow for optional data but there doesn't seem to be an example of any being used in the wild. Since the v32 firmware appeared on the hubs, the `healthStatus`, `DeviceWatch-Enroll` and `DeviceWatch-DeviceStatus` attributes seem to be reported as null even when they have been assigned values, though some devices seem to retain `DeviceWatch-Enroll`. It might be as simple as existing values not having been deleted, but it just doesn't seem that easy. Some new devices seem to get the value while others don't. It has also been observed that since October 5th `DeviceWatch-DeviceStatus` events have stopped being propagated so it isn't clear if legacy apps can now subscribe to any connectivity events. 

The *Health API* is not documented with the REST API for some reason, but allows the connectivity status of devices and hubs to be retrieved. The available statuses seem to be:

* `ONLINE`
* `OFFLINE`
* `UNHEALTHY`
* `CONNECTED` (historical?)
* `DISCONNECTED` (historical?)

The `UNHEALTHY` status is apparently an intermediate state between a device exceeding its 'check interval' without activity and `ping()` being called, and being set to `OFFLINE`. The `CONNECTED` and `DISCONNECTED` statuses are shown as the hub connectivity states on one page of documentation. However on a page showing the actual API calls the example response is `ONLINE` and certainly `ONLINE` and `OFFLINE` are used.

The documentation for cloud to cloud integrations illustrates the `healthStatus` attribute of `st.healthCheck` being set to `online` or `offline` to communicate the device connectivity status, but without any useful comment. Presumably such integrations are inherently untracked. The check interval is mentioned in passing but without any explanation, possibly because it is a concept for hub connected devices which are still legacy services.

#### [Anidea for SmartThings](../../../README.md) > [Anidea for Virtual Devices](../README.md#anidea-for-virtual-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Virtual Binary
This handler implements a multiple attribute binary state device. The overall state is either active, or it is inactive, as expressed by a number of attributes from stock capabilities. When the handler receives any command to set an attribute active, it sets all enabled attributes to be active. When it receives any command to set an attribute to inactive, it sets all enabled attributes to be inactive. The supported attributes, which with the exception of Switch are all disabled by default and should be enabled as required using the device settings, are:

|CAPABILITY|ATTRIBUTE|ACTIVE STATE|COMMAND|INACTIVE STATE|COMMAND|
|----------|---------|--------------|----------------|--------------|----------------|
|Contact Sensor|contact|open|open()|closed|close()|
|Motion Sensor|motion|active|active()|inactive|inactive()|
|Occupancy Sensor|occupancy|occupied|occupied()|unoccupied|unoccupied()|
|Presence Sensor|presence|present|arrived()|not present|departed()|
|Switch|switch|on|on()|off|off()|
|Water Sensor|water|wet|wet()|dry|dry()|

The commands are consistent with those used by other 'Anidea for ...' device handlers. Those are derived from the capability where the device is an actuator, from the commands used by a stock 'Simulated ...' device handler where one is available, and lastly from whatever has been chosen for use in other 'Anidea for ...' device handlers.
*The one exception is that `wet()` and `dry()` were created for the handler.*

The Switch capability is permanently enabled as its presentation includes an on/off button and the SmartThings app returns an error if pushing that button doesn't result in an attribute change. It is therefore a sensible candidate for use on the dashboard tile for both state and action.

A custom capability named 'Binary Sensor' is being developed to represent the inherent active or inactive state of the device, independently of the standard capabilities. However custom capabilities aren't mature enough to release this yet.

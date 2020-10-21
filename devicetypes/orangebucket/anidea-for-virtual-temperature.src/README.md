#### [Anidea for SmartThings](../../../README.md) > [Anidea for Virtual Devices](../README.md#anidea-for-virtual-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Virtual Temperature
The Simulated Temperature Sensor uses Switch Level to give local control in the Classic app. However this confuses things in the 'new' app because the attribute `level` is never set. There are also issues because the units are never set in the events, and also there is an omission in that the level isn't updated when the temperature is changed remotely. This device handler has been written to work properly in the new app, and not at all in the Classic app. The `up()`, `down()` and `setTemperature()` custom commands allow the temperature to be incremented, decremented and set to a particular value, using the command names from the simulated sensor as a de facto standard. The use of Switch Level has been retained and this means the handler will be able to work in Automations using `setLevel()`, without the need to implement a custom capability for the `up()`, `down()` and `setTemperature()` commands with any urgency.

The implementation of Switch Level in the UI for the new app doesn't seem to support anything but 0 to 100. To work with this, the handler defaults of a range of -40 C to 150 C, or -40 F to 302 F, depending on the temperature scale setting in the Location (there doesn't seem to be a way to change this in the new app, but it can be changed via the IDE). These values can be overridden in the settings. Temperature values below the 0% value, or above the 100% value, are changed to the minimum or maximum readings.

*The Switch Level capability in the device details doesn't seem to support entering 0%, and the Settings screen can be a little odd with 0 values too.*

*Please be aware that webCoRE recognises the `up()` and `down()` commands from another capability, and so presents them as 'Pan Camera Up' and 'Pan Camera Down'.*

#### [Anidea for SmartThings](../../../README.md) > [Anidea for Virtual Devices](../README.md#anidea-for-virtual-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Virtual Humidity
This handler has been created as a companion to the [Anidea for Virtual Temperature](../anidea-for-virtual-temperature.src) handler. The `up()`, `down()` and `setHumidity()` custom commands allow the relative humidity to be incremented, decremented and set to a particular value. Switch Level is being (ab)used to allow the handler to work in Automations using `setLevel()`, without the need to implement a custom capability for the `up()`, `down()` and `setHumidity()` commands with any urgency.

The implementation of Switch Level in the UI for the new app doesn't seem to support anything but 0 to 100, so values are being rounded to the nearest integer. Humidity values below the 0% value, or above the 100% value, are changed to the minimum or maximum readings.

*The Switch Level capability in the device details doesn't seem to support entering 0%, and the Settings screen can be a little odd with 0 values too.*

*Please be aware that webCoRE recognises the `up()` and `down()` commands from another capability, and so presents them as 'Pan Camera Up' and 'Pan Camera Down'.*

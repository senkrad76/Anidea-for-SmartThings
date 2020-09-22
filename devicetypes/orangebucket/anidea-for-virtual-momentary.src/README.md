#### [Anidea for SmartThings](../../../README.md) > [Anidea for Virtual Devices](../README.md#anidea-for-virtual-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Virtual Momentary
This device handler extends the [Virtual Button](../anidea-for-virtual-button.src/) with a momentary action for the Contact Sensor, Motion Sensor and Switch capabilities. The contact and motion actions are not enabled by default and should be enabled via the device settings as required. The switch action is permanently enabled as the new app doesn't like having an on/off button that doesn't actually do anything. 

Originally, pressing the momentary tile, or calling the `push()` method, set the active states (`open`, `active` and `on`) as required, and then immediately reset them to the inactive states (`closed`, `inactive` and `off`). This remains the default operation. However, an optional delay has been added between the active and inactive states changes as it was envisaged that could be useful in some applications. That action is dependent on a timer.

Pressing the action button on the switch tile, or calling `on()` also activates the momentary action. The `off()` method does nothing.

_This handler was kept separate from the [Virtual Button](../anidea-for-virtual-button.src/) because at the time it wasn't possible to control which attributes appeared as the dashboard status and action, and also because it really cluttered up the UI. It was created without the Button capability. Sean (steinauf) pointed out that having the Button capability was useful as it could trigger webCoRE without generating semaphore waits. Adding to Sean's valid point, it could also be argued that Momentary is really intended as a way to 'push' a button remotely, so any handler with Momentary probably ought to have Button too._

#### [Anidea for SmartThings](../../../README.md) > [Anidea for Virtual Devices](../README.md#anidea-for-virtual-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Virtual Momentary
This device handler implements a momentary action for the Contact Sensor, Motion Sensor and Switch capabilities. Although not really necessary, the contact and motion actions are not enabled by default and should be enabled via the device settings as required. The switch action is permanently enabled as the new app doesn't like having an on/off button that doesn't actually do anything. 

Originally, pressing the momentary tile, or calling the `push()` method, set the active states (`open`, `active` and `on`) as required, and then immediately reset them to the inactive states (`closed`, `inactive` and `off`). This remains the default operation. However, an optional delay has been added between the active and inactive states changes as it was envisaged that could be useful in some applications. That action is dependent on a timer.

Pressing the action button on the switch tile, or calling `on()` also activates the momentary action. The `off()` method does nothing.

*This handler could have been combined with the Virtual Button, but testing suggested the tile in the mobile app would default to the contact status rather than the button and at the time there wasn't anything that could be done about it, and also the device details screen was a bit too messy.*

Sean (steinauf) pointed out that despite the above it would still be useful to have a Button capability and on reflection it doesn't seem right having Momentary without the Button it is clearly meant for, so this has been added as standard.

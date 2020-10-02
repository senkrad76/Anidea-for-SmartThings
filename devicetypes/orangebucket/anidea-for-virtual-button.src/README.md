#### [Anidea for SmartThings](../../../README.md) > [Anidea for Virtual Devices](../README.md#anidea-for-virtual-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Virtual Button
This device handler implements a virtual button using the Button and Momentary capabilities. 

The Button capability supports the `pushed` and `down_6x` values for the `button` attribute, the latter only being used to seed the button attribute when a device is installed or updated (the mobile app likes attributes to have valid values).

The Momentary action 'presses' the button, generating the `pushed` event on the `button` attribute. The momentary action can be activated from the dashboard tile, the Momentary tile on the device details page, as an Automation action, or by other apps (e.g. webCoRE) capable of calling the `push()` command method.

#### [Anidea for SmartThings](../../../README.md) > [Anidea for Virtual Devices](../README.md#anidea-for-virtual-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Virtual Button
This device handler implements the Button and Momentary capabilities and sends `pushed` events when the momentary tile is pressed in the new app, or the `push()` method is called from other apps e.g. webCoRE. The handler also supports the `down_6x` value of the button, but this is only used to seed the button attribute at start up, which is something that keeps the new app happy.

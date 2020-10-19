#### [Anidea for SmartThings](../../../README.md) > [Anidea for Lumi Devices](../README.md#anidea-for-lumi-devices) - (C) Graham Johnson (orangebucket)
---
 s
# Anidea for Aqara Vibration
The reworking of the handler for the vibration sensor, model DJT11LM, seems to be most of the way there, although a lot of tidying up of the code is still required and there are custom attributes and commands that need replacing or removing. At the moment it is suggested this handler should be used with a degree of caution.

*As of October 2020, tiles are being displayed on the device details page that suggest they can be used for registering the open and closed positions for the sensor. The reality is that pressing the button just causes an icon to spin around instead of running the commands in the handler. This wasn't really intended for public consumption but accidentally got caught up in a GitHub pull request for something else. If you do need to set the open and closed positions you can call the `setopen` or `setclosed` commands using webCoRE. That isn't much use but that is the state of play at the moment.*

There has been one significant change from the original. 'Vibration' is now mapped to the Acceleration Sensor capability and 'tilt' to the Motion Sensor, reversing the mapping in the 'bspranger' handler. The Acceleration Sensor capability is presented as a Vibration Sensor in the 'new' app and so it is an obvious change.

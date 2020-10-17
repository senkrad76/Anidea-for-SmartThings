#### [Anidea for SmartThings](../../../README.md) > [Anidea for Lumi Devices](../README.md#anidea-for-lumi-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Aqara Vibration
The reworking of the handler for the vibration sensor, model DJT11LM, seems to be most of the way there, although a lot of tidying up of the code is still required and there are custom attributes and commands that need replacing or removing. At the moment it is suggested this handler should be used with a degree of caution.

There has been one significant change from the original. 'Vibration' is now mapped to the Acceleration Sensor capability and 'tilt' to the Motion Sensor, reversing the mapping in the 'bspranger' handler. The Acceleration Sensor capability is presented as a Vibration Sensor in the 'new' app and so it is an obvious change.

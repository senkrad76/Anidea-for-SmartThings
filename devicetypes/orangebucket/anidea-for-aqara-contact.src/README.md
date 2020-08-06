#### [Anidea for SmartThings](../../../README.md) > [Anidea for Lumi Devices](../../../README.md#anidea-for-lumi-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Aqara Contact
_**The handlers for the Mijia (MCCGQ01LM) and Aqara Door and Window sensors only truly differed in the fingerprints of the devices and how exactly the same on/off event was handled, so one handler now covers both options.**_

Please be aware that the handler sets the status of the sensor to `closed` when it is installed (or updated via the IDE) as initialising the attributes just seems to make things work better. The custom commands to 'reset' the attribute to a known state have been retained, but renamed to `open()` and `close()` to match those in the Simulated Contact Sensor.

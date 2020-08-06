#### [Anidea for SmartThings](../../../README.md) > [Anidea for Lumi Devices](../../../README.md#anidea-for-lumi-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Aqara Contact
_**The 'bspranger' handlers included two separate handlers for what are being referred to as the Mijia and Aqara contacts sensors, but close examination revealed that the only differences were in the fingerprints of the devices and the detail of the way the same on/off event was being handled. So only one handler seems necessary.**_

This handler has only been tested with the Lumi MCCGQ01LM (being referred to as the 'Mijia') and MCCGQ11LM ('Aqara') door and window sensors, which identify at the Zigbee level with the models `lumi.sensor_magnet` and `lumi.sensor_magnet.aq2`.

Please be aware that the handler sets the status of the sensor to `closed` when it is installed (or updated via the IDE) as initialising the attributes makes things work better in the SmartThings app. The setter commands to set the attribute to a known state have been renamed from the original handlers to `open()` and `close()`, which matches those used in the Simulated Contact Sensor. They are being implemented using a custom capability, which at the time of writing has been configure so that it doesn't display anything on the device details pages in the app (because it doesn't work) and will not yet work with automations (as ST have not implemented that yet).

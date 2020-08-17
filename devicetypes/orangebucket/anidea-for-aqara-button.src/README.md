#### [Anidea for SmartThings](../../../README.md) > [Anidea for Lumi Devices](../README.md#anidea-for-lumi-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Aqara Button
<img src="../../../images/afab_details.jpg?raw=true" align="right" width="100">This handler supports the same buttons as the 'bspranger' handler for Aqara buttons, but only the 'original version' of the WXKG11LM button (or 'Wireless Mini Switch') has actually been tested. The others hopefully should work but there is always the possibility that cosmetic changes to the code, and the odd bit of butchery, may have broken things. The most significant change is that it uses a broader ranger of button attribute values instead of using button numbers. The values used across the various buttons are:

* **pushed** (also used for the Momentary capability)
* **pushed_2x** (note, NOT double)
* **pushed_3x**
* **pushed_4x**
* **pushed_6x** (to represent 'shaken')
* **double** (to represent the hold release)
* **down_6x** (for a button press in the installation routine)

<img src="../../../images/afab_dashboard.jpg?raw=true" align="right" width="100">Following the SmartThings app suddenly refusing to connect to the devices in August 2020, a device presentation was created for the device handler to see if that would kick things into life. It did, and as a part of the fix a momentary button was added to the dashboard tile to handle single presses.

*The full range of attribute values is not available natively in webCoRE as that uses a lookup table which hasn't been updated. However the values can be used in a trigger condition by using an 'expression' instead of a 'value', and entering the event value as a double-quoted string e.g. `"pushed_2x"` (single quotes didn't work but this might have been because of other issues so needs to be tried again).*

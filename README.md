# Anidea for SmartThings (Anidea-ST)<br>&copy; Graham Johnson (orangebucket)

A repository of assorted SmartThings bits and bobs that were created for use within the owner's personal SmartThings environment, but hopefully to a standard that could potentially make them useful to others.

**Please be aware that this is very much a personal repository, and so can be subject to the most trivial of changes. There isn't currently any staging or anything going on. There is just the one branch.**

The repository layout is compatible with the IDE for the 'classic' [Device Handlers](devicetypes/orangebucket) and SmartApps that are written in Groovy. 

The terminology for the 'new' environment is all over the place. The term SmartApp appears in the documentation as 'SmartApp Connector' (for cloud-connected devices not using the 'Schema Connector'), as 'SmartApp' (for Automations), and 'WebHook SmartApp' (for automations not using the AWS Lambdas). However in the Developer Workspace you get it in the context of  'Automation SmartApp' (also written 'Automation | SmartApp') or 'Automation Connector | SmartApp'. So in the absence of any convention, the top level folder [automations](automations) is being used, with the namespace underneath.

The following device handlers deliberately do not define a UI for the SmartThings Classic app. They are the ones I think other users may be interested in.

- [Anidea for Lumi Devices](#anidea-for-lumi-devices)
  - [Anidea for Aqara Button](#anidea-for-aqara-button)
  - [Anidea for Aqara Contact](#anidea-for-aqara-contact)
  - [Anidea for Aqara Motion](#anidea-for-aqara-motion)
  - [Anidea for Aqara Temperature](#anidea-for-aqara-temperature)
  - [Anidea for Aqara Vibration](#anidea-for-aqara-vibration)
  - [Anidea for Mijia Contact](#anidea-for-mijia-contact)
- [Anidea for HTTP Ping](devicetypes/orangebucket/anidea-for-http-ping.src/)
- [Anidea for Scene Momentary](devicetypes/orangebucket/anidea-for-scene-momentary.src/)
- [Anidea for Virtual Devices](#anidea-for-virtual-devices)
  - Anidea for Virtual Binary
  - Anidea for Virtual Button
  - Anidea for Virtual Momentary
  - Anidea for Virtual Presence
  - Anidea for Virtual Temperature

This device handler is perhaps a little more bespoke than the others and it still supports a UI in the Classic app. You really don't want to use it.

- [LAN MultiThing](devicetypes/orangebucket/lan-multithing.src)

There is also a REST API client script for viewing capabilities, and a WebHook Endpoint library and example app. There might be some interest in reading those.

- [Anidea for WebHook Wrapper](automations/orangebucket/anidea-for-webhook-wrapper/)
- [Bucket](automations/orangebucket/anidea-for-webhook-wrapper/)
- [SmartThings Capabilities](automations/orangebucket/smartthings-capabilities/)
---
## Anidea for Lumi Devices
<img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_button.png" width="100"><img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_contact.png" width="100"><img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_motion.png" width="100"><img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_temperature.png" width="100"><img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_vibration.png" width="100">

- [Anidea for Aqara Button](#anidea-for-aqara-button)
- [Anidea for Aqara Contact](#anidea-for-aqara-contact)
- [Anidea for Aqara Motion](#anidea-for-aqara-motion)
- [Anidea for Aqara Temperature](#anidea-for-aqara-temperature)
- [Anidea for Aqara Vibration](#anidea-for-aqara-vibration)
- [Anidea for Mijia Contact](#anidea-for-mijia-contact)
  
The ['bspranger' device handlers](https://github.com/bspranger/Xiaomi) are the results of a cumulative community effort (largely driven by different single individuals at different times), to support the Mijia and Aqara brands of sensors made by Lumi, but generally referred to by the Xiaomi name. The sensors use Zigbee in a rather non-standard way and so they need special handling. SmartThings recognise their popularity enough to make some allowances for them, but neither they nor Lumi have created 'official' handlers. Although they are very effective, the handlers have a number of issues: they are very much rooted in the Classic environment; the logging might be considered a bit excessive and untidy; there are several custom attributes that don't really add anything; the Health Check support isn't quite right; and most of the settings are pretty much superfluous (for example, why have a UK / US date format setting when log messages are already timestamped?). 

The 'Anidea for ...' handlers strip things down and make them suitable for the 'new' app and environment, with the Classic app no longer supported. Suitable custom capabilities and device presentations will be created where required, but the tools required only entered alpha test in mid-June 2020.

**Although a lot of edits have been made to the device handlers, they remain underpinned by the code from the 'bspranger' handlers when it comes to the Zigbee side of things, and also when it comes to the maths used in the Vibration Sensor.**

The common changes made to all the handlers include:

* Completely remove the `tiles()` section as the Classic app is not being supported.
* Remove custom attributes and commands, except for custom 'setter' commands (used to force attributes to particular values).
* Rename custom setter commands where they differ from the equivalent commands in ST stock handlers.
* Define the custom setter commands using custom capabilities rather than using `command`.
* Initialise all attributes in the `installed()` method (the 'new' app isn't keen on attributes without values).
* Initialise `checkInterval` to twenty-four hours as battery reports take a few hours to appear.
* Set a `checkInterval` of two hours ten minutes once the first of the regular battery reports has arrived.
* Fix 0% and 100% battery levels to 2.7 V and 3.2 V rather than having settings.
* Change logging to 'house style', using `info` for each method entered, and `debug` for finer details.
* Change code to 'house style': lower case variable and method names except where required for compatibility; Allman style indentation; single quotes where possible; spaces around contents of brackets and parentheses.

### Anidea for Aqara Button
<img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/afab_details.jpg" align="right" width="100">This handler supports the same buttons as the 'bspranger' handler for Aqara buttons, but only the 'original version' of the WXKG11LM button (or 'Wireless Mini Switch') has actually been tested. The others hopefully should work but there is always the possibility that cosmetic changes to the code, and the odd bit of butchery, may have broken things. The most significant change is that it uses a broader ranger of button attribute values instead of using button numbers. The values used across the various buttons are:

* **pushed** (also used for the Momentary capability)
* **pushed_2x** (note, NOT double)
* **pushed_3x**
* **pushed_4x**
* **pushed_6x** (to represent 'shaken')
* **double** (to represent the hold release)
* **down_6x** (for a button press in the installation routine)

*The full range of attribute values is not available natively in webCoRE as that uses a lookup table which hasn't been updated. However the values can be used in a trigger condition by using an 'expression' instead of a 'value', and entering the event value as a double-quoted string e.g. `"pushed_2x"` (single quotes didn't work but this might have been because of other issues so needs to be tried again).*

### Anidea for Aqara Contact
The handlers for the Mijia (MCCGQ01LM) and Aqara Door and Window sensors only truly differed in the fingerprints of the devices and how exactly the same on/off event was handled, so one handler now covers both options. Please be aware that the handler sets the status of the sensor to `closed` when it is installed (or updated via the IDE) as initialising the attributes just seems to make things work better. The custom commands to 'reset' the attribute to a known state have been retained, but renamed to `open()` and `close()` to match those in the Simulated Contact Sensor.

### Anidea for Aqara Motion
This supports the same Aqara motion sensors as the original, providing both Motion and Illuminance. The sensors do not send inactive reports so the device handler resets motion using a timer. A number of sensors have been used with a sixty second timer for a considerable time without any obvious issue, but as this matches the 'blind' period of the sensors, and SmartThings times aren't particularly precise, it seems better to relax the default period to 65 seconds to avoid any race condition.

During April 2020, the author experienced a considerable number of issues with the motion not being reset, and all the indications were that the timer event simply wasn't being received. For this reason, `active()` and `inactive()` custom commands have been added, matching the names used in the Simulated Motion Sensor, to allow the attribute to be 'reset' to a known state. The `active()` command does not set the timer.

### Anidea for Aqara Temperature
<img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/afat_details.jpg" align="right" width="100">This supports the Aqara temperature and humidity sensor, model WSDCGQ11LM. The 'bspranger' handler extracted the atmospheric pressure but never gave it an attribute. It now uses the proposed Atmospheric Pressure Measurement capability with the `atmosphericPressure` attribute. The new app can now work with this in Automations as well as on the device pages, but it might not yet be recognised by the Developer Workspace (it hasn't been checked for a while).

The capability definition only includes the single unit 'kPa', with a range of 0 to 110, and the app only seemed to be able to display integer values when last checked (displaying zero rather than truncating). This is pretty useless as 1 kPa is the equivalent of 7.5 mmHg or 10 mbar. The device itself is specified from 30 kPa to 110 kPa, with a precision of 0.12 kPa, and seems to return units of 0.01 kPa. The combination of the capability and its implementation in the app does not really seem to be fit for purpose. Currently the handler ignores the letter of the capability and offers a choice of 'kPa', 'hPa', 'mbar', 'mmHg', or 'inHg'. However it is rather obliged to respect the display problem and only use integer values, making 'kPa' and 'inHg' pretty useless.

*The units are displayed incorrectly in the Settings area of the app because the first letter is being folded to upper case, turning correct abbreviations such as 'hPa' into the nonsensical 'HPa'.*

At the time of writing, the alpha release of the CLI tool has been used to create a custom device presentation so the temperature displays on the dashboard tile. Without it the humidity seems to display. It is assumed this works for other users.

### Anidea for Aqara Vibration
The reworking of the handler for the vibration sensor, model DJT11LM, is pretty much complete, with any further changes likely to be tidying up of the code. There has been one significant change from the original. 'Vibration' is now mapped to the Acceleration Sensor capability and 'tilt' to the Motion Sensor, reversing the mapping in the 'bspranger' handler. The Acceleration Sensor capability is presented as a Vibration Sensor in the 'new' app and so it is an obvious change.

### Anidea for Mijia Contact
The [Anidea for Aqara Contact](#anidea-for-aqara-contact) handler also covers an earlier model.

---
## Anidea for Virtual Devices

- [Anidea for Virtual Binary](devicetypes/orangebucket/anidea-for-virtual-binary.src)
- [Anidea for Virtual Button](devicetypes/orangebucket/anidea-for-virtual-button.src)
- [Anidea for Virtual Momentary](devicetypes/orangebucket/anidea-for-virtual-momentary.src)
- [Anidea for Virtual Presence](devicetypes/orangebucket/anidea-for-virtual-presence.src)
- [Anidea for Virtual Temperature](devicetypes/orangebucket/anidea-for-virtual-temperature.src)
  
At the time the [Anidea for Virtual Button](devicetypes/orangebucket/anidea-for-virtual-binary.src) handler was created, there simply wasn't a stock handler that implemented a virtual button with the momentary capability and worked cleanly with the 'new' SmartThings mobile app. Once that was put together, consideration was given to adding support for the Switch capability, as used by the stock Momentary Button Tile handler, and also Contact Sensor and Motion Sensor capabilities as the author was vaguely aware that sort of thing was useful for working with Alexa (probably incorrectly as it turns out, as it looks like Alexa needs more than momentary changes for triggering routines). As adding those capabilities made the device details page look a bit of a mess, and more significantly made the `contact` attribute the default tile status instead of `button` (which could not be corrected at the time), it was decided to create a separate handler instead, hence [Anidea for Virtual Momentary](devicetypes/orangebucket/anidea-for-virtual-momentary.src).

It also seems to be useful to be able to do things like map `switch` attributes to `contact` attributes, and vice versa. Hence the [Anidea for Virtual Binary](devicetypes/orangebucket/anidea-for-virtual-binary.src) handler. As it turns out, this is the one that is handy for Alexa routines.

Mobile presence has been using both the Presence Sensor and Occupancy Sensor capabilities for some time. The [Anidea for Virtual Presence](devicetypes/orangebucket/anidea-for-virtual-presence.src) does likewise.

A post on Facebook mentioned that the Simulated Temperature Sensor didn't work with the new app. This led to [Anidea for Virtual Temperature](devicetypes/orangebucket/anidea-for-virtual-temperature.src) being created.

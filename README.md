#### &copy; Graham Johnson (orangebucket)
---

# Anidea for SmartThings (Anidea-ST)

A repository of assorted SmartThings bits and bobs that were created for use within the owner's personal SmartThings environment, but hopefully to a standard that could potentially make them useful to others.

**Please be aware that this is very much a personal repository, and so can be subject to the most trivial of changes. There isn't currently any staging or anything going on. There is just the one branch.**

The repository layout is compatible with the IDE for the 'classic' [Device Handlers](devicetypes/orangebucket) and SmartApps that are written in Groovy. 

The terminology for the 'new' environment is all over the place. The term SmartApp appears in the documentation as 'SmartApp Connector' (for cloud-connected devices not using the 'Schema Connector'), as 'SmartApp' (for Automations), and 'WebHook SmartApp' (for automations not using the AWS Lambdas). However in the Developer Workspace you get it in the context of  'Automation SmartApp' (also written 'Automation | SmartApp') or 'Automation Connector | SmartApp'. So in the absence of any convention, the top level folder [automations](automations) is being used, with the namespace underneath.

The following device handlers deliberately do not define a UI for the SmartThings Classic app. They are the ones that would seem the most likely to interest others.

- [Anidea for Lumi Devices](#anidea-for-lumi-devices)
  - Anidea for Aqara Button
  - Anidea for Aqara Contact
  - Anidea for Aqara Motion
  - Anidea for Aqara Temperature
  - Anidea for Aqara Vibration
  - Anidea for Mijia Contact
- [Anidea for Virtual Devices](#anidea-for-virtual-devices)
  - Anidea for Virtual Binary
  - Anidea for Virtual Button
  - Anidea for Virtual Momentary
  - Anidea for Virtual Presence
  - Anidea for Virtual Temperature

These two handlers are also only for the 'new' app. HTTP Ping does a job but, apart from the code perhaps being of some limited interest, seems unlikely to be particularly useful to others. The Scene Momentary is really just illustrating yet another way of activating Scenes in places where native support isn't yet available.

- [Anidea for HTTP Ping](devicetypes/orangebucket/anidea-for-http-ping.src/)
- [Anidea for Scene Momentary](devicetypes/orangebucket/anidea-for-scene-momentary.src/)

The following device handler is perhaps a little more bespoke than the others and it still supports a UI in the Classic app. It was as much a learning exercise as anything. It works but it is all a bit contrived.

- [LAN MultiThing](devicetypes/orangebucket/lan-multithing.src)

There is also a REST API client script for viewing capabilities, and a WebHook Endpoint library and example app. There might be some interest in reading those.

- [Anidea for WebHook Wrapper](automations/orangebucket/anidea-for-webhook-wrapper/)
- [Bucket](automations/orangebucket/bucket/)
- [SmartThings Capabilities](automations/orangebucket/smartthings-capabilities/)

---

## Anidea for Lumi Devices
<img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_button.png" width="100"><img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_contact.png" width="100"><img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_motion.png" width="100"><img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_temperature.png" width="100"><img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/aqara_vibration.png" width="100">

- [Anidea for Aqara Button](devicetypes/orangebucket/anidea-for-aqara-button.src/)
- [Anidea for Aqara Contact](devicetypes/orangebucket/anidea-for-aqara-contact.src/)
- [Anidea for Aqara Motion](devicetypes/orangebucket/anidea-for-aqara-motion.src/)
- [Anidea for Aqara Temperature](devicetypes/orangebucket/anidea-for-aqara-temperature.src/)
- [Anidea for Aqara Vibration](devicetypes/orangebucket/anidea-for-aqara-vibration.src/)
- [Anidea for Mijia Contact](devicetypes/orangebucket/anidea-for-aqara-contact.src/)
  
The ['bspranger' device handlers](https://github.com/bspranger/Xiaomi) are the results of a cumulative community effort (largely driven by different single individuals at different times, with particular mentions due to Wayne ('a4refillpad'), Brian Spranger ('ArstenA' / 'bspranger') and Keith G ('veeceeoh')), to support the Mijia and Aqara brands of sensors made by Lumi, but generally referred to by the Xiaomi name. The sensors use Zigbee in a rather non-standard way and so they need special handling. SmartThings recognise their popularity enough to make some allowances for them, but neither they nor Lumi have created 'official' handlers. Although they are very effective, the handlers are not without issues, and in particular they are very much rooted in the Classic environment and some of the the Health Check support isn't quite right. There are other things about them which are more about personal taste: the logging might be considered a bit excessive and untidy; there are several custom attributes that don't really add anything; and most of the settings are pretty much superfluous (for example, why have a UK / US date format setting when log messages are already timestamped?). 

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

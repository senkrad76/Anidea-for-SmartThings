#### [Anidea for SmartThings](../../../README.md) - (C) Graham Johnson (orangebucket)
---

# Anidea for Groovy Device Handlers

- [Anidea for Lumi Devices](#anidea-for-lumi-devices)
- [Anidea for Virtual Devices](#anidea-for-virtual-devices)
- [Anidea for Odds and Sods](#anidea-for-odds-and-sods)

---

## Anidea for Lumi Devices
<img src="../../images/aqara_button.png?raw=true" width="100"><img src="../../images/aqara_contact.png?raw=true" width="100"><img src="../../images/aqara_motion.png?raw=true" width="100"><img src="../../images/aqara_temperature.png?raw=true" width="100"><img src="../../images/aqara_vibration.png?raw=true" width="100">

- [Anidea for Aqara Button](anidea-for-aqara-button.src/)
- [Anidea for Aqara Contact](anidea-for-aqara-contact.src/)
- [Anidea for Aqara Motion](anidea-for-aqara-motion.src/)
- [Anidea for Aqara Temperature](anidea-for-aqara-temperature.src/)
- [Anidea for Aqara Vibration](anidea-for-aqara-vibration.src/)
- [Anidea for Mijia Contact](anidea-for-aqara-contact.src/)
  
The ['bspranger' device handlers](https://github.com/bspranger/Xiaomi) are the results of a cumulative community effort to support the Mijia and Aqara brands of sensors made by Lumi, but generally referred to by the Xiaomi name. The work seems to have been largely driven by different single individuals at different times, with particular mentions due to Wayne Man ('a4refillpad') for creating the first set of handlers, Brian Spranger ('ArstenA' / 'bspranger') for taking the work forward, and Keith Gaumont ('veeceeoh') for keeping it going. The original handlers also credit Alec McLure ('alecm'), Alix JG ('alixjg'), Christian Scheiene ('cscheiene'), 'gn0st1c', 'foz333', Jon Magnuson ('jmagnuson'), 'rinkek', Ron van de Graaf ('ronvandegraaf'), 'snalee', Steven Dale ('tmleafs'), Andy ('twonk') and Christian Paiva ('xtianpaiva'), with apologies to anyone that has been overlooked.

The sensors use Zigbee in a rather non-standard way and so they need special handling. SmartThings recognise their popularity enough to make some allowances for them, but neither they nor Lumi have created 'official' handlers. Although they are very effective, the handlers are not without issues, and in particular they are very much rooted in the Classic environment and some of the the Health Check support isn't quite right. There are other things about them which are more about personal taste: the logging might be considered a bit excessive and untidy; there are several custom attributes that don't really add anything; and most of the settings are pretty much superfluous (for example, why have a UK / US date format setting when log messages are already timestamped?). 

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

- [Anidea for Virtual Binary](anidea-for-virtual-binary.src)
- [Anidea for Virtual Button](anidea-for-virtual-button.src)
- [Anidea for Virtual Humidity](anidea-for-virtual-humidity.src)
- [Anidea for Virtual Momentary](anidea-for-virtual-momentary.src)
- [Anidea for Virtual Presence](anidea-for-virtual-presence.src)
- [Anidea for Virtual Temperature](anidea-for-virtual-temperature.src)
  
At the time the [Anidea for Virtual Button](anidea-for-virtual-button.src) handler was created, there simply wasn't a stock handler that implemented a virtual button with the momentary capability and worked cleanly with the 'new' SmartThings mobile app. Once that was put together, consideration was given to adding support for the Switch capability, as used by the stock Momentary Button Tile handler, and also Contact Sensor and Motion Sensor capabilities as the author was vaguely aware that sort of thing was useful for working with Alexa. As adding those capabilities made the device details page look a bit of a mess, and more significantly made the `contact` attribute the default tile status instead of `button` (which could not be corrected at the time), it was decided to create a separate handler instead, hence [Anidea for Virtual Momentary](anidea-for-virtual-momentary.src). An optional delay has been added between the momentary active and inactive actions, which is perhaps a misnomer, but seems potentially useful. Having a Momentary action without a Button seems wrong so that has also been added, and the handler can now be considered as an extension of [Anidea for Virtual Button](anidea-for-virtual-button.src).

It also seems to be useful to be able to do things like map `switch` attributes to `contact` attributes, and vice versa. Hence the [Anidea for Virtual Binary](anidea-for-virtual-binary.src) handler. This can be useful for Alexa routines and it is particularly useful as an alternative to stock simulated sensors when the setter commands aren't available.

Mobile presence has been using both the Presence Sensor and Occupancy Sensor capabilities for some time. The [Anidea for Virtual Presence](anidea-for-virtual-presence.src) does likewise. Please be aware that this handles presence and occupancy independently and the [Anidea for Virtual Binary](anidea-for-virtual-binary.src) handler might be better suited for some purposes.

A post on Facebook mentioned that the Simulated Temperature Sensor didn't work with the new app. This led to [Anidea for Virtual Temperature](anidea-for-virtual-temperature.src) being created. A few months later a similar appeal was made for devices handlers for virtual humidity so that led to [Anidea for Virtual Humidity](anidea-for-virtual-humidity.src).

---

## Anidea for Odds and Sods

- [Anidea for HTTP Ping](anidea-for-http-ping.src)
- [Anidea for Scene Momentary](anidea-for-scene-momentary.src)
- [LAN Multithing](lan-multithing.src)

The last group of DTHs probably aren't of any practical use to other users though there might be something in the code that is of interest. [Anidea for HTTP Ping](anidea-for-http-ping.src) was written to detect a TV being switched on as active motion, back when Smart Lighting appeared to behave sensibly with multiple motion sensors. [Anidea for Scene Momentary](anidea-for-scene-momentary.src) is a momentary button that activates a scene, which could have been useful in ActionTiles pending the arrival of the official integration (which is now live), though it is also easy to use a bit of JavaScript in a URL Shortcut or a virtual switch with a trivial automation. [LAN Multithing](lan-multithing.src) was largely written as a learning exercise, though it does have a practical application. It really is a bit contrived though.

#### &copy; Graham Johnson (orangebucket)
---

# Anidea for SmartThings (Anidea-ST)

A repository of assorted SmartThings bits and bobs that were created for use within the owner's personal SmartThings environment, but hopefully to a standard that could potentially make them useful to others.

**Please be aware that this is very much a personal repository, and so can be subject to the most trivial of changes. However, as a number of users have forked the master repository, a private branch has been provided to create a bit of a buffer.**

The repository layout is compatible with the IDE for the 'classic' Device Handlers and SmartApps that are written in Groovy. In the absence of any convention, or even consistent terminology, this structure has been extended for Automations using the top level folder 'automations'.
 
- [Anidea for Groovy Device Handlers](devicetypes/orangebucket/README.md)
  - [Anidea for Lumi Devices](devicetypes/orangebucket/README.md#anidea-for-lumi-devices)
    - Anidea for Aqara Button
    - Anidea for Aqara Contact
    - Anidea for Aqara Motion
    - Anidea for Aqara Temperature
    - Anidea for Aqara Vibration
    - Anidea for Mijia Contact
  - [Anidea for Virtual Devices](devicetypes/orangebucket/README.md#anidea-for-virtual-devices)
    - Anidea for Virtual Binary
    - Anidea for Virtual Button
    - Anidea for Virtual Momentary
    - Anidea for Virtual Presence
    - Anidea for Virtual Temperature
  - [Anidea for Odds and Sods](devicetypes/orangebucket/README.md#anidea-for-odds-and-sods)
    - Anidea for HTTP Ping
    - Anidea for Scene Momentary
    - LAN MultiThing
  - [Anidea for Automations](automations/orangebucket)
    - Anidea for WebHook Wrapper
    - Bucket
    - SmartThings Capabilities

A number of custom capabilities are being used with the various device handlers. They are being placed in a [capabilities](capabilities) folder with the namespace adding an extra level of hierarchy.

- [Custom Capabilities (circlemusic21301)](capabilities/circlemusic21301/)

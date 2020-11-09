#### &copy; Graham Johnson (orangebucket)
---
# Device Handler UI Metadata
The SmartThings 'Classic' app builds a custom UI for a device using the `tiles()` method of the Groovy Device Handlers (DTH). 
As the 'new' style C2C and direct integrations don’t use these device handlers, the UI for those in the Classic app tends to be non-existent. 
The ‘new’ app, a.k.a. the 'Connect' or 'One' app, does things differently, being guided by the device metadata to access bespoke device presentations,
and so it can have the reverse problem where Groovy Device Handlers don't have an effective UI in the 'new' app.

## Device Handler Definitions - `definition()`
A Groovy Device Handler (DTH) has a `metadata()` method that includes a `definition()` method that takes a map of parameters. These parameters include
metadata used for defining the UI in the 'new' app. It seems that some of this metadata may be overloaded by metadata in a matching `fingerprint`.

### Device Handler Name - `name`
The `name` of the device handler has fairly obvious uses and is defined in the DTH as e.g. `name: 'Zigbee Switch'`. Apart from contributing to uniquely identifying a device, it has no obvious bearing on the UI.

### Namespace - `namespace`
The `namespace` is typically a GitHub username but needn't be. The Groovy IDE is linked in with GitHub and the `namespace` is part of the folder structure.
In a DTH it is defined as e.g. `namespace: 'smartthings'`. Again, apart from contributing to uniquely identifying a device, it has no obvious bearing on the UI.

A different 'namespace' is used with *Custom Capabilities*. It is a catenation of two words and a number and is automatically generated for each user account.
 
### Author - `author`
The author parameter is just what it seems, and has no bearing on the UI. An example would be `author: 'Smart Stuff'`. There isn’t any obvious bearing on the UI.

### Device Type - `ocfDeviceType`
When managing *Device Profiles*, the Developer Workspace lets you specify device types from a limited number of options and the one chosen is used to define the icon in the *[Device Presentation](#device-config-and-device-presentation)*. 
In a DTH the device type is defined using an OCF Device Type  e.g. `ocfDeviceType: 'oic.d.thermostat'` and the presentations used don't seem to define the device icon directly.

The following list of device types was extracted from the Developer Workspace on 5th July 2020.

Device Type|ocfDeviceType
:---|:---
Air Conditioner|oic.d.airconditioner
Air Purifier|oic.d.airpurifier
Air Quality Detector|x.com.st.d.airqualitysensor
Blind|oic.d.blind
Blu-ray Player|x.com.st.d.blurayplayer
Camera|oic.d.camera
Contact Sensor|x.com.st.d.sensor.contact
Cooktop|x.com.st.d.cooktop
Dishwasher|oic.d.dishwasher
Door Bell|x.com.st.d.doorbell
Dryer|oic.d.dryer
Elevator|x.com.st.d.elevator
Fan|oic.d.fan
Garage Door|oic.d.garagedoor
Gas Valve|x.com.st.d.gasvalve
Health Tracker|x.com.st.d.healthtracker
Hub|x.com.st.d.hub
Humidifier|x.com.st.d.humidifier
IR Remote|x.com.st.d.irblaster
Irrigation|x.com.st.d.irrigation
Leak Sensor|x.com.st.d.sensor.moisture
Light|oic.d.light
Motion Sensor|x.com.st.d.sensor.motion
MultiFunctional Sensor|x.com.st.d.sensor.multifunction
Network Audio|oic.d.networkaudio
Others|oic.wk.d
Oven|oic.d.oven
Presence Sensor|x.com.st.d.sensor.presence
Refrigerator|oic.d.refrigerator
Remote Controller|x.com.st.d.remotecontroller
Robot Cleaner|oic.d.robotcleaner
Siren|x.com.st.d.siren
Smart Lock|oic.d.smartlock
Smart Plug|oic.d.smartplug
Smoke Detector|x.com.st.d.sensor.smoke
Stove|x.com.st.d.stove
Switch|oic.d.switch
Television|oic.d.tv
Thermostat|oic.d.thermostat
Vent|x.com.st.d.vent
Voice Assistance|x.com.st.d.voiceassistance
Washer|oic.d.washer
Water Heater|x.com.st.d.waterheater
Water Valve|oic.d.watervalve
WiFi Router|oic.d.wirelessrouter
Wine Cellar|x.com.st.d.winecellar

### Manufacturer Name - `mnmn`
The *Manufacturer Name* seems to be either a brand registered in the SmartThings API, or the four character organisation ID 
that will be familiar for those that have registered to use the Developer Workspace. The one used for stock handlers is 
`SmartThings`, and it appears in the DTH as `mnmn: 'SmartThings'`. The one used for community handlers is `SmartThingsCommunity`.

The replacement key `manufacturerName` appeared the API c. September 2020 alongside `mnmn`. At the time of writing it isn't clear if
this is also intended to be used in the DTH.

### Presentation ID - `vid`
The Developer Workspace does not seem to have been created with community developers in mind and that is reflected in some of the terminology used.
The Device Profile defined a unique *Vendor ID* string (that is to say, unique for the developer or organisation). A combination of the *Manufacturer Name*
and the *Vendor ID* identified the *UI Manifest* to be used for a device. When the VID Selector tool was released the terms had morphed into *Visualization Identifier*
and *Presentation Resource*. With the introduction of the CLI and Custom Capabilities the term *Device Presentation* came along, and then in September 2020 the key 
`presentationId` replaced `vid` as the preferred term in the API.

The *Presentation ID* appears in the DTH as e.g. `vid: 'generic-switch'` and works in combinationn with the `mnmn`. For community generated presentations the *VID* 
(as it is often known) takes the form of a UUID and works alongside the manufacturer name `SmartThingsCommunity`.

*As something of an aside, the VID Selector tool reveals that stock device handlers without an explicit VID still seem to have bespoke presentations with identifiers
built from the name (with spaces replaced with underscores), the namespace, and the mnmn in the combination mnmn-namespace-name, 
e.g. `SmartThings-smartthings-Ikea_Button`.*

## Device Config and Device Presentation
When a *Device Profile* is created in the Developer Workspace, it automatically creates a JSON file defining the UI based on the supplied device type, capabilities, 
the choice of tile status (default is the online / offline status) and tile action (default none). 
This file, which can be replaced by a custom version, used to be called the *UI Manifest'* and was named using the developer or organisation ID and the vendor ID 
e.g. `12AB_large-widget_ui.json`. A similar file referenced by the VID Selector tool was called a *Presentation Resource*.

When the CLI tool was introduced the terms *Device Config* and *Device Presentation* appeared and the terms *Config* and *Presentation* are being retrofitted to the
Device Profiles tool in the Developer Workspace to make things more consistent. 

The *Device Presentation* is a JSON file, typically around 100 kb in size,
that defines the UI for the 'new' app, or indeed any other UI. It is the same thing as the *UI Manifest* or *Presentation Resource*, though those generated for
DTHs don't define the device icon at the time of writing.

The *Device Config* is a much smaller JSON file that defines the user configurable parts of the *Device Presentation*. It defines which capabilities should
be used for the dashboard status, dashboard actions, the details view, automation conditions and automation actions. Each capability, standard or custom, has a
*Capability Presentation* that defines how the capability should be presented in each of those places. The *Device Config* is fleshed out with the *Capability 
Presentations* and multi-language support to create the *Device Presentation*.

Each *Config* and *Presentation* includes the *Manufacturer Name* and *Presentation ID*, originally as `mnmn` and `vid` but from September 2020 also as `manufacturerName`
and `presentationId`. In the API the values match, but local copies of the *Config* may have an older ID if they have been edited.

.

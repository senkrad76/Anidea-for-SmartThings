#### [Anidea for SmartThings](../../../README.md) > [Anidea for Odds and Sods](../README.md#anidea-for-odds-ands-sods) - (C) Graham Johnson (orangebucket)
---

# Anidea for HTTP Ping
A light in a room is switched automatically by a motion sensor at certain times of day. Very occasionally the room may also be occupied at those times and it would be a nuisance if the lights kept turning off because the occupants were watching the TV and not moving about. If it were possible to detect if the TV is switched on then the automation could keep the lights on. Given the automation is working with a motion sensor it is likely to be able to handle a second one. Therefore a device handler which treats the TV being on as active motion would be rather handy.

This simple device handler was built for the job described above, but as well as being able to act as a virtual Motion Sensor, it can also be set to be a Contact Sensor, Occupancy Sensor, Presence Sensor and Switch and it behaves like the [Anidea for Virtual Binary](#anidea-for-virtual-binary) handler in having a single overall active or inactive state. Every fifteen minutes it attempts to connect an HTTP server on the IP address, port and path defined in the preferences. If the `parse()` command picks up the response the status is set to active. If no response is received within a minute the status is set to inactive. The `refresh()` command can also be used to check the status out of band, and custom setter commands can be used to set the attributes directly (see [Anidea for Virtual Binary](#anidea-for-virtual-binary) for the commands, though bear in mind the Water Sensor capability is not being used).

*Polling more frequently than every fifteen minutes just seems like 'a bad thing', but it will probably be made configurable at some stage.*

*A number of users claimed that, if Smart Lighting was configured with multiple motion sensors, they didn't 'or' together when it came to inactivity timeouts. Several tests and months of usage suggested this was not the case. Unfortunately things seem to have changed and now the second motion sensor being active will not prevent inactivity timeouts.*

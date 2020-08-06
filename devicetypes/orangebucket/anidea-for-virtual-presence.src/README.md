#### [Anidea for SmartThings](../../../README.md) > [Anidea for Virtual Devices](../../../README.md#anidea-for-virtual-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Virtual Presence
The Simulated Presence Sensor doesn't allow for the Occupancy Sensor capability used in mobile presence. This handler supports both the Presence Sensor and Occupancy Sensor capabilities independently, and supports the `arrived()` and `departed()` custom commands to set presence, and uses `occupied()` and `unoccupied()` for occupancy. The [Anidea for Virtual Binary](../anidea-for-virtual-binary.src/) handler is an alternative if you just want presence, or want to link presence and occupancy together.

#### [Anidea for SmartThings](../../../README.md) > [Anidea for Lumi Devices](../README.md#anidea-for-lumi-devices) - (C) Graham Johnson (orangebucket)
---

# Anidea for Aqara Temperature
<img src="https://raw.githubusercontent.com/orangebucket/Anidea-for-SmartThings/master/images/afat_details.jpg" align="right" width="100">This supports the Aqara temperature and humidity sensor, model WSDCGQ11LM. The 'bspranger' handler extracted the atmospheric pressure but never gave it an attribute. It now uses the proposed Atmospheric Pressure Measurement capability with the `atmosphericPressure` attribute. The new app can now work with this in Automations as well as on the device pages, but it might not yet be recognised by the Developer Workspace (it hasn't been checked for a while).

The capability definition only includes the single unit 'kPa', with a range of 0 to 110, and the app only seemed to be able to display integer values when last checked (displaying zero rather than truncating). This is pretty useless as 1 kPa is the equivalent of 7.5 mmHg or 10 mbar. The device itself is specified from 30 kPa to 110 kPa, with a precision of 0.12 kPa, and seems to return units of 0.01 kPa. The combination of the capability and its implementation in the app does not really seem to be fit for purpose. Currently the handler ignores the letter of the capability and offers a choice of 'kPa', 'hPa', 'mbar', 'mmHg', or 'inHg'. However it is rather obliged to respect the display problem and only use integer values, making 'kPa' and 'inHg' pretty useless.

*The units are displayed incorrectly in the Settings area of the app because the first letter is being folded to upper case, turning correct abbreviations such as 'hPa' into the nonsensical 'HPa'.*

At the time of writing, the alpha release of the CLI tool has been used to create a custom device presentation so the temperature displays on the dashboard tile. Without it the humidity seems to display. It is assumed this works for other users.

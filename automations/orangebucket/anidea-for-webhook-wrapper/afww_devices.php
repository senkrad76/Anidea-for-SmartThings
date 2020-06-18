<?php
/* ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * ---------------------------------------------------------------------------------
 *
 * Anidea for WebHook Wrapper (afww_devices.php)
 * =============================================
 * Version: 20.06.18.00
 */

function afww_devices_getdescription( $deviceid, $authtoken )
{
    return afww_curl_api_get( "/devices/$deviceid", $authtoken );
}

function afww_devices_getfullstatus( $deviceid, $authtoken )
{
    return afww_curl_api_get( "/devices/$deviceid/status" );
}

?>

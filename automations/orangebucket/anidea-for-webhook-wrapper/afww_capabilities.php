<?php
/* ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * ---------------------------------------------------------------------------------
 *
 * Anidea for WebHook Wrapper (afww_capabilities.php)
 * ==================================================
 * Version: 20.06.18.00
 */

function afww_capabilities_list( $authtoken )
{
    return afww_curl_api_get( '/capabilities', $authtoken );
}

function afww_capabilities_get( $capid, $capversion, $authtoken )
{
    return afww_curl_api_get( "/capabilities/$capid/$capversion", $authtoken );
}
?>

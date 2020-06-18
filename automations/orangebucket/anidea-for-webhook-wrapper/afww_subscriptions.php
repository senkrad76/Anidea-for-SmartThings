<?php
/* ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * ---------------------------------------------------------------------------------
 *
 * Anidea for WebHook Wrapper (afww_subscriptions.php)
 * ===================================================
 * Version: 20.06.18.00
 */

function afww_subscriptions_create( $appid, $authtoken, $sub )
{
    return afww_curl_api_post( "/installedapps/$appid/subscriptions", $authtoken, $sub );
}

function afww_subscriptions_deleteall( $appid, $authtoken )
{
    return afww_curl_api_delete( "/installedapps/$appid/subscriptions", $authtoken );
}

function afww_subscriptions_list( $appid, $authtoken )
{       
    return afww_curl_api_get( "/installedapps/$appid/subscriptions", $authtoken );
}
?>

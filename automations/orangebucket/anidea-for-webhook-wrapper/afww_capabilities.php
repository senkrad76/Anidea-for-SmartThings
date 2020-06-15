<?php
/* ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * ---------------------------------------------------------------------------------
 *
 * Anidea for WebHook Wrapper (afww_capabilities.php)
 * ==================================================
 * Version: 20.06.15.00
 */

function afww_capabilities_list( $authtoken )
{
    $ch = curl_init( 'https://api.smartthings.com/v1/capabilities' );
    
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1 );
    curl_setopt( $ch, CURLOPT_POST,           0 );
    curl_setopt( $ch, CURLOPT_FAILONERROR,    1 );
    curl_setopt( $ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );

    $capabilities = curl_exec( $ch );

    curl_close( $ch );

    return $capabilities;
}

function afww_capabilities_get( $capid, $capversion, $authtoken )
{
    $ch = curl_init( "https://api.smartthings.com/v1/capabilities/$capid/$capversion" );
    
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1 );
    curl_setopt( $ch, CURLOPT_POST,           0 );
    curl_setopt( $ch, CURLOPT_FAILONERROR,    1 );
    curl_setopt( $ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );

    $capability =  curl_exec( $ch );

    curl_close( $ch );
    
    return $capability;
}
?>

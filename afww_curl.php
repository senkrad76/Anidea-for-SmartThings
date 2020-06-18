<?php
/* ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * ---------------------------------------------------------------------------------
 *
 * Anidea for WebHook Wrapper (afww_curl.php)
 * ==========================================
 * Version: 20.06.18.00
 */

$api = 'https://api.smartthings.com/v1';

function afww_curl_api_delete( $path, $authtoken )
{
    global $api;
    
    $ch = curl_init( $api . $path );
    
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST,   'DELETE');
    curl_setopt( $ch, CURLOPT_FAILONERROR,    true );
    curl_setopt( $ch, CURLOPT_HTTPHEADER,     array( "Authorization: Bearer $authtoken" ) );
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );

    $delete = curl_exec( $ch );

    curl_close($ch);
    
    return json_decode( $delete, true);
}

function afww_curl_api_get( $path, $authtoken )
{
    global $api;
    
    $ch = curl_init( $api . $path );
    
    curl_setopt( $ch, CURLOPT_FAILONERROR,    true );
    curl_setopt( $ch, CURLOPT_HTTPHEADER,     array( "Authorization: Bearer $authtoken" ) );
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );

    $get = curl_exec( $ch );

    curl_close( $ch );

    return json_decode( $get, true );
}

function afww_curl_api_post( $path, $authtoken, $data )
{
    global $api;
    
    $ch = curl_init( $api . $path );
    
    curl_setopt( $ch, CURLOPT_FAILONERROR,    true );
    curl_setopt( $ch, CURLOPT_HTTPHEADER,     array( "Authorization: Bearer $authtoken" ) );
    curl_setopt( $ch, CURLOPT_POSTFIELDS,     json_encode( $data ) );
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true);

    $post = curl_exec( $ch );
    
    curl_close($ch);
    
    return json_decode( $post, true);
}

?>

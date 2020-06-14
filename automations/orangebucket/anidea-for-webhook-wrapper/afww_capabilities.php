<?php
//
// Anidea-ST Webhook Library (afswl_capabilities.php) - (C) Graham Johnson 2020
// ============================================================================
// Version: 20.06.13.00
//

function afswl_capabilities_list( $authtoken )
{
    $ch = curl_init( 'https://api.smartthings.com/v1/capabilities' );
    
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1 );
    curl_setopt( $ch, CURLOPT_POST,           0 );
    curl_setopt( $ch, CURLOPT_FAILONERROR,    1 );
    curl_setopt( $ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );

    $capabilities = curl_exec( $ch );
if( $capabilities === false)
{
    error_log( curl_error($ch) );
}
    curl_close( $ch );

    return $capabilities;
}
?>

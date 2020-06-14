<?php
//
// Anidea for WebHook Wrapper (afww_capabilities.php) - (C) Graham Johnson 2020
// ============================================================================
// Version: 20.06.14.00
//

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
?>

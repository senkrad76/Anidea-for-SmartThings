<?php
//
// Anidea-ST Webhook Library (afswl_devices.php) - (C) Graham Johnson 2020
// =======================================================================
// Version: 20.06.13.00
//

function afswl_devices_getdescription( $deviceid, $authtoken )
{
    $ch = curl_init( "https://api.smartthings.com/v1/devices/$deviceid" );

    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $desc = json_decode( curl_exec( $ch ), true );
    curl_close($ch);
    
    afswl_log_asjson( $desc, 'DEVICES_GETDESCRIPTION' );
    
    return $desc;
}

function afswl_devices_getfullstatus( $deviceid, $authtoken )
{
    $ch = curl_init( "https://api.smartthings.com/v1/devices/$deviceid/status" );

    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $status = json_decode( curl_exec( $ch ), true );
    curl_close($ch);
    
    afswl_log_asjson( $status, 'DEVICES_GETFULLSTATUS' );
    
    return $status;
}

?>

<?php
//
// Anidea for WebHook Wrapper (afww_devices.php) - (C) Graham Johnson 2020
// =======================================================================
// Version: 20.06.14.00
//

function afww_devices_getdescription( $deviceid, $authtoken )
{
    $ch = curl_init( "https://api.smartthings.com/v1/devices/$deviceid" );

    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $desc = json_decode( curl_exec( $ch ), true );
    curl_close($ch);
    
    afww_log_asjson( $desc, 'DEVICES_GETDESCRIPTION' );
    
    return $desc;
}

function afww_devices_getfullstatus( $deviceid, $authtoken )
{
    $ch = curl_init( "https://api.smartthings.com/v1/devices/$deviceid/status" );

    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $status = json_decode( curl_exec( $ch ), true );
    curl_close($ch);
    
    afww_log_asjson( $status, 'DEVICES_GETFULLSTATUS' );
    
    return $status;
}

?>

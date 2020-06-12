<?php
//
// Anidea-ST Webhook Library (subscriptions.php) - (C) Graham Johnson 2020
// =======================================================================
// Version: 20.06.12.00
//

function afswl_subscriptions_create( $appid, $authtoken, $sub )
{
    $ch = curl_init( "https://api.smartthings.com/v1/installedapps/$appid/subscriptions" );

    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode( $sub ) );
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec( $ch );
    curl_close($ch);
    
    afswl_log_asjson( $sub, 'SUBSCRIPTION_REQUEST' );
}

function afswl_subscriptions_deleteall( $appid, $authtoken )
{
    $ch = curl_init( "https://api.smartthings.com/installedapps/$appid/subscriptions" );
    
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "DELETE");
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec( $ch );
    curl_close($ch);
}

function afswl_subscriptions_list( $appid, $authtoken )
{       
    $ch = curl_init( "https://api.smartthings.com/installedapps/$appid/subscriptions" );
    
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $subs = curl_exec( $ch );
    curl_close($ch);
    
    afswl_log_asjson( json_decode( $subs, true), 'SUBSCRIPTION_LIST' );
}
?>

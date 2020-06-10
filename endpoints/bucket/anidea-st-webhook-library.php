<?php
require_once 'st-webhook-library.php';

//
// Anidea-ST Webhook Library (anidea-st-webhook-library.php) - (C) Graham Johnson 2020
// ===================================================================================
// Version: 20.06.10.00
//

function log_asjson( $data, $logname, $logpath = './logs' )
{
    error_log( json_encode( $data, JSON_PRETTY_PRINT ) . "\n", 3, "$logpath/$logname.json");
}

function lifecycle( $request, $appconfig, $logpath = './logs' )
{
    // Log the current request.
    log_asjson( $request, $request[ 'lifecycle' ] );
    
    // Check for lifecycle events from SmartThings.
    switch ( $request[ 'lifecycle' ] )
    {
        case 'CONFIRMATION':    $response = lifecycle_confirmation( $request, $logpath );
                                break;
        case 'CONFIGURATION':   $response = lifecycle_configuration( $request, $logpath );
                                break;
        case 'INSTALL':         $response = lifecycle_install( $request, $logpath );
                                break;
        case 'UPDATE':          $response = lifecycle_update( $request, $logpath );
                                break;
        case 'EVENT':           $response = lifecycle_event( $request, $logpath );
                                break;
        case 'UNINSTALL':       $response = lifecycle_event( $request, $logpath );
                                break;
        default:                $response = false;
    }
    
    return $response;
}

function lifecycle_confirmation( $request, $logpath = './logs' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#CONFIRMATION
    
    // Create the required response.
    $response = array( 'target_url' => 'https://' . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'] );
    
    // Send a GET request to the supplied confirmation URL.
    file_get_contents( $request[ 'confirmationData' ][ 'confirmationUrl' ] );
    
    return $response;
}

function lifecycle_configuration( $request, $logpath = './logs' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#CONFIGURATION
    
    switch ( $request[ 'configurationData' ][ 'phase' ] )
    {
        case 'INITIALIZE':  $response = lifecycle_configuration_initialize( $logpath );
                            break;
        case 'PAGE':        $response = lifecycle_configuration_page( $request[ 'configurationData' ][ 'pageId' ], $logpath );
                            break;
    }
    
    return $response;
}

function lifecycle_configuration_initialize( $logpath = './logs')
{
    // Requires the user defined function config_app().
    
    $response = array( 'configurationData' => array( 'initialize' => config_app() ) );
                
    log_asjson( $response, 'CONFIGURATION_INITIALIZE_RESPONSE' );
    
    return( $response );
}

function lifecycle_configuration_page( $pageid, $logpath = './logs' )
{
    $pages = config_pages();
    
    $page = array( 'configurationData' => array( 'page' => $pages[ $pageid ] ) );
    
    log_asjson( $page, 'CONFIGURATION_PAGE_RESPONSE' );
    
    return $page;
}

function lifecycle_install( $request, $logpath = './logs' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#INSTALL
    
    $response = array( 'installData' => array( 'placeholder' => '' ) );

    return $response;
}

function lifecycle_update( $request, $logpath = './logs' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#UPDATE
    
    $response = array( 'updateData' => array( 'placeholder' => '' ) );
    
    // https://smartthings.developer.samsung.com/docs/smartapps/subscriptions.html
    
    $appid     = $request[ 'updateData' ][ 'installedApp' ][ 'installedAppId' ];
    $authtoken = $request[ 'updateData' ][ 'authToken' ];
    $config    = $request[ 'updateData' ][ 'installedApp' ][ 'config' ];
    
    // Delete the existing subscriptions to avoid setting up duplicates.
    subscriptions_deleteall( $appid, $authtoken );

    // Create new subscriptions.
    subscriptions_subscribe( $appid, $authtoken, $config, $logpath );
    
    // List the current subscriptions.
    subscriptions_list( $appid, $authtoken, $logpath );
    
    return $response;
}

function subscriptions_deleteall( $appid, $authtoken )
{
    $ch = curl_init( "https://api.smartthings.com/installedapps/$appid/subscriptions" );
    
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "DELETE");
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec( $ch );
    curl_close($ch);
}

function subscriptions_subscribe( $appid, $authtoken, $config, $logpath = './logs' )
{
    // Requires the user defined function config_subscriptions().
    
    $subs = config_subscriptions( $appid, $authtoken, $config, $logpath );
    
    foreach ( $subs as $sub )
    {
        $ch = curl_init( "https://api.smartthings.com/installedapps/$appid/subscriptions" );

        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode( $sub ) );
        curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

        $result = curl_exec( $ch );
        curl_close($ch);
    
        log_asjson( $sub, 'SUBSCRIPTION_REQUEST' );
    }
}

function subscriptions_list( $appid, $authtoken, $logpath = './logs' )
{       
    $ch = curl_init( "https://api.smartthings.com/installedapps/$appid/subscriptions" );
    
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $subs = curl_exec( $ch );
    curl_close($ch);
    
    log_asjson( json_decode( $subs ), 'SUBSCRIPTION_LIST' );
}

function lifecycle_event( $request, $logpath = './logs' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#EVENTS
    
    $response = array( 'eventData' => array( 'placeholder' => '' ) );
    
    return $response;
}
?>

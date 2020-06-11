<?php
//
// Anidea-ST Webhook Library (anidea-st-webhook-library.php) - (C) Graham Johnson 2020
// ===================================================================================
// Version: 20.06.11.01
//

function afswl_log_asjson( $data, $logname )
{
    // Requires the user defined function afswl_config_log().
    
    if ( $logpath = afswl_config_log() )
    {
        error_log( json_encode( $data, JSON_PRETTY_PRINT ) . "\n", 3, "$logpath/$logname.json");
    }
}

function afswl_log_astext( $data, $logname )
{
    // Requires the user defined function afswl_config_log().
    
    if ( $logpath = afswl_config_log() )
    {
        error_log( date( DATE_ISO8601 ) . ': ' . $data . "\n", 3, "$logpath/$logname.json");
    }
}

function afswl_main( $scripturl )
{
    // Read data from the request body, assuming it is JSON.
    if ( $request = json_decode( file_get_contents( 'php://input' ), true ) )
    {
        if ( $response = afswl_lifecycle( $request, $scripturl ) )
        {
            header( 'Content-Type: application/json' );
            echo json_encode( $response );
        }
    }
    else
    {
        // This is just a bog standard HTTP GET call so put out a web page.
        
        echo afswl_config_main();
    }
}

function afswl_lifecycle( $request, $scripturl )
{
    // Log the current request.
    afswl_log_asjson( $request, $request[ 'lifecycle' ] );
    
    // Check for afswl_lifecycle events from SmartThings.
    switch ( $request[ 'lifecycle' ] )
    {
        case 'CONFIRMATION':    $response = afswl_lifecycle_confirmation( $request, $scripturl );
                                break;
        case 'CONFIGURATION':   $response = afswl_lifecycle_configuration( $request );
                                break;
        case 'INSTALL':         $response = afswl_lifecycle_install( $request );
                                break;
        case 'UPDATE':          $response = afswl_lifecycle_update( $request );
                                break;
        case 'EVENT':           $response = afswl_lifecycle_event( $request );
                                break;
        case 'UNINSTALL':       $response = afswl_lifecycle_event( $request );
                                break;
        default:                $response = false;
    }
    
    return $response;
}

function afswl_lifecycle_confirmation( $request, $scripturl )
{
    // Create the required response.
    $response = array( 'target_url' => $scripturl );
    
    // Send a GET request to the supplied confirmation URL.
    file_get_contents( $request[ 'confirmationData' ][ 'confirmationUrl' ] );
    
    return $response;
}

function afswl_lifecycle_configuration( $request )
{
    switch ( $request[ 'configurationData' ][ 'phase' ] )
    {
        case 'INITIALIZE':  $response = afswl_lifecycle_configuration_initialize( $logpath );
                            break;
        case 'PAGE':        $response = afswl_lifecycle_configuration_page( $request[ 'configurationData' ][ 'pageId' ] );
                            break;
    }
    
    return $response;
}

function afswl_lifecycle_configuration_initialize( $logpath = './logs')
{
    // Requires the user defined function afswl_config_initialize().
    
    $response = array( 'configurationData' => array( 'initialize' => afswl_config_initialize() ) );
                
    afswl_log_asjson( $response, 'CONFIGURATION_INITIALIZE_RESPONSE' );
    
    return( $response );
}

function afswl_lifecycle_configuration_page( $pageid )
{
    // Requires the user defined function afswl_config_page().
    $pages = afswl_config_page();
    
    $page = array( 'configurationData' => array( 'page' => $pages[ $pageid ] ) );
    
    afswl_log_asjson( $page, 'CONFIGURATION_PAGE_RESPONSE' );
    
    return $page;
}

function afswl_lifecycle_install( $request )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#INSTALL
    
    $response = array( 'installData' => array( 'placeholder' => '' ) );

    return $response;
}

function afswl_lifecycle_update( $request )
{
    $response = array( 'updateData' => array( 'placeholder' => '' ) );
    
    $appid     = $request[ 'updateData' ][ 'installedApp' ][ 'installedAppId' ];
    $authtoken = $request[ 'updateData' ][ 'authToken' ];
    $config    = $request[ 'updateData' ][ 'installedApp' ][ 'config' ];
    
    // Delete the existing subscriptions to avoid setting up duplicates.
    afswl_subscriptions_deleteall( $appid, $authtoken );

    // Create new subscriptions.
    afswl_subscriptions_subscribeconfig( $appid, $authtoken, $config );
    
    // List the current subscriptions.
    afswl_subscriptions_list( $appid, $authtoken );
    
    return $response;
}

function afswl_subscriptions_subscribeconfig( $appid, $authtoken, $config )
{
    // Requires the user defined function afswl_config_subscription().
    
    $subs = afswl_config_subscription( $config );
    
    foreach ( $subs as $sub )
    {
        afswl_subscriptions_create( $appid, $authtoken, $sub );
    }
}

function afswl_lifecycle_event( $request )
{
    // Requires the user defined function afswl_config_event().
    
    $response = array( 'eventData' => array( 'placeholder' => '' ) );
    
    afswl_config_event( $request );
    
    return $response;
}

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

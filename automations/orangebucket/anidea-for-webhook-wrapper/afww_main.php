<?php
require_once 'afww_capabilities.php';
require_once 'afww_devices.php';
require_once 'afww_subscriptions.php';

//
// Anidea for WebHook Wrapper (afww_main.php) - (C) Graham Johnson 2020
// ====================================================================
// Version: 20.06.14.00
//

function afww_log_asjson( $data, $logname )
{
    // Requires the user defined function afww_config_log().
    
    if ( $logpath = afww_config_log() )
    {
        error_log( json_encode( $data, JSON_PRETTY_PRINT ) . "\n", 3, "$logpath/$logname.json");
    }
}

function afww_log_astext( $data, $logname )
{
    // Requires the user defined function afww_config_log().
    
    if ( $logpath = afww_config_log() )
    {
        error_log( date( DATE_ISO8601 ) . ': ' . $data . "\n", 3, "$logpath/$logname.json");
    }
}

function afww_main( $scripturl )
{
    // Read data from the request body, assuming it is JSON.
    if ( $request = json_decode( file_get_contents( 'php://input' ), true ) )
    {
        if ( $response = afww_lifecycle( $request, $scripturl ) )
        {
            header( 'Content-Type: application/json' );
            echo json_encode( $response );
        }
    }
    else
    {
        // This is just a bog standard HTTP GET call so put out a web page.
        
        echo afww_config_main();
    }
}

function afww_lifecycle( $request, $scripturl )
{
    // Log the current request.
    afww_log_asjson( $request, $request[ 'lifecycle' ] );
    
    // Check for afww_lifecycle events from SmartThings.
    switch ( $request[ 'lifecycle' ] )
    {
        case 'CONFIRMATION':    $response = afww_lifecycle_confirmation( $request, $scripturl );
                                break;
        case 'CONFIGURATION':   $response = afww_lifecycle_configuration( $request );
                                break;
        case 'INSTALL':         $response = afww_lifecycle_install( $request );
                                break;
        case 'UPDATE':          $response = afww_lifecycle_update( $request );
                                break;
        case 'EVENT':           $response = afww_lifecycle_event( $request );
                                break;
        case 'UNINSTALL':       $response = afww_lifecycle_event( $request );
                                break;
        default:                $response = false;
    }
    
    return $response;
}

function afww_lifecycle_confirmation( $request, $scripturl )
{
    // Create the required response.
    $response = array( 'target_url' => $scripturl );
    
    // Send a GET request to the supplied confirmation URL.
    file_get_contents( $request[ 'confirmationData' ][ 'confirmationUrl' ] );
    
    return $response;
}

function afww_lifecycle_configuration( $request )
{
    switch ( $request[ 'configurationData' ][ 'phase' ] )
    {
        case 'INITIALIZE':  $response = afww_lifecycle_configuration_initialize( $logpath );
                            break;
        case 'PAGE':        $response = afww_lifecycle_configuration_page( $request[ 'configurationData' ][ 'pageId' ] );
                            break;
    }
    
    return $response;
}

function afww_lifecycle_configuration_initialize( $logpath = './logs')
{
    // Requires the user defined function afww_config_initialize().
    
    $response = array( 'configurationData' => array( 'initialize' => afww_config_initialize() ) );
                
    afww_log_asjson( $response, 'CONFIGURATION_INITIALIZE_RESPONSE' );
    
    return( $response );
}

function afww_lifecycle_configuration_page( $pageid )
{
    // Requires the user defined function afww_config_page().
    $pages = afww_config_page();
    
    $page = array( 'configurationData' => array( 'page' => $pages[ $pageid ] ) );
    
    afww_log_asjson( $page, 'CONFIGURATION_PAGE_RESPONSE' );
    
    return $page;
}

function afww_lifecycle_install( $request )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#INSTALL
    
    $response = array( 'installData' => array( 'placeholder' => '' ) );

    return $response;
}

function afww_lifecycle_update( $request )
{
    $response = array( 'updateData' => array( 'placeholder' => '' ) );
    
    $appid     = $request[ 'updateData' ][ 'installedApp' ][ 'installedAppId' ];
    $authtoken = $request[ 'updateData' ][ 'authToken' ];
    $config    = $request[ 'updateData' ][ 'installedApp' ][ 'config' ];
    
    // Delete the existing subscriptions to avoid setting up duplicates.
    afww_subscriptions_deleteall( $appid, $authtoken );

    // Create new subscriptions.
    afww_lifecycle_update_subscriptions( $appid, $authtoken, $config );
    
    // List the current subscriptions.
    afww_subscriptions_list( $appid, $authtoken );
    
    return $response;
}

function afww_lifecycle_update_subscriptions( $appid, $authtoken, $config )
{
    // Requires the user defined function afww_config_subscription().
    
    $subs = afww_config_subscription( $config );
    
    foreach ( $subs as $sub )
    {
        afww_subscriptions_create( $appid, $authtoken, $sub );
    }
}

function afww_lifecycle_event( $request )
{
    // Requires the user defined function afww_config_event().
    
    $response = array( 'eventData' => array( 'placeholder' => '' ) );
    
    afww_config_event( $request );
    
    return $response;
}
?>

<?php
//
// Bucket (bucket.php) - (C) Graham Johnson 2020
// =============================================
// Version: 20.06.10.00
//
// This is a test application that is being used to learn how to write
// what SmartThings seem to be calling a 'WebHook Endpoint' automation.
// It is called 'Bucket' because that was the temporary project name in
// the SmartThings Developer Workspace and for some reason the project name
// can not changed. What it actually does is likely to vary with time but
// there is a tentative intention of working towards a basic library using
// procedural programming for those who can't be doing with OOP stuff.
//
// CURRENT FUNCTIONALITY:
// Requests read and execute authorisation for a switch, and read
// authorisation for multiple buttons. Subscribes to any activity from the
// switch, and changes in the button attribute for the two components of
// the first button. Records received events in a log.
//

// START OF MAIN PROGRAM.

// Read data from the request body, assuming it is JSON.
if ( $request = json_decode( file_get_contents( 'php://input' ), true ) )
{
    if ( $response = lifecycle( $request ) )
    {
        header( 'Content-Type: application/json' );
        echo json_encode( $response );
    }
}
else
{
    // This is just a bog standard HTTP GET call.
?>
<!DOCTYPE html>
<html lang="en-gb">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>Bucket</title>
    </head>
    <body>
        <h1>Bucket</h1>
    </body>
</html>
<?php
}

// END OF MAIN PROGRAM.

function log_asjson( $data, $logname, $logpath = './logs' )
{
    error_log( json_encode( $data, JSON_PRETTY_PRINT ) . "\n", 3, "$logpath/$logname.json");
}

function lifecycle( $request, $logpath = './logs' )
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
        case 'INITIALIZE':  $response = lifecycle_configuration_initialize();
                            break;
        case 'PAGE':        $response = lifecycle_configuration_page();
                            break;
    }
    
    return $response;
}

function lifecycle_configuration_initialize( $logpath = './logs')
{
    $response = array( 'configurationData' => array( 'initialize' => array( 'name' => 'Bucket',
                                                                            'description' => 'A Bucket Of Oranges',
                                                                            'id' => 'bucket',
                                                                            'permissions' => [],
                                                                            'firstPageId' => '1'
                                                                         )
                                                  )
                    );
                
    log_asjson( $response, 'CONFIGURATION_INITIALIZE_RESPONSE' );
    
    return( $response );
}

function lifecycle_configuration_page( $logpath = './logs' )
{
    // Only handling a single page of configuration for the moment.
        
    $response = array( 'configurationData' => array( 'page' => array( 'pageId' => '1',
                                                                      'name' => 'Configuring the bucket of oranges',
                                                                      'nextPageId' => null,
                                                                      'previousPageId' => null,
                                                                      'complete' => true,
                                                                      'sections' => [ array( 'name' => 'A light and a button',
                                                                                             'settings' =>  [ array( 'id'           => 'thelight',
                                                                                                                     'name'         => 'Which light?',
                                                                                                                     'description'  => 'Bla, bla',
                                                                                                                     'type'         => 'DEVICE',
                                                                                                                     'required'     => true,
                                                                                                                     'multiple'     => false,
                                                                                                                     'capabilities' => [ 'switch' ],
                                                                                                                     'permissions'  => [ 'r', 'x' ]
                                                                                                              ),
                                                                                                              array( 'id'           => 'thebutton',
                                                                                                                     'name'         => 'Which buttons?',
                                                                                                                     'description'  => 'Bla, bla',
                                                                                                                     'type'         => 'DEVICE',
                                                                                                                     'required'     => true,
                                                                                                                     'multiple'     => true,
                                                                                                                     'capabilities' => [ 'button' ],
                                                                                                                     'permissions'  => [ 'r' ]
                                                                                                                   )
                                                                                                             ]
                                                                                           )
                                                                                    ]
                                                               )
                                              ) 
                );

    log_asjson( $response, 'CONFIGURATION__PAGE_RESPONSE' );

    return $response;
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
    $url       = "https://api.smartthings.com/installedapps/$appid/subscriptions";
    
    // Delete the existing subscriptions to avoid setting up duplicates.
    
    $ch = curl_init( $url );
    
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "DELETE");
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec( $ch );
    curl_close($ch);

    $ch = curl_init( $url );
    
    $device = $request[ 'updateData' ][ 'installedApp' ][ 'config' ][ 'thelight' ][ 0 ][ 'deviceConfig' ];

    $payload = array( 'sourceType'  => 'DEVICE',
                                   'device'      => array( 'deviceId'    => $device[ 'deviceId' ],
                                                           'componentId' => $device[ 'componentId' ],
                                                           'capability'  => '*',
                                                           'attribute'   => '*',
                                                           'value'       => '*'
                                                    )
                );

    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode( $payload) );
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec( $ch );
    curl_close($ch);
    
    log_asjson( $payload, 'SUBSCRIPTION_REQUEST' );
    
    $ch = curl_init( $url );
    
    $device = $request[ 'updateData' ][ 'installedApp' ][ 'config' ][ 'thebutton' ][ 0 ][ 'deviceConfig' ];

    $payload = array( 'sourceType'  => 'DEVICE',
                                   'device'      => array( 'deviceId'    => $device[ 'deviceId' ],
                                                           'componentId' => 'button1',
                                                           'capability'  => 'button',
                                                           'attribute'   => 'button',
                                                           'value'       => '*'
                                                    )
                );

    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode( $payload) );
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec( $ch );
    curl_close($ch);
    
    log_asjson( $payload, 'SUBSCRIPTION_REQUEST' );
    
    $ch = curl_init( $url );
    
    $device = $request[ 'updateData' ][ 'installedApp' ][ 'config' ][ 'thebutton' ][ 0 ][ 'deviceConfig' ];

    $payload = array( 'sourceType'  => 'DEVICE',
                                   'device'      => array( 'deviceId'    => $device[ 'deviceId' ],
                                                           'componentId' => 'button2',
                                                           'capability'  => 'button',
                                                           'attribute'   => 'button',
                                                           'value'       => '*'
                                                    )
                );

    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode( $payload) );
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec( $ch );
    curl_close($ch);
    
    log_asjson( $payload, 'SUBSCRIPTION_REQUEST' );
        
    $ch = curl_init( $url );
    
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $subs = curl_exec( $ch );
    curl_close($ch);
    
    log_asjson( json_decode( $subs ), 'SUBSCRIPTION_LIST' );
    
    return $response;
}

function lifecycle_event( $request, $logpath = './logs' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#EVENTS
    
    $response = array( 'eventData' => array( 'placeholder' => '' ) );
    
    return $response;
}
?>

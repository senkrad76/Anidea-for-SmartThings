<?php
//
// Bucket (bucket.php) - (C) Graham Johnson 2020
// =============================================
// Version: 20.06.08.01
//
// This is a test application that is being used to learn how to write
// what SmartThings seem to be calling a 'WebHook Endpoint' automation.
// It is called 'Bucket' because the SmartThings Developer Workspace
// doesn't let you rename projects for some reason and it seems daft to
// have an app name that doesn't match the project name. What it actually
// does is likely to vary with time.
//

// Read data from the request body, assuming it is JSON.
if ( $body = json_decode( file_get_contents( 'php://input' ), true ) )
{
    // Save the request body to a file for diagnostic purposes.
    file_put_contents( 'body.txt', print_r( $body, true ) );
}
// Otherwise give the 'lifecycle' key a value to make the code neater.
else $body = array( 'lifecycle' => 'PLACEHOLDER');

// Check for lifecycle events from SmartThings.
if ( $body[ 'lifecycle' ]  == 'CONFIRMATION' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#CONFIRMATION
    
    // Send the required response.
    $json = array( 'target_url' => 'https://' . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'] );
    header( 'Content-Type: application/json' );
    echo json_encode( $json );
    
    // Send a GET request to the supplied confirmation URL.
    file_get_contents( $body[ 'confirmationData' ][ 'confirmationUrl' ] );
}
elseif ( $body[ 'lifecycle' ]  == 'CONFIGURATION' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#CONFIGURATION
    
    if ( $body[ 'configurationData' ][ 'phase' ] == 'INITIALIZE' )
    {
        $json = array( 'configurationData' => 
                            array( 'initialize' => 
                                        array(  'name' => 'Bucket',
                                                'description' => 'A Bucket Of Oranges',
                                                'id' => 'bucket',
                                                'permissions' => [],
                                                'firstPageId' => '1'
                                        )
                            )
                );
                
        header( 'Content-Type: application/json' );
        echo json_encode( $json );
        file_put_contents( 'response.txt', json_encode( $json, JSON_PRETTY_PRINT ) );
    }
    elseif ( $body[ 'configurationData' ][ 'phase' ] == 'PAGE' )
    {
        // Only handling a single page of configuration.
        
        $json = array( 'configurationData' => 
                            array( 'page' => 
                                        array( 'pageId' => '1',
                                               'name' => 'Configuring the bucket of oranges',
                                               'nextPageId' => null,
                                               'previousPageId' => null,
                                               'complete' => true,
                                               'sections' => [ array( 'name' => 'Light to monitor',
                                                                      'settings' =>  [ array( 'id' => 'thelight',
                                                                                              'name' => 'Which light?',
                                                                                              'description' => 'Bla, bla',
                                                                                              'type' => 'DEVICE',
                                                                                              'required' => true,
                                                                                              'multiple' => false,
                                                                                              'capabilities' => [ 'switch' ],
                                                                                              'permissions' => [ 'r' ]
                                                                                       )
                                                                                     ]
                                                               )
                                                             ]
                                        )
                            ) 
                );
                
        header( 'Content-Type: application/json' );
        echo json_encode( $json );
        
        file_put_contents( 'response.txt', json_encode( $json, JSON_PRETTY_PRINT ) );
    }
}
elseif ( $body[ 'lifecycle' ]  == 'INSTALL' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#INSTALL
    
    $json = array( 'installData' => array( 'placeholder' => '' ) );
    header( 'Content-Type: application/json' );
    echo json_encode( $json );
}
elseif ( $body[ 'lifecycle' ]  == 'UPDATE' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#UPDATE
    
    $json = array( 'updateData' => array( 'placeholder' => '' ) );
    header( 'Content-Type: application/json' );
    echo json_encode( $json );
    
    // https://smartthings.developer.samsung.com/docs/smartapps/subscriptions.html
    
    $appid = $body[ 'updateData' ][ 'installedApp' ][ 'installedAppId' ];
    $authtoken = $body[ 'updateData' ][ 'authToken' ];
    $url = "https://api.smartthings.com/installedapps/$appid/subscriptions";

    $ch = curl_init($url);
    
    $device = $body[ 'updateData' ][ 'installedApp' ][ 'config' ][ 'thelight' ][ 0 ][ 'deviceConfig' ];

    $payload = json_encode( array( 'sourceType'  => 'DEVICE',
                                   'device'      => array( 'deviceId' => $device[ 'deviceId' ],
                                                           'componentId' => $device[ 'componentId' ],
                                                           'capability'  => '*',
                                                           'attribute'   => '*',
                                                           'value'       => '*'
                                                    )
                            )
                );

    curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $authtoken" ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec( $ch );
    curl_close($ch);
}
elseif ( $body[ 'lifecycle' ]  == 'EVENT' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#EVENTS
    
    $json = array( 'eventData' => array( 'placeholder' => '' ) );
    header( 'Content-Type: application/json' );
    echo json_encode( $json );
    file_put_contents( 'event.txt', json_encode( $body[ 'eventData' ][ 'events' ], JSON_PRETTY_PRINT ) );
}
elseif ( $body[ 'lifecycle' ]  == 'UNINSTALL' )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#INSTALL
    
    $json = array( 'uninstallData' => array( 'placeholder' => '' ) );
    header( 'Content-Type: application/json' );
    echo json_encode( $json );
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
?>

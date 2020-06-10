<?php
require_once 'anidea-st-webhook-library.php';

//
// Bucket (main.php) - (C) Graham Johnson 2020
// =============================================
// Version: 20.06.10.01
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

// START OF CONFIG.

$logpath = './logs';

function config_app()
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#CONFIRMATION

    $appconfig = array( 'name' => 'Bucket',
                        'description' => 'A Bucket Of Oranges',
                        'id' => 'bucket',
                        'permissions' => [],
                        'firstPageId' => '1'
                 );
                  
    return $appconfig;
}

function config_pages()
{
    // https://smartthings.developer.samsung.com/docs/smartapps/lifecycles.html#CONFIRMATION
    
    $pages[1] = array( 'pageId' => '1',
                       'name' => 'Configuring the bucket of oranges',
                       'nextPageId' => null,
                       'previousPageId' => null,
                       'complete' => true,
                       'sections' => [ array( 'name'     => 'A light and a button',
                                              'settings' => [ array( 'id'           => 'thelight',
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
                     );

    return $pages;
}

function config_subscriptions( $appid, $authtoken, $config )
{
    // https://smartthings.developer.samsung.com/docs/smartapps/subscriptions.html
        
    $deviceconfig = $config[ 'thelight' ][ 0 ][ 'deviceConfig' ];

    $subs[0] = array( 'sourceType'  => 'DEVICE',
                      'device'      => array( 'deviceId'    => $deviceconfig[ 'deviceId' ],
                                              'componentId' => $deviceconfig[ 'componentId' ],
                                              'capability'  => '*',
                                              'attribute'   => '*',
                                              'value'       => '*'
                                            )
                    );
    
    $deviceconfig = $config[ 'thebutton' ][ 0 ][ 'deviceConfig' ];

    $subs[1] = array( 'sourceType'  => 'DEVICE',
                      'device'      => array( 'deviceId'    => $deviceconfig[ 'deviceId' ],
                                              'componentId' => 'button1',
                                              'capability'  => 'button',
                                              'attribute'   => 'button',
                                              'value'       => '*'
                                            )
                    );
    
    $deviceconfig = $config[ 'thebutton' ][ 0 ][ 'deviceConfig' ];

    $subs[3] = array( 'sourceType'  => 'DEVICE',
                      'device'      => array( 'deviceId'    => $device[ 'deviceId' ],
                                              'componentId' => 'button2',
                                              'capability'  => 'button',
                                              'attribute'   => 'button',
                                              'value'       => '*'
                                            )
                    );

    return subs;
}

// END OF CONFIG.

// START OF MAIN.

// Read data from the request body, assuming it is JSON.
if ( $request = json_decode( file_get_contents( 'php://input' ), true ) )
{
    if ( $response = lifecycle( $request, $appconfig, $logpath ) )
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

// END OF MAIN.
?>

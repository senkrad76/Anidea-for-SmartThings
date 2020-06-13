<?php
// The file should begin with the PHP opening tag on the first line.
require_once '../afswl/anidea-st-webhook-library.php';

//
// Anidea-ST Webhook Library (index.php) - (C) Graham Johnson 2020
// ===============================================================
// Version: 20.06.13.00
//
// This is an example app to demonstrate use of the Anidea-ST Webhook Library.
//
// CURRENT FUNCTIONALITY:
// Requests read and execute authorisation for a switch, and read
// authorisation for multiple buttons. Subscribes to any activity from the
// switch, and changes in the button attribute for the two components of
// the first button.
//

// START OF CONFIG.

function afswl_config_log()
{
    // This function MUST return a value which should either provide a path
    // to the log folder, or a value that evaluates to false if no logs are
    // required.
    
    return './logs';
}

function afswl_config_initialize()
{
    // This function MUST return an array which corresponds to the value of the 'initialize' key
    // in the CONFIGURATION INITIALIZE phase response.
    //
    // https://smartthings.developer.samsung.com/docs/smartapps/configuration.html#INITIALIZE-phase

    $appconfig = array( 'name' => 'Anidea-ST Webhook Library Example App',
                        'description' => 'An example app provided as part of the Anidea-ST Webhook Libary.',
                        'id' => 'afswlexample',
                        'permissions' => [],
                        'firstPageId' => '1'
                 );
                  
    return $appconfig;
}

function afswl_config_page()
{
    // This function MUST return an array, and the keys MUST match the IDs of the pages.
    // Each element of the array should be a configuration page definition, which corresponds
    // to value of the 'page' key in the CONFIGURATION PAGE phase response. 
    //
    // https://smartthings.developer.samsung.com/docs/smartapps/configuration.html#PAGE-phase
    
    $page[ '1' ] = array( 'pageId' => '1',
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

    return $page;
}

function afswl_config_subscription( $config )
{
    // The library configures subscriptions in the UPDATE lifecycle. This function MUST
    // return an array, each element of which corresponds to a subscription being created.
    //
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

    return $subs;
}

function afswl_config_event( $eventpost )
{
    // This function is called when an event is received.

    // The token might be needed.
    $authtoken = $eventpost[ 'eventData' ][ 'authToken' ];

    // Get the event type and the type specific data.
    switch( $eventtype = $eventpost[ 'eventData' ][ 'events'][ 0 ][ 'eventType'] )
    {
        case 'DEVICE_EVENT':    $event       = $eventpost[ 'eventData' ][ 'events'][ 0 ][ 'deviceEvent' ];
                                $description = afswl_devices_getdescription( $event[ 'deviceId'],  $authtoken );
                                $devicename  = $description[ 'label'] ?: $description[ 'name' ];
                                afswl_log_astext( "$devicename {$event[ 'attribute' ]} {$event[ 'value' ]}", 'EVENTLIST' );
                                break;
    }
    
    // Do the app specific stuff here.
}

function afswl_config_main()
{
    // This function is called when the script is called with a GET request
    // rather than as a webhook. It should return a string which will be output
    // as the response, but if required it can output directly.
    
    return <<<ENDHTML
<!DOCTYPE html>
<html lang="en-gb">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>Anidea-ST Webhook Library Example App</title>
    </head>
    <body>
        <h1>Anidea-ST Webhook Library Example App</h1>
    </body>
</html>
ENDHTML;
}

// END OF CONFIG.

// CALL THE MAIN ROUTINE IN THE LIBRARY.
afswl_main( 'https://' . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'] );
?>

<?php
/* ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * SPDX-License-Identifier: MIT
 * ---------------------------------------------------------------------------------
 *
 * Anidea for WebHook Wrapper (afww_main.php)
 * ==========================================
 * Version: 20.06.18.00
 */

require_once 'afww_curl.php';

require_once 'afww_lifecycle.php';

require_once 'afww_capabilities.php';
require_once 'afww_devices.php';
require_once 'afww_subscriptions.php';

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

?>

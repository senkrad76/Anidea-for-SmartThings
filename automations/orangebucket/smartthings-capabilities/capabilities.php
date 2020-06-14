<?php
// Change the following line to reflect the correct location.
require_once 'anidea-for-webhook-wrapper/afww_main.php';

// Personal Access Token.
$accesstoken = 'UUID';

//
// SmartThings Capabilities (capabilities.php) (C) Graham Johnson 2020
// ===================================================================
// Version: 20.06.14.00
//

function afww_config_main()
{
    global $accesstoken;
?>
<!DOCTYPE html>
<html lang="en-gb">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>SmartThings Capabilities</title>
    <style>
        html, body  { margin: 0; padding: 0; font-family: sans-serif; background: white; }
        #wrapper    { margin: 0.25em; }
        #intro span { padding: 0.1em; color: white; }
        h3          { margin: 0.25em 0; padding: 0.25em; font-weight: normal; font-size: 1.5em; color: white; background: blue; }
        #wait       { background: blue; }
        .live       { background: green; }
        .proposed   { background: orange; }
        .deprecated { background: red; }
        .dead       { background: black }
        pre         { margin: 0; padding: 1em; font-size: 1.25em; background: #eee; display: none; }
    </style>
    </head>
    <body>
        <div id="wrapper">
            <h3 id="wait">Please wait for the page to finish loading.</h3>
            <h1>SmartThings Capabilities</h1>
            <div id="intro">
                <p>The capabilities are read from the SmartThings REST API. As this takes a while and
                the capabilities do not change very often, they are cached for twenty-four hours.</p>
                <p>The capabilities are listed alphabetically and colour coded as <span class="live">live</span>, 
                <span class="proposed">proposed</span>, <span class="deprecated">deprecated</span> and 
                <span class="dead">dead</span>.</p>
                <p>Clicking on the capability name toggles display of the JSON for the capability.</p>
            </div>
<?php
$url = $_SERVER[ 'SCRIPT_NAME' ];
$ts  = $_SERVER[ 'REQUEST_TIME' ];

$break = explode('/', $url);
$file = $break[ count($break) - 1 ];

$cachefile = substr_replace($file ,"",-4).'-cache.html';
$cachetemp = $cachefile . $ts;
$cachetime = 86400;

// Serve from the cache if it is younger than $cachetime
if ( file_exists($cachefile) && time() - $cachetime < filemtime($cachefile) )
{
?>
            <div id="cache">The capabilities were last read at <?php echo date( 'r', filemtime($cachefile)); ?>.</div>
<?php
    readfile($cachefile);
?>
        </div>
    </body>
</html>
<?php
    exit;
}

$cached = fopen( $cachetemp, 'w' );

ob_start();

$capabilities = afww_capabilities_list( $accesstoken );

$listjson = json_decode( $capabilities, true );

function sortbyname( $a, $b )
{
    return $a[ 'id' ] < $b[ 'id' ] ? -1 : 1;
}

usort( $listjson[ 'items' ], 'sortbyname' );

foreach ( $listjson[ 'items' ] as $cap )
{
    $capability = "https://api.smartthings.com/v1/capabilities/${cap[ 'id' ]}/${cap[ 'version' ]}";

    $ch = curl_init( $capability );
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1 );
    curl_setopt( $ch, CURLOPT_POST,           0 );
    curl_setopt( $ch, CURLOPT_FAILONERROR,    1 );
    curl_setopt( $ch, CURLOPT_HTTPHEADER, array( "Authorization: Bearer $accesstoken" ) );

    if ( ( $resp =  curl_exec( $ch ) ) === false )
    {
?>
            <div class="error"><?php echo curl_error( $ch ); ?></div>
<?php
    }

    curl_close( $ch );

    $capjson = json_decode( $resp, true );
?>
            <h3 class="<?php echo $capjson[ 'status' ]; ?>" onclick="pre = document.getElementById( '<?php echo $capjson[ 'id' ]; ?>' ); pre.style.display = ( pre.style.display == 'none' ) ? 'block' : 'none';"><?php echo $capjson[ 'name' ]; ?></h3>
            <pre id="<?php echo $capjson[ 'id' ]; ?>"><?php echo json_encode( $capjson, JSON_PRETTY_PRINT ) ?></pre>
<?php
    fwrite( $cached, ob_get_contents() );
    ob_flush();
}
?>
        <script> document.getElementById( 'wait' ).style.display = 'none'; </script>
<?php
fwrite( $cached, ob_get_contents() );
fclose( $cached );

rename( $cachetemp, $cachefile );

ob_end_flush();
?>
        </div>
    </body>
</html>
<?php
}

afww_main( 'https://' . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'], $access_token );
?>

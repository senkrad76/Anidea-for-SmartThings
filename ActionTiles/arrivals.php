<!DOCTYPE html>
<?php
//
// arrivals.php (C) Graham Johnson 2018-2019
// ========================================
// Version: 1.1.2   18/01/2019
//
// ---------------------------------------------------------------------------------
// Permission to use, copy, modify, and/or distribute this software for any purpose
// with or without fee is hereby granted, provided that the copyright notice below
// and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH 
// REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND 
// FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
// INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
// OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER 
// TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
// THIS SOFTWARE.
// ---------------------------------------------------------------------------------
//
// Present arrivals data from the TfL Unified API in a form sympathetic to an
// ActionTiles panel.
//

//
// Read the 'anidea.ini' file for configuration information.
//
$ini = parse_ini_file( 'anidea.ini', true );

$refresh   = isset( $_GET[ "refresh" ]   ) ? $_GET[ "refresh" ]   : $ini[ 'arrivals' ][ 'refresh' ];
$stop      = isset( $_GET[ "stop" ]      ) ? $_GET[ "stop" ]      : $ini[ 'arrivals' ][ 'stop' ];
$route     = isset( $_GET[ "route" ]     ) ? $_GET[ "route" ]     : $ini[ 'arrivals' ][ 'route' ];
$direction = isset( $_GET[ "direction" ] ) ? $_GET[ "direction" ] : $ini[ 'arrivals' ][ 'direction' ];
$towards   = isset( $_GET[ "towards" ]   ) ? $_GET[ "towards" ]   : $ini[ 'arrivals' ][ 'towards' ];
$app_id    = isset( $_GET[ "app_id" ]    ) ? $_GET[ "app_id" ]    : $ini[ 'arrivals' ][ 'app_id' ];
$appkey    = isset( $_GET[ "appkey" ]    ) ? $_GET[ "appkey" ]    : $ini[ 'arrivals' ][ 'appkey' ];
?>
<html lang="en-gb">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
<?php
if ( $refresh )
{
?>
        <meta http-equiv="refresh" content="<?php echo $refresh; ?>">
<?php
}
?>
        <title>Live Arrivals</title>
        <style type="text/css">
            body                        { margin: 0; padding: 0; color: white; background: black; font-family: sans-serif;}
            .wrapper                    { margin: 4px; }
            .tiles                      { margin: 0 0 4px 0; padding: 0; overflow: auto; white-space: nowrap; font-size: 0; }
            .tile                       { display: inline-block; width: 102px; height: 102px; white-space: normal; vertical-align: top;
                                          margin: 0 4px 0 0; padding: 0; color: #000000; background: #c0c000; text-align: center; }
            .emptytile                  { display: inline-block; width: 100px; height: 100px;  white-space: normal; vertical-align: top;
                                          margin: 0 4px 0 0; padding: 0; color: #c0c000; background: #000000; border: 1px solid #c0c000; }
            .tiles > div:last-child     { margin-right: 0; }
            .lineid                     { font-size: 22px; }
            .expectedarrival            { font-weight: bold; font-size: 36px; }
            .destinationname            { font-size: 14px; }
            .description                { display: none; width: 308px; height: 96px; margin: 0 0 0 0; padding: 2px; 
                                          color: #c0c000; background: #000000; border: 1px solid #c0c000; text-align: center; }
            #t0 .expectedarrival        { text-decoration: overline underline;}
            #d0                         { display: block; }
            .stationname                { font-weight: bold; font-size: 18px; }
        </style>
        <script>
            function displaydesc( tilenum )
            {
                var i = 0;
                var tile, desc;
                
                while ( ( tile = document.getElementById( 't' + i ) ) && ( desc = document.getElementById( 'd' + i ) ) )
                {
                    if ( tile.className != 'emptytile' ) tile.children[1].style.textDecoration = ( tilenum == i ) ? 'overline underline' : 'none';
                    desc.style.display    = ( tilenum == i ) ? 'block' : 'none';
                    
                    i++;
                }
            }
        </script>
    </head>
<?php
$filterkey   = $towards ? 'towards' : ( $direction ? 'direction' : 'lineId');
$filtervalue = $towards ? $towards  : ( $direction ? $direction  : $route );
 
// The Unified API can be used for bus, tram or rail (London Overground)
// but you don't see departure times so it isn't much use at a terminus.
    
$tfl = 'https://api.tfl.gov.uk/StopPoint/' . $stop . '/Arrivals?app_id=' . $app_id . '&app_key=' . $appkey;

$ch = curl_init($tfl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
curl_setopt($ch, CURLOPT_POST,           0 );

$resp =  curl_exec($ch);
curl_close($ch);

$json = json_decode($resp,true);
?>
    <body>
        <div class="wrapper">
            <div class="tiles">
<?php  
function sorter($one, $two) {
    return ($one['expectedArrival'] > $two['expectedArrival']);
}

usort($json, 'sorter');
    
function filterer($item)
{
    global $filterkey;
    global $filtervalue;
        
    return( preg_match( '/' . $filtervalue . '/', $item[$filterkey] ) ) ;
}
    
$buses = $json;
    
if ($filtervalue) $buses = array_filter($json, 'filterer');
    
$buscount = 0;

foreach ( $buses as $bus )
{
?>
                <div class="tile" id="t<?php echo $buscount; ?>" onclick="displaydesc( <?php echo $buscount; ?> );">
                    <div class="lineid"><?php echo strtoupper(str_replace('london-overground', 'rail', $bus['lineId'])); ?></div>
                    <div class="expectedarrival"><?php echo date("H:i", strtotime($bus["expectedArrival"])); ?></div>
                    <div class="destinationname"><?php echo $bus['destinationName']; ?></div>
                </div>
<?php
    ++$buscount;
}
    
for ( ; $buscount < 3 ; ++$buscount )
{
?>
                <div class="emptytile" id="t<?php echo $buscount; ?>" onclick="displaydesc( <?php echo $buscount; ?> );"></div>
<?php
}
?>
            </div>
<?php
$buscount = 0;

foreach ( $buses as $bus )
{
?>
            <div class="description" id="d<?php echo $buscount; ?>">
                <div class="stationname"><?php echo $bus['stationName']; ?></div>
                <div class="towards">towards <strong><?php echo $bus['towards']; ?></strong></div>
                <div class="busdesc"><?php echo ucwords( $bus['lineId'] ); ?> to <?php echo $bus['destinationName']; ?></div>
                <div class="filter"><?php echo $stop . ($filtervalue ? " (" . $filterkey . "=" . $filtervalue . ")" : '') ?></div>
            </div>
<?php
    ++$buscount;
}
    
for ( ; $buscount < 3 ; ++$buscount )
{
?>
            <div class="description" id="d<?php echo $buscount; ?>">
                <div class="stationname">No information</div>
                <div class="towards">&nbsp;</div>
                <div class="filter"><?php echo $stop . ($filtervalue ? " (" . $filterkey . "=" . $filtervalue . ")" : '') ?></div>
            </div>
<?php
}
?>   
        </div>
    </body>
</html>

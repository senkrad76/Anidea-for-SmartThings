<!DOCTYPE html>
<?php
    $stop      = isset( $_GET["stop"]      ) ? $_GET["stop"]      : '<STOPID>';
    $route     = isset( $_GET["route"]     ) ? $_GET["route"]     : '';
    $direction = isset( $_GET["direction"] ) ? $_GET["direction"] : '';
    $towards   = isset( $_GET["towards"]   ) ? $_GET["towards"]   : '';
    $refresh   = isset( $_GET["refresh"]   ) ? $_GET["refresh"]   : '60';
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
            .tiles                      { margin: 0 0 4px 0; padding: 0; overflow: auto; }
            .tile                       { float: left; width: 102px; height: 102px; margin: 0 4px 0 0; padding: 0; 
                                          color: #000000; background: #c0c000; text-align: center; }
            .emptytile                  { float: left; width: 100px; height: 100px; margin: 0 4px 0 0; padding: 0; 
                                          color: #c0c000; background: #000000; border: 1px solid #c0c000; text-align: center; }
            .tiles > div:last-child     { margin-right: 0; }
            .lineid                     { font-size: 22px; }
            .expectedarrival            { font-weight: bold; font-size: 36px; }
            .destinationname            { font-size: 14px; }
            .description                { width: 308px; height: 96px; margin: 0 0 0 0; padding: 2px; 
                                          color: #c0c000; background: #000000; border: 1px solid #c0c000; text-align: center; }
            #d0                         { display: block; text-align: left;   }
            #d1                         { display: none;  text-align: center; }
            #d2                         { display: none;  text-align: right;  }
            .stationname                { font-weight: bold; font-size: 18px; }
        </style>
        <script>
            function displaydesc(tile)
            {
                document.getElementById('d0').style.display = (tile == 'd0') ? 'block' : 'none';
                document.getElementById('d1').style.display = (tile == 'd1') ? 'block' : 'none';
                document.getElementById('d2').style.display = (tile == 'd2') ? 'block' : 'none';
            }
        </script>
    </head>
<?php
    $filterkey   = $towards ? 'towards' : ( $direction ? 'direction' : 'lineId');
    $filtervalue = $towards ? $towards  : ( $direction ? $direction  : $route );
 
    // The Unified API can be used for bus, tram or rail (London Overground)
    // but you don't see departure times so it isn't much use at a terminus.
    
    $tfl = 'https://api.tfl.gov.uk/StopPoint/' . $stop . '/Arrivals?app_id=<APPID>&app_key=<APPKEY?';

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
                <div class="tile" onclick="displaydesc('d<?php echo $buscount; ?>');">
                    <div class="lineid"><?php echo strtoupper(str_replace('london-overground', 'rail', $bus['lineId'])); ?></div>
                    <div class="expectedarrival"><?php echo date("H:i", strtotime($bus["expectedArrival"])); ?></div>
                    <div class="destinationname"><?php echo $bus['destinationName']; ?></div>
                </div>
<?php
        if ( ++$buscount == 3 ) break;
    }
    
    for ( ; $buscount < 3 ; ++$buscount )
    {
?>
                <div class="emptytile" onclick="displaydesc('d<?php echo $buscount; ?>');"></div>
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
                <div class="filter"><?php echo $stop . ($filtervalue ? " (" . $filterkey . "=" . $filtervalue . ")" : '') ?></div>
            </div>
<?php
        if ( ++$buscount == 3 ) break;
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


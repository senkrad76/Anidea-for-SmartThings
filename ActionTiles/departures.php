<!DOCTYPE html>
<?php
//
// departures.php (C) Graham Johnson 2018-2019
// ===========================================
// Version: 1.1.0   18/01/2019
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
// Present departures/arrivals data from the LDBWS in a form sympathetic to an 
// ActionTiles panel.
//

$from     = isset( $_GET[ 'from' ]     ) ? $_GET[ 'from' ]     : 'ECR';
$to       = isset( $_GET[ 'to' ]       ) ? $_GET[ 'to' ]       : 'CTK';
$services = isset( $_GET[ 'services' ] ) ? $_GET[ 'services' ] : 10;                                     // Number of services to look for.
$key      = isset( $_GET[ 'key' ]      ) ? $_GET[ 'to' ]       : 'KEY';                                  // LDBWS Key.
?>
<html lang="en-gb">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta http-equiv="refresh" content="60" >
        <title>Live Departures</title>
        <style type="text/css">
            body                        { margin: 0; padding: 0; color: white; background: black; font-family: sans-serif;}
            .wrapper                    { margin: 4px; }
            #departures                 { display: block; }
            #arrivals                   { display: none; }
            .tiles                      { margin: 0px 0 4px 0; padding: 0; overflow: auto; white-space: nowrap; font-size: 0; }
            .tile                       { display: inline-block; width: 102px; height: 102px; vertical-align: top;
                                          margin: 0 4px 0 0; padding: 0; color: #000000; background: #c000c0; text-align: center; }
            .emptytile                  { display: inline-block; width: 100px; height: 100px;  vertical-align: top; 
                                          margin: 0 4px 0 0; padding: 0; color: #c000c0; background: #000000; border: 1px solid #c000c0; text-align: center; }
            .tiles > div:last-child     { margin-right: 0; }
            .etd                        { font-size: 22px;}
            .departure                  { font-weight: bold; font-size: 36px;}
            .destinationname            { font-size: 14px;}
            .description                { width: 308px; height: 96px; margin: 0 0 0 0; padding: 2px; 
                                         color: #c000c0; background: #000000; border: 1px solid #c000c0; text-align: center; }
            #departurest0, #arrivalst0  { background: #800080; }
            #departuresd0, #arrivalsd0  { display: block; }
            .stationname                { font-weight: bold; font-size: 18px;}
        </style>
        <script>
            function displaydirection( direction )
            {
                document.getElementById('departures').style.display = (direction == "departures") ? 'none' : 'block';
                document.getElementById('arrivals'  ).style.display = (direction == "arrivals")   ? 'none' : 'block';
            }
            
            function displaydesc( direction, tilenum )
            {
                var i = 0;
                var tile, desc;
                
                while ( ( tile = document.getElementById( direction + 't' + i ) ) && ( desc = document.getElementById( direction + 'd' + i ) ) )
                {
                    if ( tile.className != 'emptytile' ) tile.style.background = ( tilenum == i ) ? '#800080' : '#c000c0';
                    
                    desc.style.display = ( tilenum == i ) ? 'block' : 'none';
                    
                    i++;
                }
            }
        </script>
    </head>
<?php
require("OpenLDBWS.php");

$OpenLDBWS = new OpenLDBWS( $key );
?>
    <body>
        <div class="wrapper">
<?php       
foreach ( [ 'departures', 'arrivals' ] as $direction)
{
    $ldb = ( $direction == 'arrivals' ) ? $OpenLDBWS->GetArrivalBoard( $services, $to, $from, 'from' ) : $OpenLDBWS->GetDepartureBoard( $services, $from, $to );
?>
            <div id="<?php echo $direction; ?>">
                <div class="tiles">
<?php
    $traincount = 0;
    
    if ( isset( $ldb->GetStationBoardResult->trainServices ) ) foreach ( $ldb->GetStationBoardResult->trainServices->service as $train )
    {
?>
                    <div class="tile" id="<?php echo $direction; ?>t<?php echo $traincount; ?>" onclick="displaydesc( '<?php echo $direction; ?>', <?php echo $traincount; ?> );">
                        <div class="etd"><?php echo isset( $train->eta ) ? $train->eta : $train->etd; ?></div>
                        <div class="departure"><?php echo isset( $train->sta ) ? $train->sta : $train->std; ?></div>
                        <div class="destinationname"><?php echo $train->destination->location->locationName; ?></div>
                    </div>
<?php
        ++$traincount;
    }
        
    for ( ; $traincount < 3 ; ++$traincount )
    {
?>
                    <div class="emptytile" id="<?php echo $direction; ?>t<?php echo $traincount; ?>" onclick="displaydesc( '<?php echo $direction; ?>', <?php echo $traincount; ?> );"></div>           
<?php
    }
?>
                </div>
<?php
    $traincount = 0;

    if ( isset( $ldb->GetStationBoardResult->trainServices ) ) foreach ( $ldb->GetStationBoardResult->trainServices->service as $train )
    {
?>
                <div class="description" id="<?php echo $direction; ?>d<?php echo $traincount; ?>">
                    <div class="stationname"><?php print_r($ldb->GetStationBoardResult->locationName); ?></div>
                    <div><strong><?php echo $train->origin->location->locationName; ?></strong> to <strong><?php echo $train->destination->location->locationName; ?></strong></div>
                    <div><strong><?php echo $train->operator; ?></strong></div>
                    <div><?php echo ucfirst( $direction ) . ' ' . ( ($direction == 'departures') ? "from $from to $to" : "at $to from $from" ); ?></div>
                    <a href="#" onclick="displaydirection('<?php echo $direction; ?>'); return false;">
                    <?php echo ($direction == 'departures') ? "Arrivals at $to" : "Departures from $from"; ?></a>
                </div>
<?php
        ++$traincount;
    }
        
    for ( ; $traincount < 3 ; ++$traincount )
    {
?>
                <div class="description" id="<?php echo $direction; ?>d<?php echo $traincount; ?>">
                    <div>No information</div>
                    <a href="#" onclick="displaydirection('<?php echo $direction; ?>'); return false;">
                    <?php echo ($direction == 'departures') ? "Arrivals at $to" : "Departures from $from"; ?></a>
                </div>           
<?php
    }
?>
            </div>
<?php
}
?>
        </div>
    </body>
</html>

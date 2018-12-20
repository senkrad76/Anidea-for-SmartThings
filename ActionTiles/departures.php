<!DOCTYPE html>
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
            .tiles                      { margin: 0px 0 4px 0; padding: 0; overflow: auto; }
            .tile                       { float: left; width: 102px; height: 102px; margin: 0 4px 0 0; padding: 0; 
                                          color: #000000; background: #c0c000; text-align: center; }
            .emptytile                  { float: left; width: 100px; height: 100px; margin: 0 4px 0 0; padding: 0; 
                                          color: #c0c000; background: #000000; border: 1px solid #c0c000; text-align: center; }
            .tiles > div:last-child     { margin-right: 0; }
            .etd                        { font-size: 22px;}
            .departure                  { font-weight: bold; font-size: 36px;}
            .destinationname            { font-size: 14px;}
            .description                { width: 308px; height: 96px; margin: 0 0 0 0; padding: 2px; 
                                         color: #c0c000; background: #000000; border: 1px solid #c0c000; text-align: center; }
            #departuresd0, #arrivalsd0  { display: block; text-align: left;   }
            #departuresd1, #arrivalsd1  { display: none;  text-align: center; }
            #departuresd2, #arrivalsd2  { display: none;  text-align: right;  }
            .stationname                { font-weight: bold; font-size: 18px;}
        </style>
        <script>
            function displaydirection(direction)
            {
                document.getElementById('departures').style.display = (direction == "departures") ? 'none' : 'block';
                document.getElementById('arrivals').style.display = (direction == "arrivals") ? 'none' : 'block';
            }
            
            function displaydesc(direction, tile)
            {
                document.getElementById(direction + 'd0').style.display = (tile == 'd0') ? 'block' : 'none';
                document.getElementById(direction + 'd1').style.display = (tile == 'd1') ? 'block' : 'none';
                document.getElementById(direction + 'd2').style.display = (tile == 'd2') ? 'block' : 'none';
            }
        </script>
    </head>
<?php
    $from  = isset( $_GET['from']  ) ? $_GET['from']  : 'ECR';
    $to    = isset( $_GET['to']    ) ? $_GET['to']    : 'CTK';

    require("OpenLDBWS.php");

    $OpenLDBWS = new OpenLDBWS( '<KEY>' );
?>
    <body>
<?php       
    foreach ( [ 'departures', 'arrivals' ] as $direction)
    {
        $ldb = ( $direction == 'arrivals' ) ? $OpenLDBWS->GetArrivalBoard( 3, $to, $from, 'from' ) : $OpenLDBWS->GetDepartureBoard( 3, $from, $to );
?>
        <div id="<?php echo $direction; ?>">
            <div class="wrapper">
                <div class="tiles">
<?php
        $traincount = 0;
    
        if ( isset( $ldb->GetStationBoardResult->trainServices ) ) foreach ( $ldb->GetStationBoardResult->trainServices->service as $train )
        {
?>
                    <div class="tile" onclick="displaydesc('<?php echo $direction; ?>', 'd<?php echo $traincount; ?>');">
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
                    <div class="emptytile" onclick="displaydesc('<?php echo $direction; ?>', 'd<?php echo $traincount; ?>');"></div>           
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

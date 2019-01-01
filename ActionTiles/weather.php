<!DOCTYPE html>
<?php
    $refresh   = isset( $_GET["refresh"]   ) ? $_GET["refresh"]   : '900';
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
        <title>Weather Forecast</title>
        <style type="text/css">
            body                        { margin: 0; padding: 0; color: white; background: black; font-family: sans-serif;}
            .wrapper                    { margin: 4px; }
            .tiles                      { margin: 0 0 4px 0; padding: 0; overflow: auto; }
            .tile                       { float: left; width: 102px; height: 102px; margin: 0 4px 0 0; padding: 0; 
                                          color: #000000; background: #00c0c0; text-align: center; }
            .emptytile                  { float: left; width: 100px; height: 100px; margin: 0 4px 0 0; padding: 0; 
                                          color: #00c0c0; background: #000000; border: 1px solid #00c0c0; text-align: center; }
            .tiles > div:last-child     { margin-right: 0; }
            .fcrain                     { font-size: 16px; }
            .fctemp                     { font-weight: bold; font-size: 48px; }
            .fcfeels                    { font-size: 16px; }
            .description                { width: 308px; height: 96px; margin: 0 0 0 0; padding: 2px; 
                                          color: #00c0c0; background: #000000; border: 1px solid #00c0c0; text-align: center; }
            .weathertype                { font-size: 24px; }
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
    $weathertype = array(   'Clear night', 
                            'Sunny day',
                            'Partly cloudy (night)',
                            'Partly cloudy (day)', 
                            'Not used', 
                            'Mist', 
                            'Fog',
                            'Cloudy',
                            'Overcast',
                            'Light rain shower (night)',
                            'Light rain shower (day)',
                            'Drizzle',
                            'Light rain',
                            'Heavy rain shower (night)',
                            'Heavy rain shower (day)',
                            'Heavy rain',
                            'Sleet shower (night)',
                            'Sleet shower (day)',
                            'Sleet',
                            'Hail shower (night)',
                            'Hail shower (day)',
                            'Hail',
                            'Light snow shower (night)',
                            'Light snow shower (day)',
                            'Light snow',
                            'Heavy snow shower (night)',
                            'Heavy snow shower (day)',
                            'Heavy snow',
                            'Thunder shower (night)',
                            'Thunder shower (day)',
                            'Thunder' );
    
    
    
    $owm = 'http://datapoint.metoffice.gov.uk/public/data/val/wxfcs/all/json/<CODE>?res=3hourly&key=<APIKEY>';

	$ch = curl_init( $owm );
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
	curl_setopt($ch, CURLOPT_POST,           0 );

	$resp =  curl_exec( $ch );
	curl_close( $ch );

	$json = json_decode( $resp, true );
?>
    <body>
        <!-- <pre><?php print_r( $json ); ?></pre> -->
        <div class="wrapper">
            <div class="tiles">
<?php
    $count = 0;
    
    $now = new DateTime();
    
    for ( $period = 0;  $period < 2; $period++ )
    {
        foreach ( $json[ 'SiteRep' ][ 'DV' ][ 'Location' ][ 'Period' ][ $period ][ 'Rep' ] as $forecast )
        {
            // Have to check how this works with DST.
            $fctime =  new DateTime( $json[ 'SiteRep' ][ 'DV' ][ 'Location' ][ 'Period' ][ $period ][ 'value'] );
            $fctime->modify("+{$forecast[ '$' ]} minutes");
            
            if ( $fctime < $now ) continue;
?>
                <div class="tile" onclick="displaydesc('d<?php echo $count; ?>')";>
                    <div class="fcrain">&#x2119(rain) <strong><?php echo $forecast[ 'Pp' ]; ?>%</strong></div>
                    <div class="fctemp"><?php echo $forecast[ 'T' ]; ?>&deg;</div>
                    <div class="fcfeels">Feels like <strong><?php echo $forecast[ 'F' ]; ?>&deg;</strong></div>
                </div>
<?php
            if ( ++$count == 3 ) break;
        }
        
        if ( $count == 3 ) break;
    }
?>
            </div>
<?php
    $count = 0;
    
    for ( $period = 0;  $period < 2; $period++ )
    {
        foreach ( $json[ 'SiteRep' ][ 'DV' ][ 'Location' ][ 'Period' ][ $period ][ 'Rep' ] as $forecast )
        {
            // Have to check how this works with DST.
            $fctime =  new DateTime( $json[ 'SiteRep' ][ 'DV' ][ 'Location' ][ 'Period' ][ $period ][ 'value'] );
            $fctime->modify("+{$forecast[ '$' ]} minutes");
            
            if ( $fctime < $now ) continue;
?>
                <div class="description" id="d<?php echo $count; ?>";>
                    <div><?php echo $fctime->format( 'l jS F Y H:i' ); ?></div>
                    <div class="weathertype"><strong><?php echo $weathertype[ $forecast['W'] ]; ?></strong></div>
                    <div>Wind <?php echo $forecast['S']; ?>mph from the <?php echo $forecast['D']; ?></div>
                    <div>Max UV <?php echo $forecast['U']; ?></div>
                </div>
<?php
            if ( ++$count == 3 ) break;
        }
        
        if ( $count == 3 ) break;
    }
?>
            </div>
    </body>
</html>

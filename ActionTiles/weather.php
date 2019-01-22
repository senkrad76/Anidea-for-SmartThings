<!DOCTYPE html>
<?php
//
// weather.php (C) Graham Johnson 2018-2019
// ========================================
// Version: 1.4.0   22/01/2019
//
// ---------------------------------------------------------------------------------
// Permission to use, copy, modify, and/or distribute this software for any purpose
// with or without fee is hereby granted, provided that the copyright notice below
// and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH 
// REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND 
// FITNESS.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
// INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
// OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER 
// TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
// THIS SOFTWARE.
// ---------------------------------------------------------------------------------
//
// Present weather in a bespoke ActionTiles compatible form, sourcing the current 
// observations from OpenWeatherMap and the three-hourly weather forecasts and the
// regional text forecasts from the Met Office DataPoint API
//

//
// Read the 'anidea.ini' file for configuration information.
//
$ini = parse_ini_file( 'anidea.ini', true );
    
$refresh  = isset( $_GET["refresh"]   ) ? $_GET["refresh"]  : $ini[ 'weather' ][ 'refresh'];

// OWM.
$owmid    = isset( $_GET["owmid"]    )  ? $_GET["owmid"]    : $ini[ 'weather' ][ 'owmid' ];
$owmkey   = isset( $_GET["owmkey"]    ) ? $_GET["owmkey"]   : $ini[ 'weather' ][ 'owmkey' ];

// Met Office DataPoint.
$cityid   = isset( $_GET["cityid"]    ) ? $_GET["cityid"]   : $ini[ 'weather' ][ 'cityid' ];
$regionid = isset( $_GET["regionid"]  ) ? $_GET["regionid"] : $ini[ 'weather' ][ 'regionid' ];
$apikey   = isset( $_GET["apikey"]    ) ? $_GET["apikey"]   : $ini[ 'weather' ][ 'apikey' ];

$showjson  = isset( $_GET["showjson"] ) ? $_GET["showjson"] : $ini[ 'weather' ][ 'showjson' ];
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
        <style>
            body                        { margin: 0; padding: 0; color: white; background: black; font-family: sans-serif;}
            .wrapper                    { margin: 4px; }
            .tiles                      { margin: 0 0 4px 0; padding: 0; overflow: auto; white-space: nowrap; font-size: 0;}
            .owmtile                    { float: left; width: 100px; height: 100px; white-space: normal; margin: 0 4px 0 0; padding: 0;
                                          color: #00c0c0; background: #000000; border: 1px solid #00c0c0; text-align: center; }
            .tile                       { display: inline-block; width: 102px; height: 102px; margin: 0 4px 0 0; padding: 0; 
                                          color: #000000; background: #00c0c0; text-align: center; }
            .emptytile                  { display: inline-block; width: 100px; height: 100px; margin: 0 4px 0 0; padding: 0; 
                                          color: #00c0c0; background: #000000; border: 1px solid #00c0c0; text-align: center; }
            .tiles > div:last-child     { margin-right: 0; }
            .owmtime                    { font-weight: bold; font-size: 16px; }
            .owmtemp                    { font-weight: bold; font-size: 48px; }
            .owmweather                 { font-weight: bold; font-size: 16px; }
            .owmdescription             { font-weight: bold; font-size: 12px; }
            .owmwind                    { font-weight: bold; font-size: 16px; }
            .owmspeed                   { font-weight: bold; font-size: 48px; }
            .owmunit                    { font-weight: bold; font-size: 16px; }
            .fctime                     { font-weight: bold; font-size: 16px; }
            .fctemp                     { font-weight: bold; font-size: 48px; }
            .fcfeelsrain                { font-weight: bold; font-size: 16px; }
            #t0 .fctemp                 { text-decoration: overline underline; }
            .description                { width: 308px; height: 96px; margin: 0 0 0 0; padding: 2px; display: none;
                                          color: #00c0c0; background: #000000; border: 1px solid #00c0c0; text-align: center; }
            .weathertype                { font-size: 24px; }
            .error                      { color: #ffffff; background: #ff0000; padding: 2px; font-size: 14px; }
            #textforecast               { color: #00c0c0; font-size: 14px; }
        </style>
        <script>
            function displaydesc( tilenum )
            {     
                var i = 0;
                var tile, desc;
                
                while ( ( tile = document.getElementById( 't' + i ) ) && ( desc = document.getElementById( 'd' + i ) ) )
                {
                    tile.children[1].style.textDecoration = ( tilenum == i ) ? 'overline underline' : 'none';
                    desc.style.display    = (tilenum == i ) ? 'block'   : 'none';
                    
                    i++;
                }
    
                document.getElementById( 'fctiles' ).scrollIntoView();
                window.scrollBy(0, -4);
            }
        </script>
    </head>
    <body>
        <div class="wrapper">
<?php
$owm = "http://api.openweathermap.org/data/2.5/weather?id=$owmid&appid=$owmkey";

$ch = curl_init( $owm );
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
curl_setopt($ch, CURLOPT_POST,           0 );

$resp =  curl_exec( $ch );
curl_close( $ch );

$json = json_decode( $resp, true );

if ( $showjson ) echo "\n<pre>\n" . print_r( $json, true ) . "\n</pre>\n";

$cotime =  new DateTime( '@' . $json[ 'dt' ] );

function compasspoint( $bearing ) 
{ 
    $points = array( 'N', 'NNE', 'NE', 'ENE', 'E', 'ESE', 'SE', 'SSE', 'S', 'SSW', 'SW', 'WSW', 'W', 'WNW', 'NW', 'NNW', 'N');
    
    return $points[ round( $bearing / 22.5 ) ];
}

?>
            <div class="tiles">
                <div class="owmtile">
                     <div class="owmtime"><?php echo $cotime->format( 'D H:i' ); ?></div>
                     <div class="owmtemp"><?php echo round( $json[ 'main' ][ 'temp' ] - 273.15 ); ?>&deg;</div>
                </div>
                <div class="owmtile">
                    <div class="owmweather"><?php echo $json[ 'weather' ][ 0 ][ 'main' ]; ?></div>
                    <div><img src="https://openweathermap.org/img/w/<?php echo $json[ 'weather' ][ 0 ][ 'icon' ]; ?>.png"></div>
                    <div class="owmdescription"><?php echo ucwords( $json[ 'weather' ][ 0 ][ 'description' ] ); ?></div>
                </div>
                <div class="owmtile">
                    <div class="owmwind"><?php echo compasspoint( $json[ 'wind' ][ 'deg' ] ); ?></div>
                    <div class="owmspeed"><?php echo round( $json[ 'wind' ][ 'speed' ]* 2.237 ); ?></div>
                    <div class="owmunit">mph</div>
                </div>
            </div>
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
                        
$met = "http://datapoint.metoffice.gov.uk/public/data/val/wxfcs/all/json/$cityid?res=3hourly&key=$apikey";

$ch = curl_init( $met );
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
curl_setopt($ch, CURLOPT_POST,           0 );
curl_setopt($ch, CURLOPT_FAILONERROR,    1);

//
// The DataPoint API forecast seems to rather prone to problems on occasions and the error log shows that there
// is a problem with the argument to the 'foreach' loop for the forecast periods.  There should really have been
// some error checking anyway. Now there are two levels of checking.  Firstly any curl error is checked and
// reported, and secondly the decoded feed is checked to make sure is has a 'period' array with the raw feed
// being dumped to a file if it hasn't.
//

if ( ( $resp =  curl_exec( $ch ) ) === false )
{
?>
            <div class="error"><?php echo curl_error( $ch ); ?></div>
<?php
}

curl_close( $ch );

$json = json_decode( $resp, true );

if ( $showjson ) echo "\n<pre>\n" . print_r( $json, true ) . "\n</pre>\n";

$error = ''; 

if ( ! isset( $json[ 'SiteRep' ][ 'DV' ][ 'Location' ][ 'Period' ] ) ) $error = "No 'Period' array found in 3 hourly forecast.";

if ( $error == '' )
{
?>
            <div class="tiles" id="fctiles"><!--
<?php
    $count = 0;
    
    $now = new DateTime();
    
    for ( $period = 0;  $period < 5; $period++ )
    {
        foreach ( $json[ 'SiteRep' ][ 'DV' ][ 'Location' ][ 'Period' ][ $period ][ 'Rep' ] as $forecast )
        {
            // Have to check how this works with DST.
            $fctime =  new DateTime( $json[ 'SiteRep' ][ 'DV' ][ 'Location' ][ 'Period' ][ $period ][ 'value'] );
            $fctime->modify("+{$forecast[ '$' ]} minutes");
            
            if ( $fctime < $now ) continue;
?>
                --><div class="tile" id="t<?php echo $count; ?>" onclick="displaydesc( <?php echo $count; ?> );">
                    <div class="fctime"><?php echo $fctime->format( 'D H:i' ); ?></div>
                    <div class="fctemp"><?php echo $forecast[ 'T' ]; ?>&deg;</div>
                    <div class="fcfeelsrain">&asymp; <?php echo $forecast[ 'F' ]; ?>&deg; &#x2119; <?php echo $forecast[ 'Pp' ]; ?>%</div>
                </div><!--
<?php
            ++$count;
        }
    }
?>
            --></div>
<?php
    $count = 0;
    
    for ( $period = 0;  $period < 5; $period++ )
    {
        foreach ( $json[ 'SiteRep' ][ 'DV' ][ 'Location' ][ 'Period' ][ $period ][ 'Rep' ] as $forecast )
        {
            // Have to check how this works with DST.
            $fctime =  new DateTime( $json[ 'SiteRep' ][ 'DV' ][ 'Location' ][ 'Period' ][ $period ][ 'value'] );
            $fctime->modify("+{$forecast[ '$' ]} minutes");
            
            if ( $fctime < $now ) continue;
?>
            <div class="description" id="d<?php echo $count; ?>">
                <div><?php echo $fctime->format( 'l jS F Y' ); ?> <strong><?php echo $fctime->format( 'H:i' ); ?></strong></div>
                <div class="weathertype"><strong><?php echo $weathertype[ $forecast['W'] ]; ?></strong></div>
                <div>Wind <?php echo $forecast['S']; ?>mph from the <?php echo $forecast['D']; ?></div>
                <div>Max UV <?php echo $forecast['U']; ?></div>
            </div>
<?php
            ++$count;
        }
    }
}
else
{
?>
            <div class="error"><?php echo $error; ?></div>
<?php
    file_put_contents( 'forecast_json.txt', print_r( $resp, true) );
}

$met = "http://datapoint.metoffice.gov.uk/public/data/txt/wxfcs/regionalforecast/json/$regionid?key=$apikey";
    
$ch = curl_init( $met );
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
curl_setopt($ch, CURLOPT_POST,           0 );

if ( ( $resp =  curl_exec( $ch ) ) === false )
{
?>
            <div class="error"><?php echo curl_error( $ch ); ?></div>
<?php
}
curl_close( $ch );

$json = json_decode( $resp, true );

if ( $showjson ) echo "\n<pre>\n" . print_r( $json, true ) . "\n</pre>\n";

$error = ''; 

if ( ! isset( $json[ 'RegionalFcst' ][ 'FcstPeriods' ][ 'Period' ] ) ) $error = "No 'Period' array found in regional forecast.";

if ( $error == '' )
{
?>
            <div id="textforecast">
<?php
    $periodcount = 0;

    //
    // The regional text forecast feed is divided into four periods. The first period covers
    // days 1-2 and is further divided into paragraphs, each with a title and text, to provide
    // more detail. The second period gives the brief outlook for days 3-5 and normally has
    // just the one paragraph. The next periods, covering days 6-15 and 16-30 are UK wide
    // and are skipped.
    // 
    foreach ( $json[ 'RegionalFcst' ][ 'FcstPeriods' ][ 'Period' ] as $period )	
    {
        // Check if there are multiple paragraphs in an array, or just the one.
        if ( array_key_exists( 0, $period[ 'Paragraph' ] ) ) foreach ( $period[ 'Paragraph' ] as $paragraph )
        {
?>
                <p><strong><?php echo $paragraph[ 'title' ]; ?></strong> <?php echo $paragraph[ '$' ]; ?></p>
<?php
        }
        else
        {
?>
                <p><strong><?php echo $period[ 'Paragraph' ][ 'title' ]; ?></strong> <?php echo $period[ 'Paragraph' ][ '$' ]; ?></p>
<?php
        }
    
        // Longer range forecasts are UK wide so skip them.
        if ( ++$periodcount == 2 ) break;
    }
?>
            </div>
<?php
}
else
{
?>
            <div class="error"><?php echo $error; ?></div>
<?php
    file_put_contents( 'regional_json.txt', print_r( $resp, true) );
}
?>
        </div>    
    </body>
</html>

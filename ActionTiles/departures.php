
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
            #departurest0 .departure,
            #arrivalst0 .departure      { text-decoration: overline underline; }
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
                    if ( tile.className != 'emptytile' ) tile.children[1].style.textDecoration = ( tilenum == i ) ? 'overline underline' : 'none';
                    
                    desc.style.display = ( tilenum == i ) ? 'block' : 'none';
                    
                    i++;
                }
            }
        </script>
    </head>
    <body>
        <div class="wrapper">
            <div id="departures">
                <div class="tiles">
                    <div class="tile" id="departurest0" onclick="displaydesc( 'departures', 0 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:15</div>
                        <div class="destinationname">Peterborough</div>
                    </div>
                    <div class="tile" id="departurest1" onclick="displaydesc( 'departures', 1 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:21</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="departurest2" onclick="displaydesc( 'departures', 2 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:33</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="departurest3" onclick="displaydesc( 'departures', 3 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:45</div>
                        <div class="destinationname">Peterborough</div>
                    </div>
                    <div class="tile" id="departurest4" onclick="displaydesc( 'departures', 4 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:51</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="departurest5" onclick="displaydesc( 'departures', 5 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:01</div>
                        <div class="destinationname">Cambridge</div>
                    </div>
                    <div class="tile" id="departurest6" onclick="displaydesc( 'departures', 6 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:03</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="departurest7" onclick="displaydesc( 'departures', 7 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:15</div>
                        <div class="destinationname">Peterborough</div>
                    </div>
                    <div class="tile" id="departurest8" onclick="displaydesc( 'departures', 8 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:21</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="departurest9" onclick="displaydesc( 'departures', 9 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:33</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                </div>
                <div class="description" id="departuresd0">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Three Bridges</strong> to <strong>Peterborough</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
                <div class="description" id="departuresd1">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Brighton</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
                <div class="description" id="departuresd2">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Gatwick Airport</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
                <div class="description" id="departuresd3">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Horsham</strong> to <strong>Peterborough</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
                <div class="description" id="departuresd4">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Brighton</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
                <div class="description" id="departuresd5">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Brighton</strong> to <strong>Cambridge</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
                <div class="description" id="departuresd6">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Gatwick Airport</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
                <div class="description" id="departuresd7">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Horsham</strong> to <strong>Peterborough</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
                <div class="description" id="departuresd8">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Brighton</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
                <div class="description" id="departuresd9">
                    <div class="stationname">East Croydon</div>
                    <div><strong>Gatwick Airport</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Departures from ECR to CTK</div>
                    <a href="#" onclick="displaydirection('departures'); return false;">
                    Arrivals at CTK</a>
                </div>
            </div>
            <div id="arrivals">
                <div class="tiles">
                    <div class="tile" id="arrivalst0" onclick="displaydesc( 'arrivals', 0 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:23</div>
                        <div class="destinationname">Cambridge</div>
                    </div>
                    <div class="tile" id="arrivalst1" onclick="displaydesc( 'arrivals', 1 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:28</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="arrivalst2" onclick="displaydesc( 'arrivals', 2 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:38</div>
                        <div class="destinationname">Peterborough</div>
                    </div>
                    <div class="tile" id="arrivalst3" onclick="displaydesc( 'arrivals', 3 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:43</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="arrivalst4" onclick="displaydesc( 'arrivals', 4 );">
                        <div class="etd">On time</div>
                        <div class="departure">13:58</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="arrivalst5" onclick="displaydesc( 'arrivals', 5 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:08</div>
                        <div class="destinationname">Peterborough</div>
                    </div>
                    <div class="tile" id="arrivalst6" onclick="displaydesc( 'arrivals', 6 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:13</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="arrivalst7" onclick="displaydesc( 'arrivals', 7 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:23</div>
                        <div class="destinationname">Cambridge</div>
                    </div>
                    <div class="tile" id="arrivalst8" onclick="displaydesc( 'arrivals', 8 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:28</div>
                        <div class="destinationname">Bedford</div>
                    </div>
                    <div class="tile" id="arrivalst9" onclick="displaydesc( 'arrivals', 9 );">
                        <div class="etd">On time</div>
                        <div class="departure">14:38</div>
                        <div class="destinationname">Peterborough</div>
                    </div>
                </div>
                <div class="description" id="arrivalsd0">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Brighton</strong> to <strong>Cambridge</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
                <div class="description" id="arrivalsd1">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Gatwick Airport</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
                <div class="description" id="arrivalsd2">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Three Bridges</strong> to <strong>Peterborough</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
                <div class="description" id="arrivalsd3">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Brighton</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
                <div class="description" id="arrivalsd4">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Gatwick Airport</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
                <div class="description" id="arrivalsd5">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Horsham</strong> to <strong>Peterborough</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
                <div class="description" id="arrivalsd6">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Brighton</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
                <div class="description" id="arrivalsd7">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Brighton</strong> to <strong>Cambridge</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
                <div class="description" id="arrivalsd8">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Gatwick Airport</strong> to <strong>Bedford</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
                <div class="description" id="arrivalsd9">
                    <div class="stationname">City Thameslink</div>
                    <div><strong>Horsham</strong> to <strong>Peterborough</strong></div>
                    <div><strong>Thameslink</strong></div>
                    <div>Arrivals at CTK from ECR</div>
                    <a href="#" onclick="displaydirection('arrivals'); return false;">
                    Departures from ECR</a>
                </div>
            </div>
        </div>
    </body>
</html>

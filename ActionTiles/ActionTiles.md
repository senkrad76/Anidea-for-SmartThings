# ActionTiles Support Files
Hosted files to support an ActionTiles installation.

## tiletoiframe.js

This script converts specified tiles in a panel into iframes which can be used to display any chosen URL. It also allows the iframes to be added as targets for other URL Shortcuts in the panel so the contents can be dynamically changed.

It is probably best to convert URL Shortcuts or blank tiles as they are essentially static so removing their innards is unlikely to confuse anything. A URL Shortcut has the advantage that you can use the title, icon and the colour to make it obvious it hasn't been converted yet.

### Usage
The script needs to be edited to add the following information for each tile to be converted to an iframe.

* The `name` to be used for each iframe. This could be automatically generated but this allows for the possibility of ActionTiles being updated to support named target windows.
* The `at-tile-id` attribute of each tile's top level element, which is in UUID format. This can be found in a desktop version of Chrome by a right-click on a tile and choosing `Inspect`, then digging it out of the developer window.
* The default URL to be displayed in the iframe.
* The filter string to be used when converting URL Shortcuts to point at the iframe. This is currently matched with the start of each URL. If you don't need this just use something that won't match.

Each of the above are entered in separate arrays so the order needs to be the same in each (this is just how the code evolved).

In order to allow the iframes to display ActionTiles panels, allowance also needs to be made for the gutter around tiles which will vary depending on the size of your screeen, your choice of tiles, and the contents of your panel. The actual iframe is made a defined number of pixels bigger than the tile it replaces.

The script needs to be installed somewhere convenient and then needs to be called manually from the panel. This means defining it as a URL Shortcut with the URL `javascript: $.getScript('<path to this script>');`. If you are converting URL Shortcuts to iframes then one or more of them could have it as the underlying URL.

If you just want to display fixed URLs in each iframe, then that's all you need to do. Clicking the shortcut above will run the script and do the conversions for you.

If you want to be able to dynamically change which iframe is targetted by URL Shortcuts, or want to reset the iframe to it's default contents, you will need to use extra URL Shortcuts with the URL `javascript: anideatiletoiframe( index );` where `index` is the integer index of the iframe in the arrays in the script (which will start from 0).

### To do
* There needs to be a more flexible way of filtering the URL Shortcuts.

## arrivals.php and departures.php
These two PHP scripts are intended to provide content for an iframe, as above. The `arrivals.php` script querys the TfL Unified API for live arrivals details for buses and trams, and displays up to three times in a format compatible with an ActionTiles panel (__the CSS has to be custom designed to work with a 3x2 iframe on a particular size of panel on a particular size of screen__). The `departures.php` file does a similar thing for journeys between two defined National Rail stations, displaying the live departures from the origin but with an option to toggle to live arrivals at the destination.

## panel.php
This is a frameset document that can be configured to call an ActionTiles panel. It accommodates panels designed for a tablet with a screen width of 1280 pixels, and adjusts the scaling for other displays it runs on.

## weather.php
This PHP script provides content for an iframe, as above. It queries the Met Office DataPoint API for the next three forecasts from the 3 hourly 5 day feed and presents the information similarly to the `arrivals.php` and `departures.php` scripts. The regional text forecast for the next five days is also included. The UK wide outlook out to thirty days is stripped off but could just as easily be an option.

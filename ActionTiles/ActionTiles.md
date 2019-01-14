# ActionTiles Support Files
Hosted files to support an ActionTiles installation.

## tiletoiframe.js

This script converts specified tiles in a panel into iframes which can be used to display any chosen URL. It also allows the iframes to be added as targets for other URL Shortcuts in the panel so the contents can be dynamically changed.

_It is probably best to convert URL Shortcuts or blank tiles as they are essentially static so removing their innards is unlikely to confuse anything. A URL Shortcut has the advantage that you can use the title, icon and the colour to make it obvious it hasn't been converted yet._

### Usage
The top of script needs to be edited to define the following information:

* For each tile to be converted:
    * The `name` to be used for each iframe. This is used as the index of an associative array and as the name parameter of the HTML `<iframe>`.
    * The `id` of the tile. This is the `at-tile-id` attribute of each tile's top level element, which is in UUID format. This can be found in a desktop version of Chrome, for example, by a right-click on a tile and choosing `Inspect`, then digging it out of the developer window.
    * The `src` of the the iframe, which is a URL to be displayed in the iframe by default.
    * The `filter` which is a partial URL to be matched with the start of all of the URL Shortcuts in the panel, with matching URLs having their targets set to the `name`.
* A array containing the iframe names in the order they are to be converted. The purpose of this is to allow a particular iframe to be created last so it is the one being targetted by any matching URL Shortcuts.
* A size adjustment for the iframes so that they overlap the gutter around the original tile. This is necessary when ActionTiles panels are being loaded into iframes. This size required will depend on the size of your screeen, your choice of tiles, and the contents of your panel. 

The script needs to be installed somewhere convenient and then needs to be called manually from the panel. This means defining it as a URL Shortcut with the URL `javascript: $.getScript('<path to the script>');`. If you are converting URL Shortcuts to iframes then one or more of them could have it as the underlying URL.

If you just want to display fixed URLs in each iframe, then that's all you need to do. Clicking the shortcut above will run the script and do the conversions for you.

If you want to be able to dynamically change which iframe is targetted by URL Shortcuts, or want to reset the iframe to it's default contents, you will need to use extra URL Shortcuts with the URL `javascript: anideatiletoiframe( '<IFRAMENAME>' );` where `<IFRAMENAME>` is the `name` of an iframe.

### To do
* There needs to be a more flexible way of filtering the URL Shortcuts.

## arrivals.php and departures.php
These two PHP scripts are intended to provide content for an iframe, as above. The `arrivals.php` script querys the TfL Unified API for live arrivals details for buses and trams, and displays up to three times in a format compatible with an ActionTiles panel (__the CSS has to be custom designed to work with a 3x2 iframe on a particular size of panel on a particular size of screen__). The `departures.php` file does a similar thing for journeys between two defined National Rail stations, displaying the live departures from the origin but with an option to toggle to live arrivals at the destination.

## panel.php
This is a frameset document that can be configured to call an ActionTiles panel. It accommodates panels designed for a tablet with a screen width of 1280 pixels, and adjusts the scaling for other displays it runs on.

## weather.php
This PHP script provides content for an iframe as created by `tiletoiframe.js`. It is designed for a 3x2 tile and the CSS has been custom designed for a particular size of panel on a particular size of screen. It displays a row of three tiles showing the currrent weather for a location using a feed from OpenWeatherMap. It then displays another row of three tiles displaying the next three forecasts from the 3 hourly 5 day feed provided by the Met Office DataPoint API. This is followed by a 3x1 tile expanding on each of the forecasts (this can be brought into view by clicking on the appropriate forecast tile). The regional text forecast for the next five days from the Met Office DataPoint API follows. The UK wide outlook out to thirty days is stripped off but could just as easily be an option.

_OpenWeatherMap is used for the current observations because the Met Office DataPoint API (my preferred source) only provides hourly current observations and these can take half an hour to reach the feed, meaning the data can be up to an hour and a half old._

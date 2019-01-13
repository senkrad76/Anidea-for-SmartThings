# ActionTiles Support Files
Hosted files to support an ActionTiles installation.

## tiletoiframe.js

This JavaScript is designed to be called from a `javascript:` URL in a URL Shortcut tile in an ActionTiles panel. It searches the current panel for tiles with given IDs (the `at-tile-id` attributes which are in UUID format) and replaces each tile's child elements with an `iframe`. It then changes the targets of any other links in the window that begin with a specified string (i.e. the first part of a URL) so that they reference the iframe. The default content can be specified and also the `iframe` can be be created slightly bigger than the tile so it can accommodate an ActionTiles panel apparently seamlessly.

### Usage
Define one or more suitably sized tiles in a panel, each of which will be converted to an `iframe`. It is probably best to use URL Shortcuts or blank tiles as they are essentially static so removing their innards is unlikely to confuse anything. A URL Shortcut has the advantage that you can use the title, icon and the colour to make it obvious it hasn't been converted yet.

Track down the `at-tile-id` attribute of each tile's top level element. This can be done in a desktop version of Chrome by a right-click on a tile and choosing `Inspect`, then digging it out of the developer window. Add this to the appropriate array at the top of the script.

Define a URL Shortcut to call the script (see the script itself for details). This can be one or more of the tiles to be converted if you use URL Shortcuts.

Display the panel and click on the URL Shortcut above to create the iframes and show their default contents. Any other converted URL Shortcut tiles will then open in their associatied iframe. 

You may also define additional URL Shortcuts to reset an iframe to its default contents and update the targets.

### To do
* There needs to be a more flexible way of filtering the URL Shortcuts.

## arrivals.php and departures.php
These two PHP scripts are intended to provide content for an iframe, as above. The `arrivals.php` script querys the TfL Unified API for live arrivals details for buses and trams, and displays up to three times in a format compatible with an ActionTiles panel (__the CSS has to be custom designed to work with a 3x2 iframe on a particular size of panel on a particular size of screen__). The `departures.php` file does a similar thing for journeys between two defined National Rail stations, displaying the live departures from the origin but with an option to toggle to live arrivals at the destination.

## panel.php
This is a frameset document that can be configured to call an ActionTiles panel. It accommodates panels designed for a tablet with a screen width of 1280 pixels, and adjusts the scaling for other displays it runs on.

## weather.php
This PHP script provides content for an iframe, as above. It queries the Met Office DataPoint API for the next three forecasts from the 3 hourly 5 day feed and presents the information similarly to the `arrivals.php` and `departures.php` scripts. The regional text forecast for the next five days is also included. The UK wide outlook out to thirty days is stripped off but could just as easily be an option.

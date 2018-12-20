# ActionTiles Support Files
Hosted files to support an ActionTiles installation.

## tiletoiframe.js

This JavaScript is designed to be called from a `javascript:` URL in a URL Shortcut tile in an ActionTiles panel. It searches the current panel for a tile with a given ID (the `at-tile-id` attribute which is in UUID format) and replaces its child elements with an `iframe` which is assigned a name based on the tile ID. It then changes the targets of any other links in the window that begin with a specified string (i.e. the first part of a URL) so that they reference the iframe. The default source content can be specified and also the `iframe` can be be created slightly bigger than the tile so it can accommodate an ActionTiles panel apparently seamlessly.

### Usage
Define a suitably sized tile in the panel to be converted to an `iframe` and track down the `at-tile-id` attribute of its top level element. This can be done in a desktop version of Chrome by a right-click on a tile and choosing `Inspect`, then digging it out of the developer window. It is probably best to use a URL Shortcut or a blank tile as they are essentially static so removing their innards is unlikely to confuse anything. The URL Shortcut has the advantage that you can use the title, icon and the colour to make it obvious it hasn't been converted yet.

Define a URL Shortcut that defines some variables and calls the script (see the script itself for the variable names and how to call the script). This can be the tile itself if you won't need to reset the tile to the default contents and/or reset the targets.

Display the panel and click on the URL Shortcut to create an iframe. Any converted URL Shortcut tiles will then open in the iframe. Each time you click on the URL Shortcut the iframe will be reset to its default contents and the targets updated.

You may have multiple iframes.

### To do
* There needs to be a more flexible way of filtering the URL Shortcuts.
* If ActionTiles develops the ability to specify targets on URL Shortcuts it will be useful to be able to define simpler names for the iframes. Until then the `iframe<at-tile-id>` format will be fine.

##arrivals.php and departures.php
These two PHP scripts are intended to provide content for an iframe, as above. The `arrivals.php` script querys the TfL Unified API for live arrivals details for buses and trams, and displays up to three times in a format compatible with an ActionTiles panel (the CSS has to be custom designed to work with a 3x2 iframe on a particular size of panel on a particular screen). The `departures.php` file does a similar thing for journeys between two defined National Rail stations, displaying the live departures from the origin but with an option to toggle to live arrivals at the destination.

## panel.php
This is a frameset document that can be configured to call an ActionTiles panel. It accommodates panels designed for a tablet with a screen width of 1280 pixels, and adjusts the scaling for other displays it runs on.

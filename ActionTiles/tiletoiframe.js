//
// Here are the interesting configurables to save scrolling past the text below.
//
if ( typeof anideatileid       === 'undefined' ) var anideatileid       = "<at-tile-id>";
if ( typeof anideaiframesrc    === 'undefined' ) var anideaiframesrc    = "<url>";
if ( typeof anideahreffilter   === 'undefined' ) var anideahreffilter   = "<start-of-url>";
if ( typeof anideaiframeadjust === 'undefined' ) var anideaiframeadjust = 4;

//
// (C) Graham Johnson 2018.
//
// This script changes the contents of an ActionTiles tile, specified by the tile's UUID,
// to an iframe and then changes the target of specified URL shortcuts to that iframe.
//
// It can be called by specifying a URL shortcut as "javascript: $.getScript('<path to this file>');".
// The configurable variables may also be set in the URL to allow for more than one instance of an
// iframe with the format "javascript: anideatileid = '<uuid>'; $.getScript('<path to this file>');".
//
// This URL exploits jQuery which is already loaded into ActionTiles. Obviously the entire script could
// be written in jQuery and probably be all the better for it but I currently only know one bit of jQuery
// and that was it.
//
// The script can be placed directly into the URL shortcut but it is a pain to edit there. The intended
// usage of the iframe is for external files so it might as well be an externally hosted script.
//
// The variable names have been given 'anidea' prefix just to try and make sure they don't clash
// with anything in ActionTiles. This may or may not be necessary but when testing things it seemed to
// help, though this may have been coincidence, and it doesn't do any harm.
//

//
// ActionTiles assigns each tile an 'at-tile-id' attribute with a UUID. You have to dig into the DOM to find this.
// There may be a better way but this does work.
//
// The querySelector function is being used to search for the tile with the given at-tile-id.
//
var anideatile = document.querySelector( '[at-tile-id="' + anideatileid + '"]' );

//
// There should be a tile found. Error checking might be a bit of a luxury had an external script
// not been used. The tile gets it's existing innards replaced by an iframe which is the full size of the
// existing tile.
//
if ( anideatile )
{
    console.log( 'Info: Tile ' + anideatileid + ' found.');
    
    //
    // Generate a unique name from the tile id and then if it hasn't already been done, replace the 
    // innards of the tile with an iframe. It isn't going to do any harm to do this every time
    // but might as well check it has been done.
    //

    var anideaiframename = "iframe" + anideatileid;
    var anideaiframe     = document.querySelector( '[name="' + anideaiframename + '"]');

    if ( anideaiframe === null )
    {
        //
        // It may well be better to choose a static tile to manipulate. A URL Shortcut can 
        // call this script itself and can have its title, icon and background colour changed.
        // A blank tile would be fine too. With dynamic tiles removing their innards might
        // confuse things.
        //
        // A panel would not fit in a 100% width iframe because of its gutter. Therefore increase the
        // size of the iframe and shift it to cover the gutter. Fortunately the AT tiles have absolute
        // positions so a relative position can be used.
        //
        while(anideatile.firstChild)
        {
            anideatile.removeChild(anideatile.firstChild);
        }
        
        anideaiframe = document.createElement( 'iframe' );
        
        anideaiframe.setAttribute( "name", anideaiframename );
        
        anideaiframe.style.border = "0";
        
        var anideaiframewidth  = anideatile.offsetWidth   + ( 2 * anideaiframeadjust);
        var anideaiframeheight = anideatile.offsetHeight  + ( 2 * anideaiframeadjust);
        
        anideaiframe.style.width  = anideaiframewidth  + 'px';
        anideaiframe.style.height = anideaiframeheight + 'px';
        
        anideaiframe.style.position = "relative";
        
        anideaiframe.style.left = '-' + anideaiframeadjust + 'px';
        anideaiframe.style.top  = '-' + anideaiframeadjust + 'px';
        
        anideatile.appendChild( anideaiframe )
    }
    
    //
    // Might as well reset the contents every time this script gets run. That way
    // a URL Shortcut tile can act as a select and reset button for the iframe.
    //
    anideaiframe.setAttribute( "src",  anideaiframesrc  );
        
    //
    // Look for all the links in the document. ActionTiles uses buttons for media tiles so these should all be
    // URL Shortcut tiles. Choose the ones we are interested in and change their target to point at the iframe.
    //
    var anidealinks = document.getElementsByTagName("a");

    var anideachanged = 0;
    
    for (var i = 0; i < anidealinks.length; i++)
    {
        if (anidealinks[i].href.startsWith( anideahreffilter ))
        {
            anidealinks[i].target  = anideaiframename;
            
            anideachanged++;
        }
    }
    
    console.log( 'Info: ' + anideachanged + ' of ' + anidealinks.length + ' link targets changed.' );
}
else
{
    console.log( 'Error: Tile ' + anideatileid + ' not found.' );
}

//
// Here are the interesting configurables to save scrolling past the text below.
//

//
// Array defining the names to be used for each iframe.
//
var anideaiframename   = [ 'IFRAME1',
                           'IFRAME2',
                           'IFRAME3']
//
// Array defining the at-tile-id of each tile to be converted (same order as above).
//
var anideatileid       = [ "<UUID1}",
                           "<UUID2>",
                           "<UUID3?" ];
//
// Array defining the default source for each iframe (same order as above).
//
var anideaiframesrc    = [ "<SRC1>",
                           "<SRC2>",
                           "<SRC3>" ];
//
// Array defining the filter to be applied to URL shortcuts for each iframe.
//
var anideahreffilter   = [ "<PREFIX1>",
                           "<PREFIX2>",
                           "<PREFIX3>" ];
//
// Number of pixels to adjust the size of the iframe by to account for the gutter around tiles.
//
var anideaiframeadjust = 4;

//
// (C) Graham Johnson 2018.
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
// This script defines a number of ActionTiles tiles, defined by a UUID, which are to have their
// contents changed to be an iframe, and then converts them, initialising them with specified URLs
// and then changing any URL shortcuts in the panel that start with a specified string to use the
// iframe as their target.
//
// The script needs to be called manually after a panel is loaded. This can conveniently be done by
// using URL Shortcuts as the tiles to be converted and defining the URL of one or more of them as:
//
//      javascript: $.getScript('<path to this file>');
//
// Clicking on one of the tiles will then run this script.
//
// This URL exploits jQuery which is already loaded into ActionTiles. Obviously the entire script could
// be written in jQuery and probably be all the better for it but I currently only know one bit of jQuery
// and that was it.
//
// You may want to reset iframes to their default sources and/or make them active for URL Shortcuts. If
// so you should add URL shortcuts containing:
//
//      javascript: anideatiletoiframe( index );
//
// The argument 'index' will be the integer index of the iframe in the global variable arrays, starting
// with 0.
//

anideatileid.forEach( anideatiletoiframe );

function anideatiletoiframe( dummy, iframe = dummy )
{
    //
    // ActionTiles assigns each tile an 'at-tile-id' attribute with a UUID. You have to dig into the DOM to find this.
    // There may be a better way but this does work.
    //
    // The querySelector function is being used to search for the tile with the given at-tile-id.
    //
    var anideatile = document.querySelector( '[at-tile-id="' + anideatileid[ iframe ] + '"]' );

    //
    // There should be a tile found. Error checking might be a bit of a luxury had an external script
    // not been used. The tile gets it's existing innards replaced by an iframe which is the full size of the
    // existing tile.
    //
    if ( anideatile )
    {
        console.log( 'Info: Tile ' + anideatileid[ iframe ] + ' found.');
    
        //
        // Generate a unique name from the tile id and then if it hasn't already been done, replace the 
        // innards of the tile with an iframe. It isn't going to do any harm to do this every time
        // but might as well check it has been done.
        //

        var anideaiframe     = document.querySelector( '[name="' + anideaiframename[ iframe ] + '"]');

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
        
            anideaiframe.setAttribute( "name", anideaiframename[ iframe ] );
        
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
        anideaiframe.setAttribute( "src",  anideaiframesrc[ iframe ]  );
        
        //
        // Look for all the links in the document. ActionTiles uses buttons for media tiles so these should all be
        // URL Shortcut tiles. Choose the ones we are interested in and change their target to point at the iframe.
        //
        var anidealinks = document.getElementsByTagName("a");

        var anideachanged = 0;
    
        for (var i = 0; i < anidealinks.length; i++)
        {
            if (anidealinks[i].href.startsWith( anideahreffilter[ iframe ] ))
            {
                anidealinks[i].target  = anideaiframename[ iframe ];
            
                anideachanged++;
            }
        }
    
        console.log( 'Info: ' + anideachanged + ' of ' + anidealinks.length + ' link targets changed.' );
    }
    else
    {
        console.log( 'Error: Tile ' + anideatileid[ iframe ] + ' not found.' );
    }
}

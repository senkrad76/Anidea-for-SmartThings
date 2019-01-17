//
// tiletoiframe.js (C) Graham Johnson 2018-2019
// ============================================
// Version: 1.0.0   14/01/2019
//

//
// Here are the interesting configurables to save scrolling past the text below.
//

// 
// Order to initialise iframes in (useful to initialise a particular
// iframe last so it is being targetted by shortcuts).
//
var anideaframeorder = [ 'IFRAME1' ];

//
// Array defining the iframes.
//
var anideaiframes   = [];

anideaiframes['IFRAME1'] = { "id"  : "UUID",
                             "src" : "URL",
                             "filter" : "PARTIAL URL" };

//
// Number of pixels to adjust the size of the iframe by to account for the gutter around tiles.
//
var anideaiframeadjust = 4;

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
// This script defines a number of ActionTiles tiles which are to have their contents changed to 
// be an iframe, and then converts them, initialising them with specified URLs and then changing
// the target of any URL Shortcuts in the panel that start with a specified string to the iframe.
//
// The script needs to be called manually after a panel is loaded. This can conveniently be done by
// having at least one of the tiles to be converted be a URL Shortcut and defining the URL to be:
//
//      javascript: $.getScript('<path to this file>');
//
// Clicking on one the tile will then run this script.
//
// This URL exploits jQuery which is already loaded into ActionTiles. Obviously the entire script could
// be written in jQuery and probably be all the better for it but I currently only know one bit of jQuery
// and that was it.
//
// You may want to reset iframes to their default sources and/or make them active for URL Shortcuts. If
// so you should add URL shortcuts containing:
//
//      javascript: anideatiletoiframe( '<IFRAME NAME>' );
//

anideaframeorder.forEach( anideatiletoiframe );

function anideatiletoiframe( iframe )
{
    //
    // ActionTiles assigns each tile an 'at-tile-id' attribute with a UUID. You have to dig into the DOM to find this.
    // There may be a better way but this does work.
    //
    // The querySelector function is being used to search for the tile with the given at-tile-id.
    //
    var anideatile = document.querySelector( '[at-tile-id="' + anideaiframes[ iframe ][ 'id' ] + '"]' );

    //
    // There should be a tile found. Error checking might be a bit of a luxury had an external script
    // not been used. The tile gets it's existing innards replaced by an iframe which is the full size of the
    // existing tile.
    //
    if ( anideatile )
    {
        console.log( 'Info: Tile ' + anideaiframes[ iframe ][ 'id' ] + ' found.');
    
        //
        // Generate a unique name from the tile id and then if it hasn't already been done, replace the 
        // innards of the tile with an iframe. It isn't going to do any harm to do this every time
        // but might as well check it has been done.
        //

        var anideaiframe = document.querySelector( '[name="' + iframe + '"]');

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
        
            anideaiframe.setAttribute( "name", iframe);
        
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
        anideaiframe.setAttribute( "src",  anideaiframes[ iframe ][ 'src' ] );
        
        //
        // Look for all the links in the document. ActionTiles uses buttons for media tiles so these should all be
        // URL Shortcut tiles. Choose the ones we are interested in and change their target to point at the iframe.
        //
        var anidealinks = document.getElementsByTagName("a");

        var anideachanged = 0;
    
        for (var i = 0; i < anidealinks.length; i++)
        {
            if (anidealinks[i].href.startsWith( anideaiframes[ iframe ][ 'filter' ] ))
            {
                anidealinks[i].target  = iframe;
            
                anideachanged++;
            }
        }
    
        console.log( 'Info: ' + anideachanged + ' of ' + anidealinks.length + ' link targets changed.' );
    }
    else
    {
        console.log( 'Error: Tile ' + anideaiframes[ iframe ][ 'id' ] + ' not found.' );
    }
}

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>
    <head>
        <meta name="mobile-web-app-capable" content="yes">
        <meta name="viewport" content="width=device-width, initial-scale=1.0" id="metaviewport">
        <link rel="icon" type="image/png" href="at-fav.png">
        <title><PANEL NAME></title>
        <script type="text/javascript">
        
            // The ActionTiles panels have been designed to fit nicely on a tablet with
            // a width of 1280 CSS pixels. This script scales things for other displays.
            // It's not really responsive design as such, it just makes the one design scale.

            var metaviewport = document.getElementById('metaviewport');

            function setviewport()
            {
                var initialscale = screen.width / 1280;
		    
		        metaviewport.setAttribute( 'content', 'width=device-width, initial-scale=' + initialscale );
	        }

            setviewport();

            window.addEventListener('resize', setviewport);
            
        </script>
    </head>
    <frameset>
        <frame src="https://app.actiontiles.com/panel/<PANELID>" border="0" frameborder="0"> 
    </frameset>
</html>

/*
 * ---------------------------------------------------------------------------------
 * (C) Graham Johnson (orangebucket)
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby granted, provided that the copyright notice below
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH 
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER 
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 * ---------------------------------------------------------------------------------
 *
 * Lan MultiThing ETA Child
 * ========================
 * Version: 20.02.06.00
 *
 * This device handler implements a child ETA device to work with LAN MultiThing.
 *
 * Please be aware that this file is created in the SmartThings Groovy IDE and it may
 * format differently when viewed outside that environment.
 */

metadata
{
	definition (name: "LAN MultiThing ETA Child", namespace: "orangebucket", author: "Graham Johnson")
    {
		capability "Sensor"
        capability "Estimated Time Of Arrival" 
	}
        
	preferences
	{
	}
    
	// One day I will investigate this.
	simulator
    {
	}

	tiles
    {        
        valueTile("eta", "device.eta", decoration: "flat", width: 2, height:1)
        {
        	state "eta", label:'ETA ${currentValue}'
    	} 
        
        main "eta"
        // Sort the tiles suitably.
        details (["eta"])
	}
}

def installed()
{
	logger("installed")
    
    updated()
}


// The updated() command is called when preferences are saved. It often seems
// to be called twice so an attempt is made to only let it run once in a five
// second period.
def updated()
{
	if (state.lastupdated && now() < state.lastupdated + 5000)
    {        
        logger("updated", "Skipped as ran recently")
 
 		return
    }
        
 	state.lastupdated = now()

	logger("updated")
}

// Have own logging routine.
def logger(method, level = "debug", message ="")
{
	log."${level}" "$device.displayName [$device.name] [${method}] ${message}"
}

import groovy.json.JsonSlurper 

def parse(description)
{
	def msg = parseLanMessage(description)
 
	if (msg.body)
    {
     	def jsonSlurper = new JsonSlurper()
    	def body = jsonSlurper.parseText(msg.body)
    	def children = getChildDevices()

	   	body.attribute.each
		{
			myname, myvalue ->
                                    
        	if (myname == "eta")
        	{
				try
            	{
					def mytime = Date.parse("yyyy-MM-dd'T'HH:mm:ssX", myvalue).format("HH:mm", location.getTimeZone())
                        
                	logger("parse", "info", "attribute $myname $myvalue (${mytime})")
                    
                	sendEvent(name: myname, value: myvalue, isStateChange: true)
            	}
            	catch(Exception e)
            	{
					// The returned value is not an ISO8601 date.
					logger("parse", "info", "attribute $myname (value invalid)")

					sendEvent(name: myname, value: null, isStateChange: true)
            	}
			}
        }
	}
}




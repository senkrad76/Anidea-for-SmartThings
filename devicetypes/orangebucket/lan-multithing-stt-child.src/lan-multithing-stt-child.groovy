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
 * Lan MultiThing STT Child
 * ========================
 * This device handler implements a child Speech Recognition (STT) device to work
 * with LAN MultiThing. The Speech Recognition capability only has an attribute so
 * could be used for generic text.
 *
 * Author:	Graham Johnson (orangebucket)
 *
 * Version:	1.0.1	(30/10/2018) 
 *
 * Comments:			
 *
 * Changes:
 *
 * 1.0.1	(30/10/2018)	Correct the parsing.
 * 1.0.0	(15/10/2018)	Initial version.
 *
 * Please be aware that this file is created in the SmartThings Groovy IDE and it may
 * format differently when viewed outside that environment.
 */

preferences
{
}

metadata
{
definition (name: "LAN MultiThing STT Child", namespace: "orangebucket", author: "Graham Johnson")
    {
		capability "Sensor"
        capability "Speech Recognition" 
	}
        
	// One day I will investigate this.
	simulator
    {
	}

	tiles
    {        
        valueTile("sst", "device.phraseSpoken", decoration: "flat", width: 3, height:1)
        {
        	state "phraseSpoken", label:'${currentValue}'
    	} 
        
        main "sst"
        // Sort the tiles suitably.
        details (["sst"])
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
                                    
        	if (myname == "phraseSpoken")
        	{
				logger("parse", "info", "attribute $myname $myvalue")

				sendEvent(name: myname, value: myvalue, isStateChange: true)
			}
        }
	}
}
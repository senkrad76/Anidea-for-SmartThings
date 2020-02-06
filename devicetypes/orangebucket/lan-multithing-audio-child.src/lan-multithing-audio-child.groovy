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
 * Lan MultiThing Audio Child
 * ==========================
 * Version:	20.02.06.00
 *
 * This device handler implements a child audio device to work with LAN MultiThing.
 *
 * Please be aware that this file is created in the SmartThings Groovy IDE and it may
 * format differently when viewed outside that environment.
 */

metadata
{
	definition (name: "LAN MultiThing Audio Child", namespace: "orangebucket", author: "Graham Johnson")
    {
		capability "Actuator"
        capability "Speech Synthesis" 
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
        // This tile sends a sample text to be spoken. Speech is a dummy attribute.
        standardTile("speechSynthesis", "device.speech", width: 1, height: 1)
        {
            state "speech", label:'Speak', action:'Speech Synthesis.speak', icon:"st.Entertainment.entertainment3", backgroundColor:"#800080", defaultState: true
        }
        main "speechSynthesis"
        // Sort the tiles suitably.
        details (["speechSynthesis"])
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
 
    logger("parse", "debug", description)
}

//
// Speech Synthesis
//

def speak(words)
{
    if (!words?.trim()) words = device.name
   
   	logger("speak", "debug", "${device} ${words}")
    
	parent.childspeak(device, words)
}

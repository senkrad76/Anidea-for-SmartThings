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
 * AncillaryAT
 * ===========
 * Bla bla.
 *
 * Author:				Graham Johnson (orangebucket)
 *
 * Version:				1.0.0	(21/11/2018) 
 *
 * Comments:
 *
 * None.
 *
 * Changes:
 *
 * 1.0.0		(21/11/2018)	Initial noodlings.
 *
 * Please be aware that this file is created in the SmartThings Groovy IDE and it may
 * format differently when viewed outside that environment.
 */
 
definition(
    name: "AncillaryAT",
    namespace: "orangebucket",
    author: "Graham Johnson",
    description: "Does things.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "AncillaryAT", displayLink: "http://localhost"])


preferences
{
	section ("Allow external service to control these things...")
    {
		input "etas", "capability.estimatedTimeOfArrival", multiple: true, required: true
	}
}

mappings
{
	path("/devices")
	{
		action: [
        	GET: "listdevices"
    	]
	}
}

def listdevices()
{
	def resp = []
    
    etas.each
    {
		log.debug "$it.displayName ${it.etaState["value"]}"
        resp << [name: it.displayName, value: it.etaState["value"]]
    }
    return resp
}

def installed() {}

def updated() {}
# REST API OAuth

I just wanted somewhere to keep some notes on how to get an OAuth2 access token for a Web Services SmartApp. This process can be automated in apps by tweaking the redirect URL. The docs on Service Manager SmartApps show apps being able to create their own access token and find their own endpoint. That procedure is supposedly for temporary tokens for use during authentication of third party services but it rather appears some SmartApps use them instead of going through the palaver below. That's all rather baffling. Anyway ...

Here is a simple SmartApp:

```groovy
definition(
    name: "REST API OAuth",
    namespace: "orangebucket",
    author: "Graham Johnson",
    description: "Does things.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "REST API OAuth", displayLink: "http://localhost"])


preferences
{
	section ("Allow external service to control these things...")
    {
		input "etas", "capability.estimatedTimeOfArrival", multiple: true, required: true
        input "stts", "capability.speechRecognition", multiple: true, required: true
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
        
        // I rather expected currentValue("eta") to work, but no such luck.
        resp << [name: it.displayName, value: it.etaState["value"]]
    }
    
    stts.each
    {
		log.debug "$it.displayName ${it.phraseSpokenState["value"]}"
        
        resp << [name: it.displayName, value: it.phraseSpokenState["value"]]
    }   
    return resp
}

def installed() {}

def updated() {}
```

OAuth needs to be enabled in the SmartApp 'App Settings' in the IDE. This creates two UUIDs, the 'Client ID' and the 'Client Secret' which we'll refer to as CLIENTID and CLIENTSECRET. The CLIENTID code is then used to request an authorisation code for the app. This is something you need to do in a browser as you will be authenticating with SmartThings and specifying which devices you wish to authorise the app to access in the usual way for third party access. The URL will be:

`https://graph.api.smartthings.com/oauth/authorize?response_type=code&client_id=CLIENTID&scope=app&redirect_uri=http%3A%2F%2Flocalhost`

If you successfully authenticate and authorise the app you will be redirected to `http://localhost/?code=AUTHCODE` where AUTHCODE is a six character code that is valid for one use within twenty-four hours. You'll also get an error message but it is the URL that is important.

The next step is to use AUTHCODE to get an OAuth2 authentication token. This involves the CLIENTID and the CLIENTSECRET codes. This may well work as an HTTP GET but the documentation uses HTTP PUT, which seems a better way of doing things. The following curl command does the job:

`curl --data "grant_type=authorization_code&code=AUTHCODE&client_id=CLIENTID&client_secret=CLIENTSECRET&redirect_uri=http%3A%2F%2Flocalhost" https://graph.api.smartthings.com/oauth/token`

At the time of writing, using the same AUTHCODE twice seems to result in an 'Internal Error' page being returned rather than failing elegantly (assuming it is only valid for one use and is supposed to fail). All being well you should receive a JSON format associative array including `"access_token":"ACCESSTOKEN"` where ACCESSTOKEN is a UUID.

The next step is to get an endpoint for the application. Again this can be done using curl.

`curl -H "Authorization: Bearer ACCESSTOKEN" https://graph.api.smartthings.com/api/smartapps/endpoints`

This should return a JSON list of associative arrays including `"uri":"https://HOST:443/api/smartapps/installations/ENDPOINT"` (the HOST may vary, and ENDPOINT is again a UUID).

You can then use this endpoint to talk to the SmartApp. So if you wanted to access '/devices' as defined in the app it would, in curl terms, be:

`curl -H "Authorization: Bearer ACCESSTOKEN" https://HOST:443/api/smartapps/installations/ENDPOINT/devices`

In PHP:

```php
    $resturl = 'https://HOST:443/api/smartapps/installations/ENDPOINT/devices';
    $access_key = 'ACCESSTOKEN';
    $ch = curl_init($resturl);
    curl_setopt( $ch, CURLOPT_HTTPHEADER, array( 'Authorization: Bearer ' . $access_key ) );
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
    curl_setopt($ch, CURLOPT_POST,           0 );
    $resp =  curl_exec($ch);
    curl_close($ch);
    $json = json_decode($resp,true);
```


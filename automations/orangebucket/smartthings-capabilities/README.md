# SmartThings Capabilities
This is a simple PHP script to pull the latest list of capabilities from the SmartThings REST API. As there are over two hundred capabilities the list is cached for two days. The script uses the [Anidea for WebHook Wrapper](#anidea-for-webhook-wrapper) just because it can.

It requires a Personal Access Token (<https://account.smartthings.com/tokens>). One with scope to access custom capabilities will work (so might others but they haven't been checked).

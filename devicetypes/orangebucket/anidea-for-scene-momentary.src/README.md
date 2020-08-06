# Anidea for Scene Momentary
This is a simple device handler that uses the Momentary capability (and thus a virtual button in the mobile app, and a `push()` command for other apps such as ActionTiles and webCoRE) and executes a Scene using the SmartThings REST API. It is an alternative to having to mess about creating HTTPS POST requests to execute scenes, or having a virtual switch and a trivial automation.

The device needs to be configured with a Personal Access Token (https://account.smartthings.com/tokens) with suitable scope to control a Scene, and the device ID of the Scene (which is arguably easiest to find in the IDE by using 'List Scenes' on your Location details page).

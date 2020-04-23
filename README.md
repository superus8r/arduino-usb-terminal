# arduino-usb-terminal
 Terminal like app to send commands to Arduino through USB
 
 This app simplifies testing your Arduino components that work with direct usb commands by giving you the ability to send custom commands and view the returned message from your Arduino device.
 
 This is being done as a hobby, and for experimenting, so probably there might be some flaws; As an example, the vendor ID of Arduino is hardcoded to only work with Arduino devices, but this is my use case and please feel free to change it to match your needs.
 
 ## Terminal
 A Simple terminal page which does what it is supposed to do interacting with an Arduino manually through the USB cable.
 
 ## Joystick
 Under construction...
 
 ### Knows Issues
 _On Android 5.1.1, the Arduino serial output cannot be shown. (It is said that an Android internal bug is the issue!)_ This was hopefully solved using LiveData
 The Arduino output characters might be shown a bit weird in the app while skipping some characters when the message is too long. This will be fixed as I figure out the reason! Any suggestions will be appreciated. :) 
 
 Suggestions and PRs are welcome! :)
 
 ### More comes as the project evolves...
<img src=".github/images/logo.png" width="128">

# arduino-usb-terminal 
 Terminal-like app to send commands to Arduino through USB

![Android CI](https://github.com/k4biri/arduino-usb-terminal/workflows/Android%20CI/badge.svg)

 
 This app simplifies testing your Arduino components that work with direct usb commands by giving you the ability to send custom commands and view the returned message from your Arduino device.
 
 This is being done as a hobby, and for experimenting, so probably there might be some flaws; As an example, the vendor ID of Arduino is hardcoded to only work with Arduino devices, but this is my use case and please feel free to change it to match your needs.
 
 # Build and Run
 The app is available for free on Google Play Store (Arduino USB Terminal).
 Otherwise, you can clone the project and run it locally.
 Please read the **Sentry Reports** part on this page before running the project to avoid build failures. 

  <a href='https://play.google.com/store/apps/details?id=org.kabiri.android.usbterminal&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="128"/></a>
 
 ## Terminal
 A Simple terminal page which does what it is supposed to do interacting with an Arduino manually through the USB cable.
 
 ## Joystick
 The Joystick is removed for the first release.
 
 ## Tests
 Under Construction
 
 ## Sentry Reports
 The project uses Sentry for the crash reports, if this is not needed, you can remove the following line in `AndroidManifest.xml`:
 `<meta-data android:name="io.sentry.dsn" android:value="@string/sentry_dsn" />`
 But if it is needed, you need to [create a Sentry dsn value](https://docs.sentry.io/platforms/android/) to put under the following path:
 `app/src/main/res/values/api_keys.xml`
 The file contents might look like similar to this:
 `<?xml version="1.0" encoding="utf-8"?>
  <resources>
      <string name="sentry_dsn" translatable="false">YOUR_SENTRY_SPECIFIC_VALUE</string>
  </resources>`
 
 
 ### Knows Issues
 _Still unknown! :) 
 
 Suggestions and PRs are welcome! :)
 
 ### More comes as the project evolves...
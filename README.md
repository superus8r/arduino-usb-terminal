<img src=".github/images/logo.png" width="128">

# arduino-usb-terminal 
Simplify testing your IoT projects by using your Android device to send commands to Arduino through USB.

![Android CI](https://github.com/k4biri/arduino-usb-terminal/workflows/Android%20CI/badge.svg)
[![superus8r](https://circleci.com/gh/superus8r/arduino-usb-terminal.svg?style=shield)](https://circleci.com/gh/superus8r/arduino-usb-terminal)
[![codecov](https://codecov.io/gh/superus8r/arduino-usb-terminal/branch/develop/graph/badge.svg?token=RYIUU345QG)](https://codecov.io/gh/superus8r/arduino-usb-terminal)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=superus8r_arduino-usb-terminal&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=superus8r_arduino-usb-terminal)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=superus8r_arduino-usb-terminal&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=superus8r_arduino-usb-terminal)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=superus8r_arduino-usb-terminal&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=superus8r_arduino-usb-terminal)

 Ever thought of testing your Arduino project on the go without using a lap top?

 Sometimes, you just want to send simple commands to an Arduino through USB without getting that 1.4 kilogram laptop out of the bag! 😉
 
 Especially if the Android phone in your pocket has enough resources to do that!
 
 This is being done as a hobby, and for experimenting, so probably there might be some flaws; As an example, the vendor ID of Arduino is set to only work with Arduino devices, but this is my use case and please feel free to change it to match your needs. Or if you have a great idea to make this dynamic wile keeping the app simple, please feel free to open a pull request!
 
 # Build and Run
 The app is available for free on Google Play Store (Arduino USB Terminal).
 Otherwise, you can clone the project and run it locally.
 Please read the **Sentry Reports** part on this page before running the project to avoid build failures. 

  <a href='https://play.google.com/store/apps/details?id=org.kabiri.android.usbterminal&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="128"/></a>


 # Tests
 You can run all the instrumented tests using a Gradle Managed Device in one line:
```
./gradlew pixel2api30DebugAndroidTest
```
This command will run an Android emulator on the background and run all the tests.
After a successful run, the test coverage file will be available in your build folder:
```
app/build/outputs/managed_device_code_coverage/pixel2api30/coverage.ec
```
To create a unified coverage report you can use the Jacoco task:
```
./gradlew jacocoTestReport
// Jacoco HTML and XML output will be under the following path:
app/build/mergedReportDir/jacocoTestReport
```
Gradle automatically recognizes your environment and configures, runs, and closes the emulator in the background.
This has been tested on environments with Arm (Apple M1) and Intel CPUs.

More info about Gradle managed devices in official Android testing docs: [Scale your tests with Gradle Managed Devices](https://developer.android.com/studio/test/gradle-managed-devices)


 
 ## Firebase Crashlytics Reports
 The project uses Firebase Crashlytics for the crash reports, therefore you will need to create a free Firebase project to use it.
 - Once you create a Firebase project, Firebase will provide you with a config file (`google-services.json`).
 - Place your `google-services.json` file under the `app/` directory and build the project to activate it.
 
 More info on [Firebase official docs for getting started with Firebase Crashlytics](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
 
 If this is not needed, you can remove the crashlytics dependency in project leve and app level build.gradle files.
 - remove `classpath "com.google.firebase:firebase-crashlytics-gradle:2.9.4"` from `build.gradle` file
 - remove `implementation("com.google.firebase:firebase-crashlytics-ktx")` from `app/build.gradle.kts` file


## Sonar Cloud Analysis
The project uses Sonar Cloud manual analysis to detect code smells and potential bugs
To run the manual analysis locally, use the gradle `sonar` task:
```
./gradlew sonar
```
Since this uses the gradle scanner, the sonar properties are defined in root project's `build.gradle` file. 
On one hand, the Sonar scanner requires local paths for `sonar.sources` and `sonar.binaries` properties, on the other hand it requires absolute path for `sonar.androidLint.reportPaths` and `sonar.coverage.jacoco.xmlReportPaths`.
More info on official Sonar docs: [SonarScanner for Gradle](https://docs.sonarcloud.io/advanced-setup/ci-based-analysis/sonarscanner-for-gradle/) 


## Dependency Verification

This project uses [Gradle Dependency Verification](https://docs.gradle.org/current/userguide/dependency_verification.html) to check the authenticity dependencies.

The following files are committed to the repository:
- `gradle/verification-metadata.xml`
- `gradle/verification-keyring.keys`
- `gradle/verification-metadata.gpg`
**For contributors: if you add or update dependencies, regenerate these files with:**
```
./gradlew --write-verification-metadata pgp,sha256 --export-keys
```
Then commit the updated files.
This improves supply chain security and hopefully helps prevent unauthorized dependency changes.
 
 ## Knows Issues
- Jacoco coverage report is incorrect
- Please feel free inform me about new issues
 
 
 Suggestions and PRs are welcome! :)
 
---
 ### More comes as the project evolves...
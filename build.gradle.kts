buildscript {
    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.gradle)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sonarqube)
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

sonarqube {
    properties {
        property("sonar.organization", "superus8r")
        property("sonar.projectKey", "superus8r_arduino-usb-terminal")
        property("sonar.projectName", "arduino-usb-terminal")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.host.url", "https://sonarcloud.io")

        property("sonar.binaries", project(":app").layout.buildDirectory.dir("tmp/kotlin-classes/debug").get().asFile.absolutePath)
        property("sonar.androidLint.reportPaths", project(":app").layout.buildDirectory.dir("reports/lint-results-debug.xml").get().asFile.absolutePath)
        property("sonar.coverage.jacoco.xmlReportPaths", project(":app").layout.buildDirectory.dir("mergedReportDir/jacocoTestReport/jacocoTestReport.xml").get().asFile.absolutePath)
    }
}

buildscript {
    extra.apply {
        set("coroutines_version", "1.6.4")
        set("firebase_bom_version", "32.8.0")
        set("hilt_version", "2.56.2")
        set("jacoco_version", "0.8.8")
        set("kotlin_version", "2.1.20")
        set("material_version", "1.12.0")
        set("mockk_version", "1.14.2")
    }
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:${extra["hilt_version"]}")
        classpath("com.google.gms:google-services:4.4.1")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
    }
}

plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
    id("org.sonarqube") version "3.5.0.2730"
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

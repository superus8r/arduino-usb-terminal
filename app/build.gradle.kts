import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("jacoco")
}

repositories {
}

android {

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileSdk = 35
    defaultConfig {
        applicationId = "org.kabiri.android.usbterminal"
        minSdk = 24
        targetSdk = 35
        versionCode = System.getenv("CIRCLE_BUILD_NUM")?.toIntOrNull() ?: 14
        versionName = "0.9.84${System.getenv("CIRCLE_BUILD_NUM") ?: ""}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {

        val ksName = "keystore.properties"
        val ksProp = loadKeyStore(ksName)
        ksProp?.let {
            create("release") {
                keyAlias = ksProp.getProperty("release.keyAlias")
                keyPassword = ksProp.getProperty("release.keyPassword")
                storeFile = file(ksProp.getProperty("release.file"))
                storePassword = ksProp.getProperty("release.storePassword")
            }
        }
    }

    buildTypes {
        named("release") {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        named("debug") {
            isTestCoverageEnabled = true
        }
    }

    testOptions {

        animationsDisabled = true

        @Suppress("UnstableApiUsage")
        managedDevices {
            allDevices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "google_atd"
                }
            }
        }
    }

    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }

    namespace = "org.kabiri.android.usbterminal"

}

jacoco {
    val jacoco_version: String by project
    toolVersion = jacoco_version
    reportsDirectory.set(layout.buildDirectory.dir("mergedReportDir"))
}

tasks.register<JacocoReport>("jacocoTestReport") {

    dependsOn("testDebugUnitTest")
    dependsOn("pixel2api30DebugAndroidTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*Test*.*", "android/**/*.*")
    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(fileFilter) }
    val mainSrc = layout.projectDirectory.dir("src/main/kotlin")
    sourceDirectories.from(files(setOf(mainSrc)))
    classDirectories.from(files(setOf(debugTree)))
    executionData.from(fileTree(layout.buildDirectory) { include(setOf(
        "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
        "outputs/managed_device_code_coverage/debug/pixel2api30/coverage.ec",
    ))})
}

sonarqube {
    properties {
        property("sonar.projectKey", System.getenv("SONAR_PROJECT_KEY"))
        property("sonar.organization", System.getenv("SONAR_ORGANIZATION"))
        property("sonar.host.url", System.getenv("SONAR_HOST_URL"))
    }
}

tasks.register("generateGoogleServicesJson") {
    doLast {
        val jsonFileName = "google-services.json"
        val fileContent = System.getenv("GOOGLE_SERVICES_JSON")
        File(projectDir, jsonFileName).apply {
            createNewFile(); writeText(fileContent)
            println("generated $jsonFileName")
        }
    }
}

tasks.register("generateKsFile") {
    doLast {
        val jsonFileName = "bad.json"
        val encodedFileContent = System.getenv("KS_USB_TERMINAL_PLAY_STORE_RAW")
        val decodedBytes = Base64.getDecoder().decode(encodedFileContent)
        File(projectDir, jsonFileName).apply {
            createNewFile()
            writeBytes(decodedBytes)
            println("generated ${this.path}")
        }
    }
}

tasks.register("generateKsPropFile") {
    doLast {
        val configFileName = "keystore.properties"
        File(projectDir, configFileName).apply {
            createNewFile()
            writeText("""
                # Gradle signing properties for app module
                release.file=${System.getenv("USB_TERMINAL_KS_PATH")}
                release.storePassword=${System.getenv("USB_TERMINAL_KS_PASSWORD")}
                release.keyAlias=${System.getenv("USB_TERMINAL_KS_KEY_ALIAS")}
                release.keyPassword=${System.getenv("USB_TERMINAL_KS_KEY_PASSWORD")}
                """.trimIndent())
            println("generated ${this.path}")
        }
    }
}

tasks.register("generateAppDistKey") {
    doLast {
        val jsonFileName = "app-dist-key.json"
        val fileContent = System.getenv("GOOGLE_APP_DIST_FASTLANE_SERVICE_ACCOUNT")
        File(rootDir, jsonFileName).apply {
                createNewFile()
                writeText(fileContent)
                println("generated ${this.path}")
        }
    }
}

tasks.register("generateInternalReleaseKey") {
    doLast {
        val jsonFileName = "internal-release-key.json"
        val fileContent = System.getenv("SERVICE_ACCOUNT_USB_TERMINAL_PLAY_STORE_RAW")
        File(rootDir, jsonFileName).apply {
            createNewFile()
            writeText(fileContent)
            println("generated ${this.path}")
        }
    }
}

fun loadKeyStore(name: String): Properties? {
    val ksProp = Properties()
    val ksPropFile = file(name)
    return if (ksPropFile.exists()) {
        ksProp.load(FileInputStream(ksPropFile))
        ksProp
    } else {
        println("ERROR: local keystore file not found")
        null
    }
}

val firebase_bom_version: String by project
val hilt_version: String by project
val coroutines_version: String by project
val material_version: String by project
val mockk_version: String by project
dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:$firebase_bom_version"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Dependency Injection
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-compiler:$hilt_version")

    // Coroutines
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Compose Bom
    val composeBom = platform("androidx.compose:compose-bom:2023.06.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    // Compose - Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.10.1")

    // Other UI Libraries
    implementation("com.google.android.material:material:$material_version")

    // data
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // unit test libs
    testImplementation("junit:junit:4.13.2")

    // instrumented test libs
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Hamcrest for view matching
    androidTestImplementation("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")

    // coroutine testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")

    // google truth for assertions
    testImplementation("com.google.truth:truth:1.1.3")
    androidTestImplementation("androidx.test.ext:truth:1.6.0")

    // mockk
    testImplementation("io.mockk:mockk-android:$mockk_version")
    testImplementation("io.mockk:mockk-agent:$mockk_version")
    androidTestImplementation("io.mockk:mockk-android:$mockk_version")
    androidTestImplementation("io.mockk:mockk-agent:$mockk_version")

    // hilt testing - https://developer.android.com/training/dependency-injection/hilt-testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hilt_version")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:$hilt_version")

    // Android Serial Controller
    implementation("com.github.superus8r:UsbSerial:6.1.1")
}

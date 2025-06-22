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
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
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
    toolVersion = libs.versions.jacoco.get()
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

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*",
        "**/Dagger*.*", "**/*_Hilt*.*", "**/*Hilt*.*",
    )
    val javaDebugTree = fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) { exclude(fileFilter) }
    val kotlinDebugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(fileFilter) }
    val mainJavaSrc = layout.projectDirectory.dir("src/main/java")
    val mainKotlinSrc = layout.projectDirectory.dir("src/main/kotlin")
    sourceDirectories.from(files(mainJavaSrc, mainKotlinSrc))
    classDirectories.from(files(javaDebugTree, kotlinDebugTree))
    executionData.from(fileTree(layout.buildDirectory) {
        include(
            "outputs/unit_test_code_coverage/**/*.exec",
            "outputs/managed_device_code_coverage/**/*.ec",
            "outputs/managed_device_code_coverage/**/*.exec"
        )
    })
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

dependencies {

    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.constraintlayout)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)

    // Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Coroutines
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.extensions)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Compose Bom
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    // Compose - Android Studio Preview support
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)

    // Other UI Libraries
    implementation(libs.material)

    // Data
    implementation(libs.datastore.preferences)

    // Unit Test Libraries
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)

    // Instrumented Test Libraries
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hamcrest)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.truth)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)

    // Hilt Testing
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)

    // Android Serial Controller
    implementation(libs.usb.serial)
}

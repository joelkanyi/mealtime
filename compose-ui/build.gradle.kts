import org.jetbrains.kotlin.js.translate.context.Namer.kotlin

plugins {
    id(Plugins.androidLibrary)
    id("kotlin-android")
    id("org.jetbrains.kotlin.android")
}

apply {
    from("$rootDir/base-module.gradle")
}

android {
    namespace = "com.kanyideveloper.mealtime.composeui"
    compileSdk = AndroidConfig.compileSDK

    defaultConfig {
        minSdk = AndroidConfig.minSDK
        targetSdk = AndroidConfig.targetSDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = AndroidConfig.javaVersion
        targetCompatibility = AndroidConfig.javaVersion
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kotlin {
        jvmToolchain(17)
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packagingOptions {
        resources.excludes.apply {
            add("META-INF/LICENSE")
            add("META-INF/LICENSE-notice.md")
            add("META-INF/LICENSE.md")
            add("META-INF/*.properties")
            add("META-INF/AL2.0")
            add("META-INF/LGPL2.1")
        }
    }
}

dependencies {
    // implementation("androidx.appcompat:appcompat:1.6.0")
    implementation(libs.appcompat)

    // Accompanist System UI controller
    implementation(libs.accompanist.system.ui.controller)
}

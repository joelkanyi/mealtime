plugins {
    id(Plugins.androidLibrary)
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.android")
}

apply {
    from("$rootDir/base-module.gradle")
}

android {
    namespace = "com.kanyideveloper.mealtime.core_database"

    compileSdk = AndroidConfig.compileSDK

    defaultConfig {
        minSdk = AndroidConfig.minSDK
        targetSdk = AndroidConfig.targetSDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas")) // Room
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = AndroidConfig.javaVersion
        targetCompatibility = AndroidConfig.javaVersion
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
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
    implementation(project(Modules.core))

    // Room
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    androidTestImplementation(libs.room.testing)

    // Dagger - Hilt
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.hilt.compiler)
}

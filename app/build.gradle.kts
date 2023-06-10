plugins {
    id(Plugins.androidApplication)
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

apply {
    from("$rootDir/base-module.gradle")
}

android {
    compileSdk = AndroidConfig.compileSDK

    defaultConfig {
        applicationId = AndroidConfig.applicationId
        minSdk = AndroidConfig.minSDK
        targetSdk = AndroidConfig.targetSDK
        versionCode = AndroidConfig.versionCode
        versionName = AndroidConfig.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packagingOptions {
        resources {
            excludes += "META-INF/"
            excludes += "okhttp3/"
            excludes += "kotlin/"
            excludes += "org/"
            excludes += ".properties"
            excludes += ".bin"
        }
    }
    namespace = "com.kanyideveloper.mealtime"

    applicationVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}

dependencies {
    // Modules
    implementation(project(Modules.core))
    implementation(project(Modules.composeUi))

    implementation(project(Modules.home))
    implementation(project(Modules.search))
    implementation(project(Modules.favorites))
    implementation(project(Modules.settings))
    implementation(project(Modules.addMeal))
    implementation(project(Modules.mealPlanner))
    implementation(project(Modules.kitchenTimer))
    implementation(project(Modules.auth))

    // RamCosta Navigation
    implementation(libs.compose.destinations.animations)
    ksp(libs.compose.destinations.ksp)

    // implementation(libs.material.design)

    // Splash Screen API
    implementation(libs.core.splash.screen)

    // Dagger - Hilt
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.hilt.compiler)
}

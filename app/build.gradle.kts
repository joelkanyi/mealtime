plugins {
    id(Plugins.androidApplication)
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp") version ("1.8.10-1.0.9")
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
        sourceCompatibility = AndroidConfig.javaVersion
        targetCompatibility = AndroidConfig.javaVersion
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeCompiler
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

    implementation(libs.material.design)

    // Splash Screen API
    implementation(libs.core.splash.screen)
}

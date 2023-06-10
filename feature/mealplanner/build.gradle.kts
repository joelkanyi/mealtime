plugins {
    id(Plugins.androidLibrary)
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.android")
}

apply {
    from("$rootDir/base-module.gradle")
}

android {
    namespace = "com.kanyideveloper.mealtime.mealplanner"
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
    // RamCosta Navigation
    implementation(libs.compose.destinations.animations)
    ksp(libs.compose.destinations.ksp)

    implementation(project(Modules.core))
    implementation(project(Modules.composeUi))
    implementation(project(Modules.coreDatabase))
    implementation(project(Modules.coreNetwork))

    // Joel Kanyi - HorizontalCalendarView
    implementation(libs.horizontal.calendar.view)

    // Room
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    // Dagger - Hilt
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.hilt.compiler)
}

kotlin {
    sourceSets {
        debug {
            kotlin.srcDir("build/generated/ksp/debug/kotlin")
        }
        release {
            kotlin.srcDir("build/generated/ksp/release/kotlin")
        }
    }
}

ksp {
    arg("compose-destinations.mode", "destinations")
    arg("compose-destinations.moduleName", "addmeal")
}

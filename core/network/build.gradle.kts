@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.parcelize)
}

apply {
    from("$rootDir/base-module.gradle")
}

android {
    namespace = "com.joelkanyi.network"

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
        sourceCompatibility = AndroidConfig.javaVersion
        targetCompatibility = AndroidConfig.javaVersion
    }
    kotlinOptions {
        jvmTarget = AndroidConfig.jvmTarget
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.settings)

    // Retrofit
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.logging.interceptor)

    // Chucker
    debugImplementation("com.github.chuckerteam.chucker:library:4.0.0")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:3.5.2")
}

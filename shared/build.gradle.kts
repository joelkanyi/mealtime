plugins {
    kotlin("multiplatform")
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqlDelight.plugin)
    alias(libs.plugins.kotlinX.serialization.plugin)
}

kotlin {
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = AndroidConfig.jvmTarget
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.sqlDelight.runtime)
                implementation(libs.sqlDelight.coroutine)
                api(libs.koin.core)
                api(libs.multiplatformSettings)
                implementation(libs.kotlinxCoroutines)
                implementation(libs.kotlinxSerialization)
                implementation(libs.kotlinxDateTime)
                implementation(libs.ktor.core)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.logging)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.cio)
                implementation(libs.ktor.json)
                implementation(libs.sqlDelight.runtime)
                implementation(libs.sqlDelight.coroutine)
                implementation(libs.multiplatformSettings)
                implementation(libs.multiplatformSettingsCoroutines)
                api(libs.napier)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqlDelight.android)
                implementation(libs.ktor.android)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation(libs.sqlDelight.native)
                implementation(libs.ktor.ios)
            }
        }
    }
}

android {
    namespace = "com.joelkanyi.shared"
    compileSdk = AndroidConfig.compileSDK
    defaultConfig {
        minSdk = AndroidConfig.minSDK
    }
    compileOptions {
        sourceCompatibility = AndroidConfig.javaVersion
        targetCompatibility = AndroidConfig.javaVersion
    }
}

sqldelight {
    database(name = "MealTimeDatabase") {
        packageName = "com.joelkanyi.mealtime.data.local.sqldelight"
        sourceFolders = listOf("kotlin")
    }
}
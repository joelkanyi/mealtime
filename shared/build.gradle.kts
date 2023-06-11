plugins {
    kotlin("multiplatform")
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqlDelight.plugin)
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
                //put your multiplatform dependencies here
                implementation(libs.sqlDelight.runtime)
                implementation(libs.sqlDelight.coroutine)
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
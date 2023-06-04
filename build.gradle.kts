buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.14")
    }
} // Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version ("7.4.0") apply false
    id("com.android.library") version ("7.4.0") apply false
    //id("org.jetbrains.kotlin.android") version Versions.kotlin apply false
    alias(libs.plugins.android.kotlin) apply false
    // id("org.jetbrains.kotlin.jvm") version Versions.kotlin apply false
    id("com.google.dagger.hilt.android") version ("2.44") apply false
    id("org.jlleitschuh.gradle.ktlint") version ("11.3.1")
    id("com.diffplug.spotless") version ("5.17.1")
}

/*tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}*/

subprojects {

    apply(plugin = "com.diffplug.spotless")
    spotless {
        kotlin {
            target("**/*.kt")
            ktlint().userData(mapOf("disabled_rules" to "filename"))
            licenseHeaderFile(
                rootProject.file("${project.rootDir}/spotless/copyright.kt"),
                "^(package|object|import|interface)"
            )
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("misc") {
            target("**/*.md", "**/.gitignore")
            trimTrailingWhitespace()
            indentWithTabs()
            endWithNewline()
        }
        java {
            target("src/*/java/**/*.java")
            googleJavaFormat("1.7").aosp()
            indentWithSpaces()
            licenseHeaderFile(rootProject.file("spotless/copyright.java"))
            removeUnusedImports()
        }
        groovyGradle {
            target("**/*.gradle")
        }
    }

    repositories {
        mavenCentral()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    ktlint {
        android.set(true)
        verbose.set(true)
        debug.set(true)
        outputToConsole.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(true)
        enableExperimentalRules.set(true)
        filter {
            exclude { element -> element.file.path.contains("generated/") }
        }
    }
}

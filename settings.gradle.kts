pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.google.com")
        maven(url = "https://androidx.dev/storage/compose-compiler/repository/")
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}
rootProject.name = "MealTime"
include(":app")
include(":feature:addmeal")
include(":core")
include(":feature:favorites")
include(":feature:home")
include(":feature:settings")
include(":feature:search")
include(":compose-ui")
include(":core-database")
include(":core-network")
include(":feature:mealplanner")
include(":feature:kitchen-timer")
include(":feature:auth")

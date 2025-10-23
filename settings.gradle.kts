pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT) // Change this line
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NovacPaymentSDK"
include ':lib'

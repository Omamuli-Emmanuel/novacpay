pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Remove dependencyResolutionManagement to avoid conflicts
rootProject.name = "NovacPaymentSDK"
include(":lib")

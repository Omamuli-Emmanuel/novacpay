// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

// Add this to root project
plugins {
    id("maven-publish") apply false
}

subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.android.library")) {
            apply(plugin = "maven-publish")
            
            publishing {
                publications {
                    create<MavenPublication>("release") {
                        from(components["release"])
                        groupId = "com.github.Omamuli-Emmanuel"
                        artifactId = "novacpay"
                        version = "1.0.0"
                    }
                }
            }
        }
    }
}

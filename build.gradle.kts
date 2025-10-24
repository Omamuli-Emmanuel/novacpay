buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
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

// Add explicit publishToMavenLocal task at root level
tasks.register("publishToMavenLocal") {
    group = "publishing"
    description = "Publishes all Maven publications to the local Maven repository."
    
    // This will be configured after evaluation when all tasks are available
    doFirst {
        if (project.tasks.findByName(":lib:publishToMavenLocal") != null) {
            dependsOn(":lib:publishToMavenLocal")
        }
    }
}

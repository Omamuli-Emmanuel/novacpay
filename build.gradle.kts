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

// Add both required tasks at root level
tasks.register("assemble") {
    group = "build"
    description = "Assembles all variants of all applications and secondary packages."
    // The dependency will be added after evaluation when tasks exist
}

tasks.register("publishToMavenLocal") {
    group = "publishing"
    description = "Publishes all Maven publications to the local Maven repository."
    // The dependency will be added after evaluation when tasks exist
}

// Configure task dependencies after all projects are evaluated
gradle.projectsEvaluated {
    tasks.named("assemble") {
        val libAssemble = project(":lib").tasks.findByName("assemble")
        if (libAssemble != null) {
            dependsOn(libAssemble)
        }
    }
    
    tasks.named("publishToMavenLocal") {
        val libPublish = project(":lib").tasks.findByName("publishToMavenLocal")
        if (libPublish != null) {
            dependsOn(libPublish)
        }
    }
}

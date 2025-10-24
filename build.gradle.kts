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

// Root tasks that depend on library tasks
tasks.register("assemble") {
    group = "build"
}

tasks.register("publishToMavenLocal") {
    group = "publishing"
}

gradle.projectsEvaluated {
    tasks.named("assemble") {
        dependsOn(":lib:assembleRelease")
    }
    
    tasks.named("publishToMavenLocal") {
        dependsOn(":lib:publishToMavenLocal")
    }
}

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

// Add root-level publishToMavenLocal task
tasks.register("publishToMavenLocal") {
    dependsOn(":lib:publishToMavenLocal")
    group = "publishing"
    description = "Publishes all Maven publications to the local Maven repository."
}

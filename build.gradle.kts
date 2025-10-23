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

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

// Add this root-level task that depends on the library task
tasks.register("publishToMavenLocal") {
    dependsOn(":lib:publishToMavenLocal")
    group = "publishing"
    description = "Publishes all Maven publications to the local Maven repository."
}

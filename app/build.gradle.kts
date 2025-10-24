plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.novacpaymen.paywithnovac_android_skd"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    
    // Configure publishing variants
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}

// Create sources JAR task
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

// Create javadoc JAR task (empty for now, but required)
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

// Configure publishing - this must run
publishing {
    publications {
        create<MavenPublication>("mavenPublication") {
            // Use the bundleReleaseAar task output
            artifact(tasks.getByName("bundleReleaseAar"))
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
            
            groupId = "com.github.Omamuli-Emmanuel"
            artifactId = "novacpay"
            version = "1.0.0"
            
            // Add POM configuration
            pom {
                name.set("Novac Payment Android SDK")
                description.set("Android SDK for Novac Payment integration")
                url.set("https://github.com/Omamuli-Emmanuel/novacpay")
                
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                
                developers {
                    developer {
                        id.set("Omamuli-Emmanuel")
                        name.set("Omamuli Emmanuel")
                    }
                }
                
                scm {
                    connection.set("scm:git:github.com/Omamuli-Emmanuel/novacpay.git")
                    developerConnection.set("scm:git:ssh://github.com/Omamuli-Emmanuel/novacpay.git")
                    url.set("https://github.com/Omamuli-Emmanuel/novacpay")
                }
            }
        }
    }
}

// Ensure publishToMavenLocal task exists and works
afterEvaluate {
    tasks.named("publishMavenPublicationPublicationToMavenLocal") {
        dependsOn("bundleReleaseAar")
    }
}

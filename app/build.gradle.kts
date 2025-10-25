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
<<<<<<< HEAD
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Library metadata
        buildConfigField("String", "LIBRARY_VERSION", "\"1.0.0\"")
=======
        versionCode = 1
        versionName = "1.0.0"
>>>>>>> 5e6b39049fb1b6bde149dca2d9f129323903bc42
    }

    buildTypes {
        release {
            isMinifyEnabled = false
<<<<<<< HEAD
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        // REMOVED: compose = true
    }

    // REMOVED: composeOptions block

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
=======
        }
    }
    
    // Configure publishing variants
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
>>>>>>> 5e6b39049fb1b6bde149dca2d9f129323903bc42
    }
}

dependencies {
<<<<<<< HEAD
    // Core Android
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.5.5")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // REMOVED: All Compose dependencies
}

val androidSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // Configure the publication
                groupId = "com.github.Omamuli-Emmanuel"
                artifactId = "novacpay"
                version = "1.0.0"

                // Add the AAR artifact
                from(components["release"])

                // Add sources JAR
                artifact(androidSourcesJar.get())

                // Configure POM for Maven Central standards
                pom {
                    name.set("Novac Payment Android SDK")
                    description.set("Official Android SDK for Novac Payment integration. Easily integrate payment processing into your Android applications.")
                    url.set("https://github.com/Omamuli-Emmanuel/novacpay")

                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://opensource.org/licenses/Apache-2.0")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("Omamuli-Emmanuel")
                            name.set("Omamuli Emmanuel")
                            email.set("your-email@example.com") // Replace with actual email
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/Omamuli-Emmanuel/novacpay.git")
                        developerConnection.set("scm:git:ssh://github.com/Omamuli-Emmanuel/novacpay.git")
                        url.set("https://github.com/Omamuli-Emmanuel/novacpay")
                    }
                }
=======
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
>>>>>>> 5e6b39049fb1b6bde149dca2d9f129323903bc42
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

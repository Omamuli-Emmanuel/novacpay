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
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    // Explicitly configure publishing variants
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

// Publishing configuration that works with JitPack
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // Use the release component
                from(components["release"])
                
                groupId = "com.github.Omamuli-Emmanuel"
                artifactId = "novacpay"
                version = "1.0.0"
                
                // Add POM metadata
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
}

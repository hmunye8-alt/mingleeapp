// Top-level build.gradle.kts (Project-level)

plugins {
    // Android Gradle Plugin
    id("com.android.application") version "9.1.0" apply false
    id("com.android.library") version "9.1.0" apply false

    // Kotlin
    id("org.jetbrains.kotlin.android") version "2.3.20" apply false

    // Google Services (Firebase)
    id("com.google.gms.google-services") version "4.4.4" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Firebase Crashlytics ve Performance pluginleri
        classpath(libs.firebase.crashlytics.gradle)
        classpath(libs.firebase.perf.plugin)
    }
}

// Artık allprojects { repositories { … } } kullanma!
// Repository tanımlarını settings.gradle.kts içine taşımalısın.

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
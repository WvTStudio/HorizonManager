buildscript {
    repositories {
        mavenCentral()
        google()
        maven("https://kotlin.bintray.com/kotlinx")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0-beta02")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath(kotlin("serialization", version = "1.5.31"))
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.40.1")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://kotlin.bintray.com/kotlinx")
    }
}
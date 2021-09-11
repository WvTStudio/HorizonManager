buildscript {
    repositories {
        mavenCentral()
        google()
        maven("https://kotlin.bintray.com/kotlinx")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0-alpha10")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        classpath(kotlin("serialization", version = "1.5.21"))
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.38.1")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://kotlin.bintray.com/kotlinx")
    }
}
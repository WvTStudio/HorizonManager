buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0-beta04")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
        classpath(kotlin("serialization", "1.6.0"))
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.40.5")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
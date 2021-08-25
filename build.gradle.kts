buildscript {
    repositories {
        maven("https://kotlin.bintray.com/kotlinx")
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0-alpha08")
        classpath("com.google.gms:google-services:4.3.8")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
        classpath(kotlin("serialization", version = "1.5.10"))
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.36")
    }
}

allprojects {
    repositories {
        maven("https://kotlin.bintray.com/kotlinx")
        mavenCentral()
        google()
    }
}
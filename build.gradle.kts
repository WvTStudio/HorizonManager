buildscript {
    repositories {
        maven("https://kotlin.bintray.com/kotlinx")
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha15")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.5.2")
        classpath(kotlin("gradle-plugin", version = "1.4.32"))
        classpath(kotlin("serialization", version = "1.4.32"))
    }
}

allprojects {
    repositories {
        maven("https://kotlin.bintray.com/kotlinx")
        mavenCentral()
        google()
    }
}
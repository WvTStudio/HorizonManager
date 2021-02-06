buildscript {
    repositories {
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/jcenter/google")
//        maven("https://maven.aliyun.com/repository/jcenter")
//        maven("https://dl.bintray.com/k/otlin/kotlin-eap")
        maven("https://kotlin.bintray.com/kotlinx")
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha05")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21-2")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.4.1")
    }
}

allprojects {
    repositories {
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/jcenter/google")
//        maven("https://maven.aliyun.com/repository/jcenter")

//        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://kotlin.bintray.com/kotlinx")
        jcenter()
        mavenCentral()
        google()
    }
}
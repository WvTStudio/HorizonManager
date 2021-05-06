plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlinx-serialization")
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 21
        targetSdk = 30
//        versionCode = 1
//        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        create("beta") {
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
        }
        create("alpha") {
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
        }
        getByName("release") {
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
}
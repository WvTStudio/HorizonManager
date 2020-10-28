plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(30)
    buildToolsVersion = "30.0.2"

    defaultConfig {
        applicationId = "org.wvt.horizonmgr"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 2
        versionName = "2.0-alpha4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            proguardFiles = mutableListOf(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
            isShrinkResources = true
            isMinifyEnabled = true
            isUseProguard = true
//            minifyEnabled(true)
//            setUseProguard(true)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerVersion = "1.4.0"
        kotlinCompilerExtensionVersion = "1.0.0-alpha05"
    }
    lintOptions {
        disable("InvalidFragmentVersionForActivityResult")
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xallow-jvm-ir-dependencies",
            "-Xskip-prerelease-check"
        )
    }
}

dependencies {
    val composeVersion = "1.0.0-alpha05"

    implementation(platform("com.google.firebase:firebase-bom:26.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.0")

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("com.google.android.material:material:1.2.1")

    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

    implementation("androidx.compose.compiler:compiler:$composeVersion")
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")

    implementation("androidx.ui:ui-tooling:$composeVersion")

    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

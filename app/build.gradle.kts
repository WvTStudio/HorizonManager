plugins {
    id("com.android.application")
//    kotlin("android")
    kotlin("android.extensions")
    id("kotlin-android")
}

android {
    compileSdkVersion(30)
    buildToolsVersion = "30.0.0-rc2"
    defaultConfig {
        applicationId = "org.wvt.horizonmgr"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "2.0-alpha3"

        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }

    buildTypes {
        getByName("release") {
            proguardFiles = mutableListOf(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
            minifyEnabled(true)
            setUseProguard(true)
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
        kotlinCompilerExtensionVersion = "1.0.0-alpha03"
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
    val composeVersion = "1.0.0-alpha03"
    implementation(kotlin("stdlib"))

    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.2.1")

    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

    implementation("androidx.compose:compose-compiler:$composeVersion")
//    implementation("androidx.compose:compose-runtime:$composeVersion")

    implementation("androidx.compose.runtime:runtime:$composeVersion")

    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.foundation:foundation-layout:$composeVersion")
    implementation("androidx.compose.foundation:foundation-text:$composeVersion")

    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.compose.animation:animation-core:$composeVersion")

    implementation("androidx.compose.ui:ui:$composeVersion")
//    implementation("androidx.compose.ui:ui-geometry:$composeVersion")
//    implementation("androidx.compose.ui:ui-graphics:$composeVersion")
    implementation("androidx.compose.ui:ui-text:$composeVersion")
    implementation("androidx.compose.ui:ui-text-android:$composeVersion")
    implementation("androidx.compose.ui:ui-unit:$composeVersion")
    implementation("androidx.compose.ui:ui-util:$composeVersion")
//    implementation("androidx.compose.ui:ui-viewbinding:$composeVersion")

    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")

    implementation("androidx.ui:ui-tooling:$composeVersion")

    testImplementation("junit:junit:4.13")
//    androidTestImplementation("androidx.test:runner:1.1.0")
//    androidTestImplementation("androidx.test:rules:1.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

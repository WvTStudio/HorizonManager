plugins {
    id("com.android.application")
    id("kotlin-android")

    kotlin("kapt")
    id("dagger.hilt.android.plugin")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = 31
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "org.wvt.horizonmgr"
        minSdk = 21
        targetSdk = 31
        versionCode = 24
        versionName = "2.1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
//            isShrinkResources = true
//            isMinifyEnabled = true
        }
        getByName("debug") {
            versionNameSuffix = "-debug"
        }
        create("beta") {
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
            versionNameSuffix = "-beta4"
        }
        create("alpha") {
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
            versionNameSuffix = "-alpha12"
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.1"
    }
    /*lint {
        disable("InvalidFragmentVersionForActivityResult")
    }*/
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packagingOptions {
        resources {
            pickFirsts.apply {
                add("META-INF/AL2.0")
                add("META-INF/LGPL2.1")
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation(kotlin("stdlib"))

    implementation(project(":webapi"))
    implementation(project(":service"))

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.activity:activity-ktx:1.3.1")
    implementation("androidx.activity:activity-compose:1.3.1")

    implementation("androidx.appcompat:appcompat:1.4.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
    implementation("com.google.android.material:material:1.4.0")

    implementation("androidx.navigation:navigation-compose:2.4.0-alpha07")

    implementation("androidx.work:work-runtime-ktx:2.7.0-alpha05")


    val hiltVersion = "2.38.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")

    implementation("io.coil-kt:coil-compose:1.3.2")

    val accompanistVersion = "0.17.0"
    implementation("com.google.accompanist:accompanist-insets:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-placeholder:$accompanistVersion")

    val composeVersion = "1.0.1"
    implementation("androidx.compose.compiler:compiler:$composeVersion")
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-util:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-ripple:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui-test:$composeVersion")

//    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
//    implementation("androidx.compose.ui:ui-test:$composeVersion")

    implementation(platform("com.google.firebase:firebase-bom:26.1.0"))
//    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")

//    implementation("com.github.bumptech.glide:glide:4.12.0")
//    kapt("com.github.bumptech.glide:compiler:4.12.0")

    val markwonVersion = "4.6.2"
    implementation("io.noties.markwon:core:$markwonVersion")
    implementation("io.noties.markwon:ext-latex:$markwonVersion")
    implementation("io.noties.markwon:ext-tables:$markwonVersion")
    implementation("io.noties.markwon:image-coil:$markwonVersion")
    implementation("io.noties.markwon:syntax-highlight:$markwonVersion")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    debugImplementation(kotlin("reflect"))
}

configurations.all {
    exclude("org.jetbrains", "annotations-java5") // For io.noties.markwon
}
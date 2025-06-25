plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
//    alias(libs.plugins.gms)
//    alias(libs.plugins.kotlin.serialization)
//    alias(libs.plugins.buildkonfig)
}

android {
    namespace = "org.wvt.horizonmgr"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.wvt.horizonmgr"
        minSdk = 25
        targetSdk = 35
        versionCode = 26
        versionName = "2.1.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
//            isShrinkResources = true
            isMinifyEnabled = true
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0-alpha01"
    }
    /*lint {
        disable("InvalidFragmentVersionForActivityResult")
    }*/
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            pickFirsts.add("META-INF/AL2.0")
            pickFirsts.add("META-INF/LGPL2.1")
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(kotlin("stdlib"))

    implementation(project(":webapi"))
    implementation(project(":service"))

//    implementation(project.dependencies.platform(libs.koin.bom))
//    implementation(libs.koin.core)
//    implementation(libs.koin.compose)
//    implementation(libs.koin.compose.viewmodel)
//    implementation(libs.koin.compose.viewmodel.navigation)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.material)

    implementation(libs.navigation.compose)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.ktor.client.cio)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel)

    implementation(libs.coil.compose)

    implementation(libs.accompanist.insets)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.placeholder)

    implementation(libs.compose.compiler)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.material)
    implementation(libs.compose.material.ripple)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.test)

//    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.crashlytics.ktx)

    implementation(libs.kotlinx.datetime)

    implementation(libs.markwon.core)
    implementation(libs.markwon.ext.latex)
    implementation(libs.markwon.ext.tables)
    implementation(libs.markwon.image.coil)
    implementation(libs.markwon.syntax.highlight)

    implementation(libs.zip4j)

    implementation(libs.androidx.monitor)

    testImplementation(kotlin("test-junit5"))
//    androidTestImplementation(kotlin("test-junit5"))

    testImplementation(libs.json)
//    androidTestImplementation("junit:junit:4.12")

//    debugImplementation(kotlin("reflect"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Djava.net.useSystemProxies=true")
}

configurations.all {
    exclude("org.jetbrains", "annotations-java5") // For io.noties.markwon
}
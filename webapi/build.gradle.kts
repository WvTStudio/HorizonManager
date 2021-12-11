plugins {
    kotlin("jvm")
    id("kotlinx-serialization")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    val ktorVersion = "1.6.7"
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")

    implementation("org.json:json:20211205")

    testImplementation(kotlin("test-junit5"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.serialization)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.json)

    testImplementation(kotlin("test-junit5"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
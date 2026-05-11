plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.2.0")
    implementation("io.quarkus:io.quarkus.gradle.plugin:3.30.2")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:12.1.1")
}

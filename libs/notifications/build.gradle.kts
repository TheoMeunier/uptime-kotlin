dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))

    implementation(libs.quarkus.mailer)
    implementation(libs.quarkus.scheduler)
    implementation(libs.quarkus.micrometer.registry.prometheus)
    implementation(rootProject.libs.quarkus.arc)

    testImplementation(libs.junit)
    testImplementation(libs.jackson.module.kotlin)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:notifications"))

    implementation(libs.quarkus.scheduler)
    implementation(libs.minidns)
    implementation(libs.jackson.module.kotlin)
    implementation(rootProject.libs.quarkus.arc)

    testImplementation(libs.junit)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

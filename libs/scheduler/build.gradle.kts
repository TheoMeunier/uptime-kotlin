dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:notifications"))

    implementation(libs.quarkus.scheduler)
    implementation(libs.quarkus.jdbc.mssql)
    implementation(libs.quarkus.jdbc.mysql)
    implementation(libs.quarkus.jdbc.mariadb)
    implementation(libs.quarkus.messaging.kafka)
    implementation(libs.minidns)
    implementation(libs.jackson.module.kotlin)
    implementation(rootProject.libs.quarkus.arc)

    testImplementation(libs.junit)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

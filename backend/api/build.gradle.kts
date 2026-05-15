dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:scheduler"))
    implementation(project(":libs:notifications"))

    // Quarkus core
    implementation(libs.quarkus.rest.jackson)
    implementation(libs.quarkus.hibernate.validator)

    // Monitoring
    implementation(libs.quarkus.smallrye.health)

    // Auth
    implementation(libs.quarkus.smallrye.jwt)
    implementation(libs.quarkus.smallrye.jwt.build)

    // Tests
    testImplementation(libs.quarkus.junit5)
    testImplementation(libs.rest.assured)
}


plugins {
    id("quarkus-app")
}

dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:scheduler"))
    implementation(project(":libs:notifications"))
    
    // Quarkus core
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")

    // Monitoring
    implementation("io.quarkus:quarkus-smallrye-health")

    // Auth
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-jwt-build")

    // Tests
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}

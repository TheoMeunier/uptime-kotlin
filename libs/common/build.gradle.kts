plugins {
    id("kotlin-lib")
}

dependencies {
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("at.favre.lib:bcrypt:0.10.2")

    testImplementation(kotlin("test"))
}

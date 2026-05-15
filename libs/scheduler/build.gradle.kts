plugins {
    id("kotlin-lib")
}

dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:notifications"))

    implementation("io.quarkus:quarkus-scheduler")
    implementation("org.minidns:minidns-hla:1.1.1")

    testImplementation(kotlin("test"))
}

plugins {
    id("quarkus-app")
}

dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))

    implementation("io.quarkus:quarkus-scheduler")
    implementation("org.minidns:minidns-hla:1.1.1")


    testImplementation(kotlin("test"))
}

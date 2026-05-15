plugins {
    id("quarkus-app")
}

dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:notifications"))

    implementation("io.quarkus:quarkus-messaging-rabbitmq")

    testImplementation(kotlin("test"))
}

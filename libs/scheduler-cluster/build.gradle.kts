plugins {
    id("quarkus-app")
}

dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:notifications"))
    implementation(project(":libs:scheduler-cluster"))

    implementation("io.quarkus:quarkus-scheduler")

    implementation("io.quarkus:quarkus-messaging-rabbitmq")
    implementation("io.quarkus:quarkus-redis-client")

    testImplementation(kotlin("test"))
}

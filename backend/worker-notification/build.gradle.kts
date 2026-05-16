dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:notifications"))

    api(libs.quarkus.rabbitmq)
    api(libs.quarkus.redis)
}

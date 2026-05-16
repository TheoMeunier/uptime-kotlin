dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:notifications"))

    implementation(libs.quarkus.scheduler)
    api(libs.quarkus.redis)
    api(libs.quarkus.rabbitmq)
}

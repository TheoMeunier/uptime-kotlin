dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:notifications"))

    implementation(libs.quarkus.rabbitmq)
}

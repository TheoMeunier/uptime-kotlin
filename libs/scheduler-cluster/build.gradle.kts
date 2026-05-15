dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:notifications"))
    implementation(project(":libs:scheduler-cluster"))

    implementation(libs.quarkus.scheduler)
    implementation(libs.quarkus.redis)
    implementation(libs.quarkus.rabbitmq)

    implementation(rootProject.libs.quarkus.arc)
}

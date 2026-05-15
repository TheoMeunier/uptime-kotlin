dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:scheduler-cluster"))
    implementation(project(":libs:scheduler"))

    implementation(libs.quarkus.rabbitmq)
}

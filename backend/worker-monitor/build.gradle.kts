dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:scheduler"))

    implementation(libs.jackson.module.kotlin)
    implementation(libs.quarkus.scheduler)

}

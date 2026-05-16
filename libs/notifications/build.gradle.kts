dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))

    implementation(libs.quarkus.mailer)
    implementation(rootProject.libs.quarkus.arc)
}

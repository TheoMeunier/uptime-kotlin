dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:notifications"))

    implementation(libs.quarkus.scheduler)
    implementation(libs.minidns)
    implementation(libs.quarkus.hibernate.orm.panache.kotlin)
    implementation(rootProject.libs.quarkus.arc)
}

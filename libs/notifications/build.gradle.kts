dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))

    implementation(libs.quarkus.mailer)
    implementation(libs.quarkus.hibernate.orm.panache.kotlin)
    implementation(rootProject.libs.quarkus.arc)

}

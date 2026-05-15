dependencies {
    implementation(libs.bcrypt)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.quarkus.rest.jackson)
    implementation(libs.quarkus.hibernate.validator)
    implementation(rootProject.libs.quarkus.arc)
}

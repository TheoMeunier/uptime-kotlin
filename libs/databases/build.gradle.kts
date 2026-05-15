dependencies {
    implementation(project(":libs:common"))

    api(libs.quarkus.hibernate.orm.panache.kotlin)
    api(libs.quarkus.jdbc.postgresql)
    implementation(libs.quarkus.hibernate.validator)
    implementation(libs.quarkus.flyway)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.jackson.module.kotlin)
    implementation(rootProject.libs.quarkus.arc)

}

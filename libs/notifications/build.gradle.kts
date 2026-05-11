plugins {
    id("quarkus-app")
}

dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))

    implementation("io.quarkus:quarkus-mailer")

    testImplementation(kotlin("test"))
}

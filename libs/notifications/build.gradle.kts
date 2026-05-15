plugins {
    id("kotlin-lib")
}

dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))

    implementation("io.quarkus:quarkus-mailer")

    testImplementation(kotlin("test"))
}

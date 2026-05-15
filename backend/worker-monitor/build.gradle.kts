import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("quarkus-app")
}

dependencies {
    implementation(project(":libs:common"))
    implementation(project(":libs:databases"))
    implementation(project(":libs:scheduler-cluster"))
    implementation(project(":libs:scheduler"))

    testImplementation(kotlin("test"))

    implementation("io.quarkus:quarkus-messaging-rabbitmq")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}

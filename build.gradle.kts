plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.quarkus) apply false
    id("org.kordamp.gradle.jandex") version "2.1.0" apply false
}

group = "tmenier.fr"
version = "1.0.0-SNAPSHOT"

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")
    apply(plugin = "org.kordamp.gradle.jandex")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation(rootProject.libs.quarkus.kotlin)
        implementation(enforcedPlatform(rootProject.libs.quarkus.bom))
        implementation(rootProject.libs.kotlin.logging)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
            javaParameters = true
        }
    }


}

configure(subprojects.filter { it.path.startsWith(":backend") }) {
    apply(plugin = "io.quarkus")

    tasks.named("quarkusDependenciesBuild") {
        dependsOn(tasks.named("jandex"))
    }
}

allprojects {
    group = "tmenier.fr"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    allOpen {
        annotation("jakarta.ws.rs.Path")
        annotation("jakarta.enterprise.context.ApplicationScoped")
        annotation("jakarta.persistence.Entity")
        annotation("io.quarkus.test.junit.QuarkusTest")
        annotation("jakarta.transaction.Transactional")
        annotation("io.quarkus.scheduler.Scheduled")
    }
}

plugins {
    id("org.owasp.dependencycheck") version "8.4.0" apply false
}

group = "tmenier.fr"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

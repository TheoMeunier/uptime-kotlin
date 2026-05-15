pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
    }
}

rootProject.name = "uptime-kotlin"

//shares libs
include(":libs:common")
include(":libs:notifications")
include(":libs:scheduler")
include(":libs:scheduler-cluster")
include(":libs:databases")

// applications
include(":backend:api")
include(":backend:worker-monitor")
include(":backend:worker-notification")

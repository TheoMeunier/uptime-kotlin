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
include(
    ":libs:common",
    ":libs:notifications",
    ":libs:scheduler",
    ":libs:scheduler-cluster",
    ":libs:databases",

    ":backend:api",
    ":backend:worker-monitor",
    ":backend:worker-notification",
)

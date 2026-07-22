pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "uptime-kotlin"

// libs modules
include(
    ":libs:common",
    ":libs:notifications",
    ":libs:scheduler",
    ":libs:databases",
)

// backend modules
include(
    ":backend:api",
    ":backend:worker-monitor",
    ":backend:worker-notification"
)

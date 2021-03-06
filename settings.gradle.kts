pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
        id("maven")
    }
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "imogen"

include(":processor")
include(":sample")
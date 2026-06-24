rootProject.name = "NRFMatterforMobile"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from("no.nordicsemi.gradle:version-catalog:3.1.2")
        }
    }
}

include(":composeApp")
include(":androidDeps")
include(":androidApp")

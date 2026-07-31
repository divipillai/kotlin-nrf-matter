plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.nordic.android.kmp.library)
    alias(libs.plugins.nordic.kotlin)
    alias(libs.plugins.ksp)
}

group = "no.nordicsemi.nrf.matter"

kotlin {
    android {
        namespace = "no.nordicsemi.nrf.matter.shared"

        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true

            export(project(":composeApp"))
        }
    }

    sourceSets {
        androidMain.dependencies {
        }
        commonMain.dependencies {
            // The project, not the published matter-support artifact: ios-matter is
            // now a local SwiftPM package declared by :composeApp, and its
            // swiftPMDependencies metadata has to reach the Xcode-side linked
            // package without a publishToMavenLocal round trip in between.
            api(project(":composeApp"))

            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.resources)
        }
        commonTest.dependencies {
        }
    }
}

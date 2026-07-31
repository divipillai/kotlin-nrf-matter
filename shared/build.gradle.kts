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

//            export(project(":composeApp"))
            export("no.nordicsemi.nrf.matter:matter-support:1.0.0")
        }
    }

    sourceSets {
        androidMain.dependencies {
        }
        commonMain.dependencies {
//            api(project(":composeApp"))
            // api, not implementation: androidApp depends on :shared and uses
            // matter-support types directly (App, commonModule, androidModule,
            // LogDatabase), so they have to stay on its compile classpath.
            api("no.nordicsemi.nrf.matter:matter-support:1.0.0")

            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.resources)
        }
        commonTest.dependencies {
        }
    }
}

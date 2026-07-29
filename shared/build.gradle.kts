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
            // Re-export matter-support's own API so symbols like MainViewControllerKt are visible
            // from Swift via `import shared`. Do NOT use transitiveExport: it would also export
            // transitive deps such as cmptoast, whose own MainViewController.kt collides with
            // matter-support's and forces MainViewController() into a "MainViewControllerKt_" class.
            //
            // Must stay identical to the api(...) notation below: exporting a dependency the source
            // set doesn't declare as `api` fails the link with "dependencies exported in the
            // debugFramework binary are not specified as API-dependencies".
            export("no.nordicsemi.nrf.matter:matter-support:1.0.0")
        }
    }

    sourceSets {
        androidMain.dependencies {
        }
        commonMain.dependencies {
//            api(project(":composeApp"))

            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.resources)
            api("no.nordicsemi.nrf.matter:matter-support:1.0.0")
        }
        commonTest.dependencies {
        }
    }
}

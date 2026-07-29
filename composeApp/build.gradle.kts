@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.nordic.android.kmp.library)
    alias(libs.plugins.nordic.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.nordic.publish.kmp)
}

group = "no.nordicsemi.nrf.matter"

nordicPublishing {
    POM_ARTIFACT_ID = "matter-support"
    POM_NAME = "Nordic library for Matter connectivity."

    POM_DESCRIPTION = "Nordic Android Matter Library"
    POM_URL = "https://github.com/nordicsemi/kotlin-nrf-matter"
    POM_SCM_URL = "https://github.com/nordicsemi/kotlin-nrf-matter"
    POM_SCM_CONNECTION = "scm:git@github.com:nordicsemi/kotlin-nrf-matter.git"
    POM_SCM_DEV_CONNECTION = "scm:git@github.com:nordicsemi/kotlin-nrf-matter.git"
}

kotlin {
    android {
        namespace = "no.nordicsemi.nrf.matter.lib"

        androidResources {
            enable = true
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(project(":core"))
        }
    }

    swiftPMDependencies {
        // Must match the `platforms` requirement declared by the ios-matter package.
        iosMinimumDeploymentTarget.set("26.0")

        // Pinned exactly: `from(...)` would allow any 0.x release, and ios-matter
        // makes no API-stability promise below 1.0.
        //
        // Changing this version makes the NEXT iOS build fail once, by design
        // ("Synthetic project regenerated" -- Kotlin rewrites the generated
        // Package.swift mid-build). Re-resolve packages and build again; see the
        // bump procedure in iosApp/Configuration/check_swiftpm_lockfiles.sh.
        swiftPackage(
            url = url("git@github.com:sylwester-zielinski/ios-matter.git"),
            version = exact("0.0.10"),
            products = listOf(product("ios-matter")),
        )
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":androidDeps"))
//            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.accompanist.permissions)
            implementation(libs.jetbrains.compose.viewmodel)
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.room.runtime)
            implementation(libs.room.ktx)
        }
        commonMain.dependencies {
            api(project(":core"))

            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.foundation)
            implementation(libs.jetbrains.icons.extended)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.resources)

            // Preview
            implementation(libs.jetbrains.ui.tooling.preview)
            // Data time
            implementation(libs.kotlinx.datetime)
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // collections
            implementation(libs.kotlinx.collections.immutable)
            // Nav 3
            implementation(libs.jetbrains.navigation)
            implementation(libs.jetbrains.adaptive.navigation)
            implementation(libs.jetbrains.lifecycle.navigation)
            // serialization
            implementation(libs.kotlinx.serialization.json)
            // data store
            implementation(libs.androidx.dataStore.preferences)
            implementation(libs.androidx.dataStore.core)

            // Cloudy to have blur effect.
            implementation(libs.skydoves.cloudy)
            // CMPToast: Toasts for Compose Multiplatform
            implementation(libs.cmptoast)
            implementation(libs.compottie)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}

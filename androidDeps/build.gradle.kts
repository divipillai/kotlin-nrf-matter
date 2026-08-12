import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.spdx.sbom.gradle.SpdxSbomTask
import org.spdx.sbom.gradle.extensions.DefaultSpdxSbomTaskExtension
import java.net.URI

plugins {
    alias(libs.plugins.nordic.android.library)
    alias(libs.plugins.nordic.publish.android)
}

group = "no.nordicsemi.nrf.matter"

nordicPublishing {
    POM_ARTIFACT_ID = "android-deps"
    POM_NAME = "Nordic library for Matter connectivity."

    POM_DESCRIPTION = "Nordic Android Matter Library"
    POM_URL = "https://github.com/nordicsemi/kotlin-nrf-matter"
    POM_SCM_URL = "https://github.com/nordicsemi/kotlin-nrf-matter"
    POM_SCM_CONNECTION = "scm:git@github.com:nordicsemi/kotlin-nrf-matter.git"
    POM_SCM_DEV_CONNECTION = "scm:git@github.com:nordicsemi/kotlin-nrf-matter.git"
}

android {
    namespace = "no.nordicsemi.nrf.matter.android"

    defaultConfig {
        minSdk = 27
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs/jniLibs")
        }
    }
}

// The Home API SDK is not on any public repository, so it is consumed from the
// flat `mavenLocal` directory in this repo. SPDX rejects `file:` download
// locations, which fails the SBOM task, so report those packages as coming from
// the Google Maven repository they will eventually be published to.
tasks.withType<SpdxSbomTask>().configureEach {
    taskExtension.set(object : DefaultSpdxSbomTaskExtension() {
        override fun mapRepoUri(input: URI?, module: ModuleVersionIdentifier): URI? =
            if (input?.scheme == "file") URI.create("https://maven.google.com") else input
    })
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.so"))))

    implementation(project(":core"))
    // Home API SDK dependency
    api(libs.play.services.home)
    api(libs.play.services.types)
}

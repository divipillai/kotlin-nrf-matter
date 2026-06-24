plugins {
    alias(libs.plugins.nordic.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "no.nordicsemi.nrf.matter.app"

    defaultConfig {
        applicationId = "no.nordicsemi.nrf.matter"
    }
}

dependencies {
    implementation(project(":androidDeps"))
    implementation(project(":composeApp"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.android)
    implementation(libs.koin.android.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.jetbrains.compose.viewmodel)
    implementation(libs.jetbrains.compose.runtime)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(libs.androidx.core.splashscreen)
//    implementation(libs.koin.android)
//    implementation(libs.accompanist.permissions)
//    implementation(libs.androidx.lifecycle.viewmodelCompose)
//    implementation(libs.androidx.lifecycle.runtimeCompose)
//    implementation(libs.androidx.material3)
//    implementation(libs.androidx.material)
//    implementation(libs.androidx.runtime.livedata)
//    implementation(libs.room.core)
//    implementation(libs.room.ktx)
//    implementation(libs.koin.core)
//    implementation(libs.koin.androidx.compose)
//    ksp(libs.room.ksp)
//    implementation(libs.components.resources)
}
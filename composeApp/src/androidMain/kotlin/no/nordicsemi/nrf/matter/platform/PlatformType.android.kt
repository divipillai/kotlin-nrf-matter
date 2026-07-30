package no.nordicsemi.nrf.matter.platform

import no.nordicsemi.nrf.matter.android.BuildConfig

actual val currentType: PlatformType = PlatformType.ANDROID

actual fun getAppVersion(): String {
    return BuildConfig.VERSION_NAME
}
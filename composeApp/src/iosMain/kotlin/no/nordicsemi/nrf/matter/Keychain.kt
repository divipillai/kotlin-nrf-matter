@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.no.nordicsemi.nrf.matter.composeApp.KeypairInitializer

/**
 * Clears leftover keychain data on the first launch after a fresh install.
 *
 * The keychain survives app deletion, so a reinstalled app could otherwise pick up a stale
 * private key that no longer matches its fabric.
 *
 * Exists so the iOS app target can trigger this from `iOSApp.init()` without importing
 * `ios_matter`: the SwiftPM dependency is declared by this Kotlin module only.
 *
 * Not named `initKeychain`: Kotlin/Native exports `init`-prefixed functions to Objective-C
 * with a `do` prefix, which would make the Swift call site `KeychainKt.doInitKeychain()`.
 */
fun prepareKeychain() {
    KeypairInitializer.initKeychain()
}

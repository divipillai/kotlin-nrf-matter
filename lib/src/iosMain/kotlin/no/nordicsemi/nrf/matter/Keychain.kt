@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter

import kotlinx.cinterop.ExperimentalForeignApi
import iosMatter.KeypairInitializer

/**
 * Clears leftover keychain data on the first launch after a fresh install.
 *
 * The keychain survives app deletion, so a reinstalled app could otherwise pick up a stale
 * private key that no longer matches its fabric.
 *
 * Exists so the iOS app target can trigger this from `iOSApp.init()` without importing
 * `ios_matter`: this module is the only one that cinterops against it, and the compiled Swift ships
 * inside the klib, so Xcode has no module to import even if it wanted to.
 *
 * Not named `initKeychain`: Kotlin/Native exports `init`-prefixed functions to Objective-C
 * with a `do` prefix, which would make the Swift call site `KeychainKt.doInitKeychain()`.
 */
fun prepareKeychain() {
    KeypairInitializer.initKeychain()
}

//
//  KeypairInitializer.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 12/06/2026.
//

import Foundation

/// Ensures the signing keypair stored in the keychain is not stale after a fresh app install.
///
/// Exposed to Objective-C so Kotlin can reach it through `swiftPMImport`: the app entry point
/// calls this via the shared Kotlin framework rather than importing `ios_matter` itself.
@objc public final class KeypairInitializer : NSObject {

    private static let isInitializedKey = "is_keypair_initilized"

    /// Clears any leftover keychain data on first launch after a fresh install.
    ///
    /// The keychain survives app deletion and reinstallation, so without this check a
    /// reinstalled app could pick up a stale private key that no longer matches its fabric.
    @objc public static func initKeychain() {
        let helper = KeypairHelper(logTag: "KeypairInitializer")
        let storage = SharedStorage()
        
        if (storage.getBool(key: isInitializedKey) != true) {
            SwiftLogger.debug("Detected fresh app install. Clearing stale keychain data.")
            helper.deletePrivateKey()
            storage.storeBool(key: isInitializedKey, value: true)
            SwiftLogger.debug("Keychain cleared.")
        }
    }
}

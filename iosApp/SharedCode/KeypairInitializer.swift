//
//  KeypairInitializer.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 12/06/2026.
//

public class KeypairInitializer {
    
    private static let isInitializedKey = "is_keypair_initilized"
    
    public static func initKeychain() {
        let helper = KeypairHelper(logTag: "KeypairInitializer")
        let storage = SharedStorage(suitName: SharedConsts.sharedStorage)
        
        if (storage.getBool(key: isInitializedKey) != true) {
            SharedLogger.debug("Detected fresh app install. Clearing stale keychain data.")
            helper.deletePrivateKey()
            storage.storeBool(key: isInitializedKey, value: true)
            SharedLogger.debug("Keychain cleared.")
        }
    }
}

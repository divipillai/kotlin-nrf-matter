//
//  KeypairHelper.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 05/03/2026.
//

import Security
import Foundation

/**
 * A helper class for managing signing keys.
 * A signing keys needs to be unique and persistent for a specific fabric so those are
 * stored on the phone and retrieved when needed.
 */
class KeypairHelper {
    
    private let logTag: String
    private let tag: Data
    
    init(logTag: String) {
        self.logTag = logTag
        let name = "com.nordicsemi.nrf.matter"
        tag = name.data(using: .utf8)!
    }
    
    func generatePrivateKey() throws -> SecKey {
        SharedLogger.debug("\(self.logTag) - Generating new key.")
        
        let attributes: [String: Any] = [
            kSecAttrKeyType as String           : kSecAttrKeyTypeECSECPrimeRandom,
            kSecAttrKeySizeInBits as String     : 256,
            kSecPrivateKeyAttrs as String : [
                kSecAttrIsPermanent as String       : true,
                kSecAttrApplicationTag as String    : tag,
            ]
        ]

        var error: Unmanaged<CFError>? = nil

        let secKey = SecKeyCreateRandomKey(attributes as CFDictionary, &error)

        if error != nil {
            SharedLogger.debug("\(self.logTag) - Error during generation of a new key.")
            throw KeypairError.generatePrivateKeyFailed
        }

        guard let secKey = secKey else {
            SharedLogger.debug("\(self.logTag) - Error during generation of a new key.")
            throw KeypairError.generatePrivateKeyReturnedNil
        }
        
        SharedLogger.debug("\(self.logTag) - Returning newly generated key.")

        return secKey
    }
    
    func getPrivateKey() -> SecKey? {
        SharedLogger.debug("\(self.logTag) - Getting private key.")

        let query: [String: Any] = [
            kSecClass as String                 : kSecClassKey,
            kSecAttrApplicationTag as String    : tag,
            kSecAttrKeyType as String           : kSecAttrKeyTypeECSECPrimeRandom,
            kSecReturnRef as String             : kCFBooleanTrue as Any,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        guard status == errSecSuccess else {
            SharedLogger.debug("\(self.logTag) - Private key not found.")
            return nil
        }
        SharedLogger.debug("\(self.logTag) - Private key found.")
        guard item != nil else {
            SharedLogger.debug("\(self.logTag) - Private key is nil. Deleting.")
            deletePrivateKey()
            return nil
        }
        return (item as! SecKey)
    }
    
    public func deletePrivateKey() {
        SharedLogger.debug("\(self.logTag) - Deleting private key.")
        
        let deleteQuery: [String: Any] = [
            kSecClass as String: kSecClassKey,
            kSecAttrApplicationTag as String: tag
        ]
        SecItemDelete(deleteQuery as CFDictionary)
    }
}

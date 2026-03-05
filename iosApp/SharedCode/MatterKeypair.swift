//
//  MatterKeypair.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 05/03/2026.
//

import Matter
import os.log

public class MatterKeypair: NSObject, MTRKeypair {

    private let privateKey: SecKey
    private let _publicKey: SecKey
    
    private static let logger = Logger(subsystem: "nrf.matter", category: "DeviceSetup")
    private static let tag = name.data(using: .utf8)!

    public override init() {
        let existingKey = Self.getPrivateKey()
        let privateKey = existingKey != nil ? existingKey : (try! Self.generatePrivateKey())
        self._publicKey = SecKeyCopyPublicKey(privateKey!)!
        self.privateKey = privateKey!
        super.init()
        
        Self.logger.debug("AAATESTAAA - Printing private key in app.")
        self.printSecKey(privateKey!)
        Self.logger.debug("AAATESTAAA - Printing public key in app.")
        self.printSecKey(_publicKey)
    }
    
    private static let name = "nordicsemi.nrf.matter"
    
    public static func getPrivateKey() -> SecKey? {
        logger.debug("AAATESTAAA - Getting private key.")

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
            logger.debug("AAATESTAAA - Private key not found.")
            return nil
        }
        logger.debug("AAATESTAAA - Private key found.")
        guard item != nil else {
            logger.debug("AAATESTAAA - Private key is nil. Deleting.")
            deletePrivateKey()
            return nil
        }
        return (item as! SecKey)
    }
    
    public static func deletePrivateKey() {
        logger.debug("AAATESTAAA - Deleting private key.")
        
        let deleteQuery: [String: Any] = [
            kSecClass as String: kSecClassKey,
            kSecAttrApplicationTag as String: tag
        ]
        SecItemDelete(deleteQuery as CFDictionary)
    }

    public func signMessageECDSA_DER(_ message: Data) -> Data {
        var error: Unmanaged<CFError>? = nil
        let signedMessage = SecKeyCreateSignature(
            privateKey,
            .ecdsaSignatureMessageX962SHA256,
            message as CFData,
            &error
        )

        if error != nil {
            return Data()
        }

        guard let signedMessage = signedMessage else {
            return Data()
        }
        
        return signedMessage as Data
    }

    public func publicKey() -> Unmanaged<SecKey> {
        return Unmanaged.passRetained(_publicKey).autorelease()
    }

    private static func generatePrivateKey() throws -> SecKey {
        logger.debug("AAATESTAAA - Generating new key.")

        let tag = "nordicsemi.nrf.matter".data(using: .utf8)!
        
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
            logger.debug("AAATESTAAA - Error during generation of a new key.")
            throw Error.generatePrivateKeyFailed
        }

        guard let secKey = secKey else {
            logger.debug("AAATESTAAA - Error during generation of a new key.")
            throw Error.generatePrivateKeyReturnedNil
        }
        
        logger.debug("AAATESTAAA - Returning newly generated key.")

        return secKey
    }
    
    func printSecKey(_ key: SecKey) {
        var error: Unmanaged<CFError>?
        Self.logger.debug("AAATESTAAA - printSecKey")
        
        // Export the key to its external representation (usually DER format)
        if let keyData = SecKeyCopyExternalRepresentation(key, &error) as Data? {
            // Print as Base64 (standard for keys)
            Self.logger.debug("AAATESTAAA - Key Base64: \(keyData.base64EncodedString(), privacy: .public)")
            
            // Or print as Hex for a more "raw" look
            let hexString = keyData.map { String(format: "%02hhx", $0) }.joined()
            Self.logger.debug("AAATESTAAA - Key Hex: \(hexString, privacy: .public)")
        } else {
            if let error = error?.takeRetainedValue() {
                Self.logger.debug("AAATESTAAA - Error extracting key data: \(error.localizedDescription)")
            }
        }
    }
}

extension MatterKeypair {
    
    public enum Error: Swift.Error {
        case copyPublicKeyFailed
        case generatePrivateKeyFailed
        case generatePrivateKeyReturnedNil
    }
}

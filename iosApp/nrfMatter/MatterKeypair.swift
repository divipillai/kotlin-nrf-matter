//
//  MatterKeypair.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter
import os.log

class MatterKeypair: NSObject, MTRKeypair {

    private let privateKey: SecKey
    private let _publicKey: SecKey
    
    private let logger = Logger(subsystem: "nrf.matter", category: "DeviceSetup")

    override init() {
        let privateKey = try! Self.generatePrivateKey()
        self._publicKey = SecKeyCopyPublicKey(privateKey)!
        self.privateKey = privateKey
        super.init()
        
        logger.debug("AAATESTAAA - Printing private key in extension.")
        self.printSecKey(privateKey)
        logger.debug("AAATESTAAA - Printing public key in extension.")
        self.printSecKey(_publicKey)
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
        
        let tag = "nordicsemi.nrf.matter".data(using: .utf8)!

        let attributes: [CFString: Any] = [
            kSecAttrKeyType: kSecAttrKeyTypeECSECPrimeRandom,
            kSecAttrKeyClass: kSecAttrKeyClassPrivate,
            kSecAttrKeySizeInBits: 256,
            kSecAttrIsPermanent: false,
        ]

        var error: Unmanaged<CFError>? = nil

        let secKey = SecKeyCreateRandomKey(attributes as CFDictionary, &error)

        if error != nil {
            throw Error.generatePrivateKeyFailed
        }

        guard let secKey = secKey else {
            throw Error.generatePrivateKeyReturnedNil
        }

        return secKey
    }
    
    func printSecKey(_ key: SecKey) {
        var error: Unmanaged<CFError>?
        logger.debug("AAATESTAAA - printSecKey")
        
        // Export the key to its external representation (usually DER format)
        if let keyData = SecKeyCopyExternalRepresentation(key, &error) as Data? {
            // Print as Base64 (standard for keys)
            logger.debug("AAATESTAAA - Key Base64: \(keyData.base64EncodedString())")
            
            // Or print as Hex for a more "raw" look
            let hexString = keyData.map { String(format: "%02hhx", $0) }.joined()
            logger.debug("AAATESTAAA - Key Hex: \(hexString)")
        } else {
            if let error = error?.takeRetainedValue() {
                logger.debug("AAATESTAAA - Error extracting key data: \(error.localizedDescription)")
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

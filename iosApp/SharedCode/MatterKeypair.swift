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
    private let logTag: String
    
    private static let logger = Logger(subsystem: "nrf.matter", category: "DeviceSetup")
    
    public override init() {
        self.logTag = ""
        let helper = KeypairHelper(logTag: self.logTag)
        let existingKey = helper.getPrivateKey()
        let privateKey = existingKey != nil ? existingKey : (try! helper.generatePrivateKey())
        self._publicKey = SecKeyCopyPublicKey(privateKey!)!
        self.privateKey = privateKey!
        super.init()
        
        Self.logger.debug("MatterKeypair - Printing private key in app.")
        self.printSecKey(privateKey!)
        Self.logger.debug("MatterKeypair - Printing public key in app.")
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
    
    func printSecKey(_ key: SecKey) {
        var error: Unmanaged<CFError>?
        Self.logger.debug("MatterKeypair - printSecKey")
        
        // Export the key to its external representation (usually DER format)
        if let keyData = SecKeyCopyExternalRepresentation(key, &error) as Data? {
            // Print as Base64 (standard for keys)
            Self.logger.debug("MatterKeypair - Key Base64: \(keyData.base64EncodedString(), privacy: .public)")
            
            // Or print as Hex for a more "raw" look
            let hexString = keyData.map { String(format: "%02hhx", $0) }.joined()
            Self.logger.debug("MatterKeypair - Key Hex: \(hexString, privacy: .public)")
        } else {
            if let error = error?.takeRetainedValue() {
                Self.logger.debug("MatterKeypair - Error extracting key data: \(error.localizedDescription)")
            }
        }
    }
}

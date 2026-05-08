//
//  MatterKeypair.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 05/03/2026.
//

import Matter
import os.log

/**
 * Class used for noc signing.
 * It contains privete and public key which needs to be the same for a specific fabric.
 * Generated keys are stored sefely int the secure storage on the phone.
 */
public class MatterKeypair: NSObject, MTRKeypair {

    private let privateKey: SecKey
    private let _publicKey: SecKey
    private let logTag: String
    
    public override init() {
        self.logTag = ""
        let helper = KeypairHelper(logTag: self.logTag)
        let existingKey = helper.getPrivateKey()
        let privateKey = existingKey != nil ? existingKey : (try! helper.generatePrivateKey())
        self._publicKey = SecKeyCopyPublicKey(privateKey!)!
        self.privateKey = privateKey!
        super.init()
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
}

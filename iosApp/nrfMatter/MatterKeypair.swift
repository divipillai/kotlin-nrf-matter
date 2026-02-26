//
//  MatterKeypair.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter

class MatterKeypair: NSObject, MTRKeypair {

    private let privateKey: SecKey
    private let _publicKey: SecKey

    override init() {
        let privateKey = try! Self.generatePrivateKey()
        self._publicKey = SecKeyCopyPublicKey(privateKey)!
        self.privateKey = privateKey
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
}

extension MatterKeypair {
    
    public enum Error: Swift.Error {
        case copyPublicKeyFailed
        case generatePrivateKeyFailed
        case generatePrivateKeyReturnedNil
    }
}

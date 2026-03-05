//
//  KeypairError.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 05/03/2026.
//

public enum KeypairError: Swift.Error {
    case copyPublicKeyFailed
    case generatePrivateKeyFailed
    case generatePrivateKeyReturnedNil
}

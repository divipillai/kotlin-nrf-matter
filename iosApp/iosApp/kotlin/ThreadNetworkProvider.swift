//
//  ThreadNetworkProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import ComposeApp
import ThreadNetwork

struct THCredentialsSendable : Sendable {
    let name: String
    let data: Data?
}

class ThreadNetworkProviderImpl : ThreadNetworkProvider {
    
    func getAvailableThreadNetworks() async throws -> [ThreadNetwork] {
        print("AAATESTAAA - aaa")
        let client = THClient()
        print("AAATESTAAA - bbb")
        
        let preferredCredentials = try? await client.preferredCredentials()
        print("AAATESTAAA - preferredCredentials: \(preferredCredentials)")
        let tmpResult = try? await client.allActiveCredentials()
        print("AAATESTAAA - tmpResult: \(tmpResult)")
        
        let result = try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<[THCredentialsSendable], Error>) in
            print("AAATESTAAA - retrieveAllCredentials")
            client.retrieveAllCredentials { result, error in
                print("AAATESTAAA - result: \(result)")
                print("AAATESTAAA - error: \(error)")
                if let error = error {
                    print("AAATESTAAA - ddd")
                    continuation.resume(throwing: error)
                } else if let result = result {
                    print("AAATESTAAA - eee")
                    let networks = result.map { item in
                        let credential: THCredentials = item
                        return THCredentialsSendable(name: credential.description, data: credential.activeOperationalDataSet)
                    }
                    
                    continuation.resume(returning: networks)
                }
            }
        }

        print("AAATESTAAA - result: \(result)")
        
        let networks = result.map { item in
            ThreadNetwork(name: item.name, data: item.data)
        }
        
        print("AAATESTAAA - networks: \(networks)")
        
        return networks
    }
}

//
//  ThreadNetworkProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import ComposeApp
import ThreadNetwork

class ThreadNetworkProviderImpl : ThreadNetworkProvider {
    
    func getAvailableThreadNetworks() async throws -> [ThreadNetwork] {
        let client = THClient()
        let result = try await client.allCredentials().map { item in
            let credential: THCredentials = item
            return ThreadNetwork(name: credential.description, data: credential.activeOperationalDataSet)
        }
        
        print("AAATESTAAA - result: \(result)")
        
        return result
    }
}

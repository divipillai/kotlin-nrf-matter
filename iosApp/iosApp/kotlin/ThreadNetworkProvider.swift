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
        let client = THClient()
        var result: [ThreadNetwork] = []
      
//        client.storeCredentials(forBorderAgent: <#T##Data#>, activeOperationalDataSet: <#T##Data#>)

        if let preferredCredentials = try? await client.preferredCredentials() {
            print("AAATESTAAA - networkKey: \(preferredCredentials.networkKey)")
            print("AAATESTAAA - networkName: \(preferredCredentials.networkName)")
            print("AAATESTAAA - activeOperationalDataSet: \(preferredCredentials.activeOperationalDataSet)")
            print("AAATESTAAA - borderAgentID: \(preferredCredentials.borderAgentID)")
            print("AAATESTAAA - panID: \(preferredCredentials.panID)")
            print("AAATESTAAA - pskc: \(preferredCredentials.pskc)")
            result = [ThreadNetwork(name: preferredCredentials.networkName ?? "Unknown", data: preferredCredentials.activeOperationalDataSet)]
        }

        let allActiveCredentials = try? await client.allActiveCredentials()
        let allCredentials = try? await client.allCredentials()
        print("AAATESTAAA - allActiveCredentials: \(allActiveCredentials)")
        print("AAATESTAAA - allCredentials: \(allCredentials)")
        
        return result
    }
}

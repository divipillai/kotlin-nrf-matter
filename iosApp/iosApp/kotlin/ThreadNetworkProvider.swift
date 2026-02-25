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
        if let preferredCredentials = try? await client.preferredCredentials() {
            print("AAATESTAAA - networkKey: \(preferredCredentials.networkKey)")
            print("AAATESTAAA - networkName: \(preferredCredentials.networkName)")
            print("AAATESTAAA - activeOperationalDataSet: \(preferredCredentials.activeOperationalDataSet)")
            print("AAATESTAAA - borderAgentID: \(preferredCredentials.borderAgentID)")
            print("AAATESTAAA - panID: \(preferredCredentials.panID)")
            print("AAATESTAAA - pskc: \(preferredCredentials.pskc)")
            return [ThreadNetwork(name: preferredCredentials.networkName ?? "Unknown", data: preferredCredentials.activeOperationalDataSet)]
        }
        return []
    }
}

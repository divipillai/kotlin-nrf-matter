//
//  MatterClusterDiscovery.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 10/03/2026.
//

import ComposeApp

class MatterClusterDiscoveryImpl: MatterClusterDiscovery {
    
    func discoverClusters(nodeId: Int32) async throws {
        return await MatterClusterDiscoveryHelper(nodeId: nodeId as NSNumber).discoverClusters()
    }
}

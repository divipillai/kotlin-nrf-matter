//
//  MyMatterSupport.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import ComposeApp
import Matter
import MatterSupport
import nrfMatter
import SharedCode

class LocalMatterCommissioner : MatterCommissioner {
    
    func startIosCommissioning(onError: @escaping () -> Void) async throws -> Device? {
        return await commission()
    }
    
    func commission() async -> Device? {
        let homes = [MatterAddDeviceRequest.Home(displayName: "Nordic Home")]
        let topology = MatterAddDeviceRequest.Topology(ecosystemName: "Nordic Ecosystem", homes: homes)
        
        let request = MatterAddDeviceRequest(topology: topology, shouldScanNetworks: true)
        
        do {
            let storage = SharedStorage(suitName: SharedConsts.sharedStorage)
            storage.storeString(key: SharedConsts.matterEnvStorageKey, value: MatterEnv.local.rawValue)
            
            try await request.perform()
            
            let nodeID: NSNumber = NodeIdProvider.id // todo
            
            let device = await LocalMatterClusterDiscovery(nodeId: nodeID).discoverClusters()
            return device
        } catch {
            SharedLogger.info("Failed to set up device with error: \(error.localizedDescription).")
        }
        return nil
    }
}

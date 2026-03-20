//
//  MyMatterSupport.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import ComposeApp
import Matter
import MatterSupport
import os.log
import nrfMatter
import SharedCode

class LocalMatterCommissioner : MatterCommissioner {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "LocalMatterCommissioner")
    
    func startIosCommissioning(onError: @escaping () -> Void) async throws -> Device? {
        return await commission()
    }
    
    func commission() async -> Device? {
        let homes = [MatterAddDeviceRequest.Home(displayName: "Nordic Home")]
        let topology = MatterAddDeviceRequest.Topology(ecosystemName: "Nordic Ecosystem", homes: homes)
        
        let request = MatterAddDeviceRequest(topology: topology, shouldScanNetworks: true)
        
        do {
            let storage = SharedStorage()
            storage.storeString(key: SharedConsts.matterEnvStorageKey, value: MatterEnv.local.rawValue)
            
            try await request.perform()
            
            let nodeID: NSNumber = NodeIdProvider.id // todo
            
            let device = await LocalMatterClusterDiscovery(nodeId: nodeID).discoverClusters()
            return device
        } catch {
            logger.info("Failed to set up device with error: \(error.localizedDescription).")
        }
        return nil
    }
}

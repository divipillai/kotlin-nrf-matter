//
//  GoogleCommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 12/03/2026.
//

import ComposeApp
import Matter
import MatterSupport
import os.log
import nrfMatter
import SharedCode
import GoogleHomeSDK

class GoogleHomeCommissioner : MatterCommissioner {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "GoogleHomeCommissioner")
    
    func startIosCommissioning(onError: @escaping () -> Void) async throws -> Device? {
        return await commission()
    }
    
    func commission() async -> Device? {
        let home = try? await Home.connect()
        
        guard let home else { return nil }
        
        let allStructuresChanges = home.structures()
        let allStructures = (try? await allStructuresChanges.list()) ?? []
        let structure = allStructures.first

        guard let structure else { return nil }
        
        let homes = [MatterAddDeviceRequest.Home(displayName: "Nordic Home")]
        let topology = MatterAddDeviceRequest.Topology(ecosystemName: "Nordic Ecosystem", homes: homes)
        
        let request = MatterAddDeviceRequest(topology: topology, shouldScanNetworks: true)
        
        do {
            let storage = MatterStorage()
            storage.storeString(key: SharedConsts.matterEnvStorageKey, value: MatterEnv.google.rawValue)
            
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

//
//  MatterClusterDiscovery.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 10/03/2026.
//

import ComposeApp
import Matter
import OSLog
import SharedCode

class MatterClusterDiscoveryImpl : MatterClusterDiscovery {
    
    private let device: MTRDevice
    private let baseDevice: MTRBaseDevice
    
    private let logger = Logger(subsystem: "nrf.matter", category: "MatterClusterDiscovery")
    
    init() {
        let controller = MatterControllerProviderImpl().getController()!
        device = MTRDevice(nodeID: NodeIdProvider.id, controller: controller)
        baseDevice = MTRBaseDevice(nodeID: NodeIdProvider.id, controller: controller)
    }
    
    func discoverClusters() async {
        let deviceTypes = await getDeviceType()
        
        let endpoints = await readEndpoints()
        for endpoint in endpoints {
            let clusters = await readClusters(endpoint: endpoint)
        }

        logger.debug("discoverClusters - finished")
    }
    
    func getDeviceType() async -> [MTRDescriptorClusterDeviceTypeStruct] {
        logger.debug("getDeviceType")
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let result = (try? await descriptor?.readAttributeDeviceTypeList())?.map { $0 as! MTRDescriptorClusterDeviceTypeStruct} ?? []
        let printableResult = result.map { $0.deviceType }
        logger.debug("Supported device types: \(result)")
        return result
    }
    
    func readEndpoints() async -> [NSNumber] {
        logger.debug("readEndpoints")
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let result = (try? await descriptor?.readAttributePartsList())?.map { $0 as! NSNumber} ?? []
        logger.debug("Supported endpoints: \(result)")
        return result
    }
    
    func readClusters(endpoint: NSNumber) async -> [NSNumber] {
        logger.debug("readClusters")
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: endpoint, queue: DispatchQueue.global())
        let result = (try? await descriptor?.readAttributeServerList())?.map { $0 as! NSNumber} ?? []
        logger.debug("Supported clusters: \(result)")
        return result
    }
}

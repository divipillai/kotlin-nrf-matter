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
 
        
        let endpoints = await readEndpoints()
        for endpoint in endpoints {
            let clusters = await readClusters(endpoint: endpoint)
        }

        logger.debug("discoverClusters - finished")
    }
    
//    func getDeviceType() async {
//        logger.debug("readClusters")
//        return await withCheckedContinuation { (continuation: CheckedContinuation<[Int], Never>) in
//            let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: endpoint, queue: DispatchQueue.global())
//            logger.debug("Descriptor: \(descriptor)")
//            descriptor?.readAttributeServerList { [weak self] (clusters: [Any]?, error: Error?) in
//                if let endpoints = clusters {
//                    self?.logger.debug("Supported clusters: \(endpoints)")
//                    let result = endpoints.map { $0 as! Int}
//                    continuation.resume(returning: result)
//                }
//            }
//        }
//    }
//    
    func readEndpoints() async -> [NSNumber] {
        logger.debug("readEndpoints")
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let result = (try? await descriptor?.readAttributePartsList())?.map { $0 as! NSNumber} ?? []
        logger.debug("Supported endpoints: \(result)")
        return result
    }
    
    func readClusters(endpoint: NSNumber) async -> [NSNumber] {
        logger.debug("readClusters")
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let result = (try? await descriptor?.readAttributeServerList())?.map { $0 as! NSNumber} ?? []
        logger.debug("Supported clusters: \(result)")
        return result
    }
}

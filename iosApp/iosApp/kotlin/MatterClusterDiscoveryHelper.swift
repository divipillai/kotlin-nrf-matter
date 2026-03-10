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

class MatterClusterDiscoveryHelper {
    
    private let device: MTRDevice
    private let baseDevice: MTRBaseDevice
    
    private let logger = Logger(subsystem: "nrf.matter", category: "MatterClusterDiscovery")
    
    init(nodeId: NSNumber) {
        let controller = MatterControllerProviderImpl().getController()!
        device = MTRDevice(nodeID: nodeId, controller: controller)
        baseDevice = MTRBaseDevice(nodeID: nodeId, controller: controller)
    }
    
    func getProductName() async -> String {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: .main)
        let productName = (try? await information?.readAttributeProductName()) ?? "unknown"
        logger.debug("ProductName: \(productName)")
        return productName
    }
    
    func getProductId() async -> NSNumber? {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: .main)
        let productId = try? await information?.readAttributeProductID() ?? nil
        logger.debug("ProductId: \(productId)")
        return productId
    }
    
    func getVendorName() async -> String {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: .main)
        let vendorName = (try? await information?.readAttributeVendorName()) ?? "unknown"
        logger.debug("VendorName: \(vendorName)")
        return vendorName
    }
    
    func getVendorId() async -> NSNumber? {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: .main)
        let vendorId = try? await information?.readAttributeVendorID() ?? nil
        logger.debug("VendorId: \(vendorId)")
        return vendorId
    }
    
    func discoverClusters() async {
        let vendorId = await getVendorId()
        let vendorName = await getVendorName()
        let productId = await getProductId()
        let productName = await getProductName()
        logger.debug("VendorId: \(vendorId)")
        logger.debug("VendorName: \(vendorName)")
        logger.debug("ProductId: \(productId)")
        logger.debug("ProductName: \(productName)")
        
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

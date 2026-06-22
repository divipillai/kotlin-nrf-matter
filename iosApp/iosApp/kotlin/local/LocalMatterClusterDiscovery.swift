//
//  LocalMatterClusterDiscovery.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 10/03/2026.
//

import ComposeApp
import Matter
import SharedCode

/**
 * A helper class for obtaining meta data for the root endpoint (0) such as device name, product name, vendor name, vendor it
 * and from supported endpoints (1..n) information about device type, client clusters and server clusters.
 */
class LocalMatterClusterDiscovery {
    
    var stage: Stage = Stage.readBasicInformation
    
    private let nodeId: NSNumber
    private let baseDevice: MTRBaseDevice

    init(nodeId: NSNumber) throws {
        self.nodeId = nodeId
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        baseDevice = MTRBaseDevice(nodeID: nodeId, controller: controller)
    }
    
    /**
     * The main function of this class for reading all available data.
     * It reads vendor name, vendor id, product name, product id from the main endpoint 0
     * as well as device type, client clusters and server clusters for all other endpoints.
     *
     * It returns a type that contains all the data. 
     */
    func discoverClusters() async throws -> Device {
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: nodeId, controller: controller)
        let cluster = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        
        guard let cluster else { throw OperationError.unknown }
        
        let deviceId = DeviceId(value: nodeId.stringValue)
        let name = "Matter device: \(nodeId)"
        let vendorId = try await cluster.getVendorId()
        let vendorName = try await cluster.getVendorName()
        let productId = try await cluster.getProductId()
        let productName = try await cluster.getProductName()
        let uniqueId = try await cluster.getUniqueId()
        let swVersion = try await cluster.getSoftwareVersion()
        let specVersion = try await cluster.getSpecificationVersion()
        let serialNumber = try? await cluster.getSerialNumber()

        self.stage = Stage.readDescriptorCluster
        
        let mainDescriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        guard let mainDescriptor else { throw OperationError.unknown }
        let _ = try await mainDescriptor.getDeviceType(endpoint: 0)
        try await mainDescriptor.readEndpoint0()

        var deviceMatterInfo: [DeviceMatterInfo] = []
        let endpoints = try await mainDescriptor.readEndpoints()
        
        for endpoint in endpoints {
            let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: endpoint, queue: DispatchQueue.global())
            guard let descriptor else { continue }
            
            let deviceTypes = try await descriptor.getDeviceType(endpoint: endpoint)
            let clientClusters = try await descriptor.readClientClusters(endpoint: endpoint)
            let serverClusters = try await descriptor.readServerClusters(endpoint: endpoint)

            let manufacturerSpecificData: ManufacturerSpecificData?
            if (serverClusters.contains(0xFFF1FC01)) {
                let controller = LocalMatterCustomClusterController()
                manufacturerSpecificData = try await controller.getData(deviceId: deviceId, endpoint: Int32(truncating: endpoint))
            } else {
                manufacturerSpecificData = nil
            }

            let newInfo = DeviceMatterInfo(
                endpoint: endpoint.int32Value,
                types: deviceTypes.map { KotlinLong(value: $0.deviceType.int64Value) },
                serverClusters: serverClusters.map { KotlinLong(value: $0.int64Value) },
                clientClusters: clientClusters.map { KotlinLong(value: $0.int64Value) },
                manufacturerSpecificData: manufacturerSpecificData,
            )
            deviceMatterInfo.append(newInfo)
        }
        
        let deviceType = mapDeviceType(deviceMatterInfo.flatMap { $0.types }.first)

        SharedLogger.debug("discoverClusters - finished")
        
        return Device(
            deviceId: deviceId,
            dateCommissioned: KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000)),
            vendorId: vendorId.stringValue,
            productId: productId.stringValue,
            deviceType: deviceType,
            name: name,
            productName: productName,
            vendorName: vendorName,
            uniqueId: uniqueId,
            softwareVersion: swVersion,
            specificationVersion: KotlinLong(value: specVersion.int64Value),
            serialNumer: serialNumber,
            deviceMatterInfo: deviceMatterInfo,
        )
    }
    
    /**
     * Maps numeric value to a specific device type.
     * This example supports only few device types dedined by a standard and one which is a custom type.
     */
    func mapDeviceType(_ deviceType: KotlinLong?) -> DeviceType {
        SharedLogger.debug("mapDeviceType: \(String(describing: deviceType))")
        switch deviceType {
        case 10: return .doorLock
        case 260: return .lightSwitch
        case 257: return .lightOnOff
        case 0xFFF10001: return .manufacturerSpecificDevice
        default: return .unsupported
        }
    }

}

private extension MTRBaseClusterDescriptor {
    
    /**
     * A helper function for reading device type, client clusters and server clusters for the root endpoint 0.
     */
    func readEndpoint0() async throws {
        let deviceTypes = try await getDeviceType(endpoint: 0)
        let clientClusters = try await readClientClusters(endpoint: 0)
        let serverClusters = try await readServerClusters(endpoint: 0)
        SharedLogger.debug("Endpoint 0 - devicetypes: \(deviceTypes)")
        SharedLogger.debug("Endpoint 0 - clientClusters: \(clientClusters)")
        SharedLogger.debug("Endpoint 0 - serverClusters: \(serverClusters)")
    }
    
    /**
     * Reads device type for the endpoint.
     *
     * It says what kind of device is defined for this endpoints.
     * The definition specifies which clusters are mandatory and which one are optional for this device type.
     */
    func getDeviceType(endpoint: NSNumber) async throws -> [MTRDescriptorClusterDeviceTypeStruct] {
        SharedLogger.debug("Cluster Descriptor - getDeviceType()")
        let result = (try await readAttributeDeviceTypeList()).map { $0 as! MTRDescriptorClusterDeviceTypeStruct}
        SharedLogger.debug("Supported device types: \(result)")
        return result
    }

    /**
     * Reads other parts list for the endpoint.
     * Parts list is a set of endpoint logically connected with the endpoint.
     * Root endpoint 0 should return all the endpoints available on the device.
     */
    func readEndpoints() async throws -> [NSNumber] {
        SharedLogger.debug("Cluster Descriptor - readEndpoints()")
        let result = (try await readAttributePartsList()).map { $0 as! NSNumber}
        SharedLogger.debug("Supported endpoints: \(result)")
        return result
    }
    
    /**
     * Reads server clusters list for the endpoint.
     * A server cluster is a cluster which implements the logic of a cluster, has its state and accept commands.
     *
     * It can be a light bulb that can receive on/off commands.
     */
    func readServerClusters(endpoint: NSNumber) async throws -> [NSNumber] {
        SharedLogger.debug("Cluster Descriptor - readServerClusters()")
        let result = (try await readAttributeServerList()).map { $0 as! NSNumber}
        SharedLogger.debug("Supported server clusters: \(result)")
        return result
    }
    
    /**
     * Reads client clusters list for the endpoint.
     * A client cluster means that the device can send commands to the same cluster
     * defined on another device as a server cluster.
     *
     * It can be a switch that sends on/off command to a light bulb.
     */
    func readClientClusters(endpoint: NSNumber) async throws -> [NSNumber] {
        SharedLogger.debug("Cluster Descriptor - readClientClusters()")
        let result = (try await readAttributeClientList()).map { $0 as! NSNumber}
        SharedLogger.debug("Supported client clusters: \(result)")
        return result
    }
}

private extension MTRBaseClusterBasicInformation {
    
    /**
     * Read device name from Basic Information Cluster.
     */
    func getName() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getName()")
        let name = try await readAttributeNodeLabel()
        SharedLogger.debug("Name: \(name)")
        return name
    }
    
    /**
     * Read product name from Basic Information Cluster.
     */
    func getProductName() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getProductName()")
        let productName = try await readAttributeProductName()
        SharedLogger.debug("ProductName: \(productName)")
        return productName
    }
    
    /**
     * Read product id from Basic Information Cluster.
     */
    func getProductId() async throws -> NSNumber {
        SharedLogger.debug("Basic Information Cluster - getProductId()")
        let productId = try await readAttributeProductID()
        SharedLogger.debug("ProductId: \(productId)")
        return productId
    }
    
    /**
     * Read vendor name from Basic Information Cluster.
     */
    func getVendorName() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getVendorName()")
        let vendorName = try await readAttributeVendorName()
        SharedLogger.debug("VendorName: \(vendorName)")
        return vendorName
    }
    
    /**
     * Read vendor id from Basic Information Cluster.
     */
    func getVendorId() async throws -> NSNumber {
        SharedLogger.debug("Basic Information Cluster - getVendorId()")
        let vendorId = try await readAttributeVendorID()
        SharedLogger.debug("VendorId: \(vendorId)")
        return vendorId
    }
    
    /**
     * Read unique id from Basic Information Cluster.
     */
    func getUniqueId() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getUniqueId()")
        let uniqueId = try await readAttributeUniqueID()
        SharedLogger.debug("UniqueId: \(uniqueId)")
        return uniqueId
    }
    
    /**
     * Read software version from Basic Information Cluster.
     */
    func getSoftwareVersion() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getSoftwareVersion()")
        let swVersion = try await readAttributeSoftwareVersionString()
        SharedLogger.debug("Software version: \(swVersion)")
        return swVersion
    }
    
    /**
     * Read specification version from Basic Information Cluster.
     */
    func getSpecificationVersion() async throws -> NSNumber {
        SharedLogger.debug("Basic Information Cluster - getSpecificationVersion()")
        let specVersion = try await readAttributeSpecificationVersion()
        SharedLogger.debug("Specification version: \(specVersion)")
        return specVersion
    }
    
    /**
     * Read serial number from Basic Information Cluster.
     */
    func getSerialNumber() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getSerialNumber()")
        let serialNumber = try await readAttributeSerialNumber()
        SharedLogger.debug("Serial number: \(serialNumber)")
        return serialNumber
    }
}

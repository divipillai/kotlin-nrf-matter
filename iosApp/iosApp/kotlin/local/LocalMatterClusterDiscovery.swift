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
    
    private let nodeId: NSNumber
    private let baseDevice: MTRBaseDevice

    init(nodeId: NSNumber) throws {
        self.nodeId = nodeId
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        baseDevice = MTRBaseDevice(nodeID: nodeId, controller: controller)
    }
    
    /**
     * Read device name from Basic Information Cluster.
     */
    func getName() async throws -> String {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let name = (try await information?.readAttributeNodeLabel()) ?? "unknown"
        SharedLogger.debug("Name: \(name)")
        return name
    }
    
    /**
     * Read product name from Basic Information Cluster.
     */
    func getProductName() async throws -> String {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let productName = (try await information?.readAttributeProductName()) ?? "unknown"
        SharedLogger.debug("ProductName: \(productName)")
        return productName
    }
    
    /**
     * Read product id from Basic Information Cluster.
     */
    func getProductId() async throws -> NSNumber? {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let productId = try await information?.readAttributeProductID() ?? nil
        SharedLogger.debug("ProductId: \(productId)")
        return productId
    }
    
    /**
     * Read vendor name from Basic Information Cluster.
     */
    func getVendorName() async throws -> String {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let vendorName = (try await information?.readAttributeVendorName()) ?? "unknown"
        SharedLogger.debug("VendorName: \(vendorName)")
        return vendorName
    }
    
    /**
     * Read vendor id from Basic Information Cluster.
     */
    func getVendorId() async throws -> NSNumber? {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let vendorId = try await information?.readAttributeVendorID() ?? nil
        SharedLogger.debug("VendorId: \(vendorId)")
        return vendorId
    }
    
    /**
     * Read unique id from Basic Information Cluster.
     */
    func getUniqueId() async throws -> String? {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let uniqueId = try await information?.readAttributeUniqueID() ?? nil
        SharedLogger.debug("UniqueId: \(uniqueId)")
        return uniqueId
    }
    
    /**
     * Read software version from Basic Information Cluster.
     */
    func getSoftwareVersion() async throws -> String? {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let swVersion = try await information?.readAttributeSoftwareVersionString() ?? nil
        SharedLogger.debug("Software version: \(swVersion)")
        return swVersion
    }
    
    /**
     * Read specification version from Basic Information Cluster.
     */
    func getSpecificationVersion() async throws -> NSNumber? {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let specVersion = try await information?.readAttributeSpecificationVersion() ?? nil
        SharedLogger.debug("Specification version: \(specVersion)")
        return specVersion
    }
    
    /**
     * Read serial number from Basic Information Cluster.
     */
    func getSerialNumber() async throws -> String? {
        let information = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let serialNumber = try await information?.readAttributeSerialNumber() ?? nil
        SharedLogger.debug("Serial number: \(serialNumber)")
        return serialNumber
    }
    
    /**
     * The main function of this class for reading all available data.
     * It reads vendor name, vendor id, product name, product id from the main endpoint 0
     * as well as device type, client clusters and server clusters for all other endpoints.
     *
     * It returns a type that contains all the data. 
     */
    func discoverClusters() async throws -> Device {
        let deviceId = DeviceId(value: nodeId.stringValue)
        let name = "Matter device: \(nodeId)"
        let vendorId = try await getVendorId()
        let vendorName = try await getVendorName()
        let productId = try await getProductId()
        let productName = try await getProductName()
        let uniqueId = try await getUniqueId()
        let swVersion = try await getSoftwareVersion()
        let specVersion = try await getSpecificationVersion()
        let serialNumber = try await getSerialNumber()
        
        let deviceTypes = try await getDeviceType(endpoint: 0)
        SharedLogger.debug("deviceTypes AAA: \(deviceTypes)")

        try await readEndpoint0()

        var deviceMatterInfo: [DeviceMatterInfo] = []
        let endpoints = try await readEndpoints()
        for endpoint in endpoints {
            let deviceTypes = try await getDeviceType(endpoint: endpoint)
            let clientClusters = try await readClientClusters(endpoint: endpoint)
            let serverClusters = try await readServerClusters(endpoint: endpoint)
            let controller = LocalMatterCustomClusterController()
            let manufacturerSpecificData = try await controller.getData(deviceId: deviceId, endpoint: Int32(truncating: endpoint))

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
            vendorId: vendorId?.stringValue ?? "unknown",
            productId: productId?.stringValue ?? "unknown",
            deviceType: deviceType,
            name: name,
            productName: productName,
            vendorName: vendorName,
            uniqueId: uniqueId,
            softwareVersion: swVersion,
            specificationVersion: specVersion.map { KotlinLong(value: $0.int64Value) },
            serialNumer: serialNumber,
            deviceMatterInfo: deviceMatterInfo,
        )
    }
    
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
     * Maps numeric value to a specific device type.
     * This example supports only few device types dedined by a standard and one which is a custom type.
     */
    func mapDeviceType(_ deviceType: KotlinLong?) -> DeviceType {
        SharedLogger.debug("mapDeviceType: \(deviceType)")
        switch deviceType {
        case 10: return .doorLock
        case 260: return .lightSwitch
        case 257: return .lightOnOff
        case 0xFFF10001: return .manufacturerSpecificDevice
        default: return .unknown
        }
    }
    
    /**
     * Reads device type for the endpoint.
     *
     * It says what kind of device is defined for this endpoints.
     * The definition specifies which clusters are mandatory and which one are optional for this device type.
     */
    func getDeviceType(endpoint: NSNumber) async throws -> [MTRDescriptorClusterDeviceTypeStruct] {
        SharedLogger.debug("getDeviceType")
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: endpoint, queue: DispatchQueue.global())
        let result = (try await descriptor?.readAttributeDeviceTypeList())?.map { $0 as! MTRDescriptorClusterDeviceTypeStruct} ?? []
        SharedLogger.debug("Supported device types: \(result)")
        return result
    }

    /**
     * Reads other parts list for the endpoint.
     * Parts list is a set of endpoint logically connected with the endpoint.
     * Root endpoint 0 should return all the endpoints available on the device.
     */
    func readEndpoints() async throws -> [NSNumber] {
        SharedLogger.debug("readEndpoints")
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        let result = (try await descriptor?.readAttributePartsList())?.map { $0 as! NSNumber} ?? []
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
        SharedLogger.debug("readServerClusters")
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: endpoint, queue: DispatchQueue.global())
        let result = (try await descriptor?.readAttributeServerList())?.map { $0 as! NSNumber} ?? []
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
        SharedLogger.debug("readClientClusters")
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: endpoint, queue: DispatchQueue.global())
        let result = (try await descriptor?.readAttributeClientList())?.map { $0 as! NSNumber} ?? []
        SharedLogger.debug("Supported client clusters: \(result)")
        return result
    }
}

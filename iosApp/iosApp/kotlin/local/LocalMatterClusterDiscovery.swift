//
//  LocalMatterClusterDiscovery.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 10/03/2026.
//

import ComposeApp
import Matter
import SharedCode

/// Reads metadata for a Matter device: basic information from the root endpoint (0), and
/// device type plus client/server clusters from each supported endpoint (1..n).
class LocalMatterClusterDiscovery {

    /// The discovery stage currently in progress, used for error reporting if discovery fails.
    var stage: Stage = Stage.readBasicInformation

    private let nodeId: NSNumber
    private let baseDevice: MTRBaseDevice

    /// Creates a discovery helper for the device with the given node ID.
    ///
    /// - Parameter nodeId: The Matter node ID of the target device.
    /// - Throws: An error if the local controller cannot be obtained.
    init(nodeId: NSNumber) throws {
        self.nodeId = nodeId
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        baseDevice = MTRBaseDevice(nodeID: nodeId, controller: controller)
    }

    /// Reads all available metadata for the device and assembles it into a `Device`.
    ///
    /// Vendor name, vendor id, product name, and product id are read from the root endpoint
    /// (0), while device type, client clusters, and server clusters are read from every other
    /// endpoint.
    ///
    /// - Returns: A `Device` containing all discovered metadata.
    /// - Throws: An error if any of the underlying attribute reads fail.
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
    
    /// Maps a raw Matter device type value to a `DeviceType`.
    ///
    /// This example only recognizes a handful of standard device types plus one custom
    /// manufacturer-specific type; anything else maps to `.unsupported`.
    ///
    /// - Parameter deviceType: The raw device type value read from the descriptor cluster.
    /// - Returns: The corresponding `DeviceType`.
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
    
    /// Reads and logs the device type, client clusters, and server clusters for the root
    /// endpoint (0).
    func readEndpoint0() async throws {
        let deviceTypes = try await getDeviceType(endpoint: 0)
        let clientClusters = try await readClientClusters(endpoint: 0)
        let serverClusters = try await readServerClusters(endpoint: 0)
        SharedLogger.debug("Endpoint 0 - devicetypes: \(deviceTypes)")
        SharedLogger.debug("Endpoint 0 - clientClusters: \(clientClusters)")
        SharedLogger.debug("Endpoint 0 - serverClusters: \(serverClusters)")
    }
    
    /// Reads the device type(s) declared for the endpoint.
    ///
    /// The device type defines what kind of device the endpoint represents, and specifies
    /// which clusters are mandatory and which are optional for that device type.
    ///
    /// - Parameter endpoint: The endpoint ID to query.
    /// - Returns: The device types declared for the endpoint.
    /// - Throws: An error if the attribute read fails.
    func getDeviceType(endpoint: NSNumber) async throws -> [MTRDescriptorClusterDeviceTypeStruct] {
        SharedLogger.debug("Cluster Descriptor - getDeviceType()")
        let result = (try await readAttributeDeviceTypeList()).map { $0 as! MTRDescriptorClusterDeviceTypeStruct}
        SharedLogger.debug("Supported device types: \(result)")
        return result
    }

    /// Reads the parts list attribute for the endpoint.
    ///
    /// The parts list is the set of endpoints logically connected to this endpoint. For the
    /// root endpoint (0), this should return all endpoints available on the device.
    ///
    /// - Returns: The endpoint IDs that make up the parts list.
    /// - Throws: An error if the attribute read fails.
    func readEndpoints() async throws -> [NSNumber] {
        SharedLogger.debug("Cluster Descriptor - readEndpoints()")
        let result = (try await readAttributePartsList()).map { $0 as! NSNumber}
        SharedLogger.debug("Supported endpoints: \(result)")
        return result
    }
    
    /// Reads the server clusters list for the endpoint.
    ///
    /// A server cluster implements the logic of a cluster, holds its state, and accepts
    /// commands — for example, a light bulb that receives on/off commands.
    ///
    /// - Parameter endpoint: The endpoint ID to query.
    /// - Returns: The server cluster IDs supported by the endpoint.
    /// - Throws: An error if the attribute read fails.
    func readServerClusters(endpoint: NSNumber) async throws -> [NSNumber] {
        SharedLogger.debug("Cluster Descriptor - readServerClusters()")
        let result = (try await readAttributeServerList()).map { $0 as! NSNumber}
        SharedLogger.debug("Supported server clusters: \(result)")
        return result
    }
    
    /// Reads the client clusters list for the endpoint.
    ///
    /// A client cluster means the device can send commands to the same cluster defined as a
    /// server cluster on another device — for example, a switch that sends on/off commands to
    /// a light bulb.
    ///
    /// - Parameter endpoint: The endpoint ID to query.
    /// - Returns: The client cluster IDs supported by the endpoint.
    /// - Throws: An error if the attribute read fails.
    func readClientClusters(endpoint: NSNumber) async throws -> [NSNumber] {
        SharedLogger.debug("Cluster Descriptor - readClientClusters()")
        let result = (try await readAttributeClientList()).map { $0 as! NSNumber}
        SharedLogger.debug("Supported client clusters: \(result)")
        return result
    }
}

private extension MTRBaseClusterBasicInformation {
    
    /// Reads the device name (node label) from the Basic Information cluster.
    ///
    /// - Returns: The device's node label.
    /// - Throws: An error if the attribute read fails.
    func getName() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getName()")
        let name = try await readAttributeNodeLabel()
        SharedLogger.debug("Name: \(name)")
        return name
    }
    
    /// Reads the product name from the Basic Information cluster.
    ///
    /// - Returns: The product name.
    /// - Throws: An error if the attribute read fails.
    func getProductName() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getProductName()")
        let productName = try await readAttributeProductName()
        SharedLogger.debug("ProductName: \(productName)")
        return productName
    }
    
    /// Reads the product ID from the Basic Information cluster.
    ///
    /// - Returns: The product ID.
    /// - Throws: An error if the attribute read fails.
    func getProductId() async throws -> NSNumber {
        SharedLogger.debug("Basic Information Cluster - getProductId()")
        let productId = try await readAttributeProductID()
        SharedLogger.debug("ProductId: \(productId)")
        return productId
    }
    
    /// Reads the vendor name from the Basic Information cluster.
    ///
    /// - Returns: The vendor name.
    /// - Throws: An error if the attribute read fails.
    func getVendorName() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getVendorName()")
        let vendorName = try await readAttributeVendorName()
        SharedLogger.debug("VendorName: \(vendorName)")
        return vendorName
    }
    
    /// Reads the vendor ID from the Basic Information cluster.
    ///
    /// - Returns: The vendor ID.
    /// - Throws: An error if the attribute read fails.
    func getVendorId() async throws -> NSNumber {
        SharedLogger.debug("Basic Information Cluster - getVendorId()")
        let vendorId = try await readAttributeVendorID()
        SharedLogger.debug("VendorId: \(vendorId)")
        return vendorId
    }
    
    /// Reads the unique ID from the Basic Information cluster.
    ///
    /// - Returns: The device's unique ID.
    /// - Throws: An error if the attribute read fails.
    func getUniqueId() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getUniqueId()")
        let uniqueId = try await readAttributeUniqueID()
        SharedLogger.debug("UniqueId: \(uniqueId)")
        return uniqueId
    }
    
    /// Reads the software version string from the Basic Information cluster.
    ///
    /// - Returns: The software version string.
    /// - Throws: An error if the attribute read fails.
    func getSoftwareVersion() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getSoftwareVersion()")
        let swVersion = try await readAttributeSoftwareVersionString()
        SharedLogger.debug("Software version: \(swVersion)")
        return swVersion
    }
    
    /// Reads the Matter specification version from the Basic Information cluster.
    ///
    /// - Returns: The specification version.
    /// - Throws: An error if the attribute read fails.
    func getSpecificationVersion() async throws -> NSNumber {
        SharedLogger.debug("Basic Information Cluster - getSpecificationVersion()")
        let specVersion = try await readAttributeSpecificationVersion()
        SharedLogger.debug("Specification version: \(specVersion)")
        return specVersion
    }
    
    /// Reads the serial number from the Basic Information cluster.
    ///
    /// - Returns: The device's serial number.
    /// - Throws: An error if the attribute read fails.
    func getSerialNumber() async throws -> String {
        SharedLogger.debug("Basic Information Cluster - getSerialNumber()")
        let serialNumber = try await readAttributeSerialNumber()
        SharedLogger.debug("Serial number: \(serialNumber)")
        return serialNumber
    }
}

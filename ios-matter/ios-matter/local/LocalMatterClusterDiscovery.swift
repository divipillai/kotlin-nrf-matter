//
//  LocalMatterClusterDiscovery.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 10/03/2026.
//

import Matter

/// Reads metadata for a Matter device: basic information from the root endpoint (0), and
/// device type plus client/server clusters from each supported endpoint (1..n).
class LocalMatterClusterDiscovery {

    /// The discovery stage currently in progress, used for error reporting if discovery fails.
//    var stage: Stage = Stage.readBasicInformation

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

    func discoverClusters() async throws -> Device {
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: nodeId, controller: controller)
        
        let basicInfo = try await readBasicInformation(baseDevice: baseDevice)
        
        guard let mainDescriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: 0, queue: .global()) else {
            throw OperationError.unknown
        }
        
        let endpoints: [NSNumber]
        do {
            endpoints = try await mainDescriptor.readEndpoints()
        } catch {
            throw (error as NSError).withMoreUserInfo(deviceId: nodeId, stage: CommissioningStage.readDescriptorCluster)
        }
        
        let deviceMatterInfo = await withTaskGroup(of: DeviceMatterInfo?.self) { group in
            for endpoint in endpoints {
                group.addTask {
                    try? await self.fetchEndpointInfo(baseDevice: baseDevice, endpoint: endpoint)
                }
            }
            
            var results: [DeviceMatterInfo] = []
            for await info in group {
                if let info = info { results.append(info) }
            }
            return results
        }
        
        let primaryDeviceType = deviceMatterInfo.compactMap { $0.types.first }.first ?? 0
        
        SwiftLogger.debug("discoverClusters - finished")
        
        return Device(
            deviceId: nodeId,
            dateCommissioned: NSNumber(value: Date().timeIntervalSince1970 * 1000),
            vendorId: basicInfo.vendorId.stringValue,
            producId: basicInfo.productId.stringValue,
            deviceType: primaryDeviceType,
            name: "Matter device: \(nodeId)",
            productName: basicInfo.productName,
            vendorName: basicInfo.vendorName,
            uniqueId: basicInfo.uniqueId,
            softwareVersion: basicInfo.swVersion,
            specificationVersion: basicInfo.specVersion,
            serialNumber: basicInfo.serialNumber,
            deviceMatterInfo: deviceMatterInfo
        )
    }

    private struct BasicDeviceDetails {
        let vendorId: NSNumber
        let vendorName: String
        let productId: NSNumber
        let productName: String
        let uniqueId: String
        let swVersion: String
        let specVersion: NSNumber
        let serialNumber: String?
    }

    private func readBasicInformation(baseDevice: MTRBaseDevice) async throws -> BasicDeviceDetails {
        guard let cluster = MTRBaseClusterBasicInformation(device: baseDevice, endpointID: 0, queue: .global()) else {
            throw OperationError.unknown
        }
        
        do {
            async let vendorId = cluster.getVendorId()
            async let vendorName = cluster.getVendorName()
            async let productId = cluster.getProductId()
            async let productName = cluster.getProductName()
            async let uniqueId = cluster.getUniqueId()
            async let swVersion = cluster.getSoftwareVersion()
            async let specVersion = cluster.getSpecificationVersion()
            async let serialNumber = cluster.getSerialNumber()
            
            return try await BasicDeviceDetails(
                vendorId: vendorId,
                vendorName: vendorName,
                productId: productId,
                productName: productName,
                uniqueId: uniqueId,
                swVersion: swVersion,
                specVersion: specVersion,
                serialNumber: try? serialNumber
            )
        } catch {
            throw (error as NSError).withMoreUserInfo(deviceId: nodeId, stage: CommissioningStage.readBaseInfo)
        }
    }

    private func fetchEndpointInfo(baseDevice: MTRBaseDevice, endpoint: NSNumber) async throws -> DeviceMatterInfo {
        guard let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: endpoint, queue: .global()) else {
            throw OperationError.unknown
        }
        
        async let deviceTypes = descriptor.getDeviceType(endpoint: endpoint)
        async let clientClusters = descriptor.readClientClusters(endpoint: endpoint)
        async let serverClusters = descriptor.readServerClusters(endpoint: endpoint)
        
        let (fetchedTypes, fetchedClients, fetchedServers) = try await (deviceTypes, clientClusters, serverClusters)
        
        let manufacturerData: ManufacturerSpecificData?
        if fetchedServers.contains(0xFFF1FC01) {
            let controller = LocalMatterCustomClusterController()
            manufacturerData = try await controller.getData(deviceId: nodeId, endpoint: endpoint)
        } else {
            manufacturerData = nil
        }
        
        return DeviceMatterInfo(
            endpoint: endpoint,
            types: fetchedTypes.map { $0.deviceType },
            serverClusters: fetchedServers,
            clientClusters: fetchedClients,
            manufacturerSpecificData: manufacturerData
        )
    }
}

private extension MTRBaseClusterDescriptor {
    
    /// Reads and logs the device type, client clusters, and server clusters for the root
    /// endpoint (0).
    func readEndpoint0() async throws {
        let deviceTypes = try await getDeviceType(endpoint: 0)
        let clientClusters = try await readClientClusters(endpoint: 0)
        let serverClusters = try await readServerClusters(endpoint: 0)
        SwiftLogger.debug("Endpoint 0 - devicetypes: \(deviceTypes)")
        SwiftLogger.debug("Endpoint 0 - clientClusters: \(clientClusters)")
        SwiftLogger.debug("Endpoint 0 - serverClusters: \(serverClusters)")
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
        SwiftLogger.debug("Cluster Descriptor - getDeviceType()")
        let result = (try await readAttributeDeviceTypeList()).map { $0 as! MTRDescriptorClusterDeviceTypeStruct}
        SwiftLogger.debug("Supported device types: \(result)")
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
        SwiftLogger.debug("Cluster Descriptor - readEndpoints()")
        let result = (try await readAttributePartsList()).map { $0 as! NSNumber}
        SwiftLogger.debug("Supported endpoints: \(result)")
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
        SwiftLogger.debug("Cluster Descriptor - readServerClusters()")
        let result = (try await readAttributeServerList()).map { $0 as! NSNumber}
        SwiftLogger.debug("Supported server clusters: \(result)")
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
        SwiftLogger.debug("Cluster Descriptor - readClientClusters()")
        let result = (try await readAttributeClientList()).map { $0 as! NSNumber}
        SwiftLogger.debug("Supported client clusters: \(result)")
        return result
    }
}

private extension MTRBaseClusterBasicInformation {
    
    /// Reads the device name (node label) from the Basic Information cluster.
    ///
    /// - Returns: The device's node label.
    /// - Throws: An error if the attribute read fails.
    func getName() async throws -> String {
        SwiftLogger.debug("Basic Information Cluster - getName()")
        let name = try await readAttributeNodeLabel()
        SwiftLogger.debug("Name: \(name)")
        return name
    }
    
    /// Reads the product name from the Basic Information cluster.
    ///
    /// - Returns: The product name.
    /// - Throws: An error if the attribute read fails.
    func getProductName() async throws -> String {
        SwiftLogger.debug("Basic Information Cluster - getProductName()")
        let productName = try await readAttributeProductName()
        SwiftLogger.debug("ProductName: \(productName)")
        return productName
    }
    
    /// Reads the product ID from the Basic Information cluster.
    ///
    /// - Returns: The product ID.
    /// - Throws: An error if the attribute read fails.
    func getProductId() async throws -> NSNumber {
        SwiftLogger.debug("Basic Information Cluster - getProductId()")
        let productId = try await readAttributeProductID()
        SwiftLogger.debug("ProductId: \(productId)")
        return productId
    }
    
    /// Reads the vendor name from the Basic Information cluster.
    ///
    /// - Returns: The vendor name.
    /// - Throws: An error if the attribute read fails.
    func getVendorName() async throws -> String {
        SwiftLogger.debug("Basic Information Cluster - getVendorName()")
        let vendorName = try await readAttributeVendorName()
        SwiftLogger.debug("VendorName: \(vendorName)")
        return vendorName
    }
    
    /// Reads the vendor ID from the Basic Information cluster.
    ///
    /// - Returns: The vendor ID.
    /// - Throws: An error if the attribute read fails.
    func getVendorId() async throws -> NSNumber {
        SwiftLogger.debug("Basic Information Cluster - getVendorId()")
        let vendorId = try await readAttributeVendorID()
        SwiftLogger.debug("VendorId: \(vendorId)")
        return vendorId
    }
    
    /// Reads the unique ID from the Basic Information cluster.
    ///
    /// - Returns: The device's unique ID.
    /// - Throws: An error if the attribute read fails.
    func getUniqueId() async throws -> String {
        SwiftLogger.debug("Basic Information Cluster - getUniqueId()")
        let uniqueId = try await readAttributeUniqueID()
        SwiftLogger.debug("UniqueId: \(uniqueId)")
        return uniqueId
    }
    
    /// Reads the software version string from the Basic Information cluster.
    ///
    /// - Returns: The software version string.
    /// - Throws: An error if the attribute read fails.
    func getSoftwareVersion() async throws -> String {
        SwiftLogger.debug("Basic Information Cluster - getSoftwareVersion()")
        let swVersion = try await readAttributeSoftwareVersionString()
        SwiftLogger.debug("Software version: \(swVersion)")
        return swVersion
    }
    
    /// Reads the Matter specification version from the Basic Information cluster.
    ///
    /// - Returns: The specification version.
    /// - Throws: An error if the attribute read fails.
    func getSpecificationVersion() async throws -> NSNumber {
        SwiftLogger.debug("Basic Information Cluster - getSpecificationVersion()")
        let specVersion = try await readAttributeSpecificationVersion()
        SwiftLogger.debug("Specification version: \(specVersion)")
        return specVersion
    }
    
    /// Reads the serial number from the Basic Information cluster.
    ///
    /// - Returns: The device's serial number.
    /// - Throws: An error if the attribute read fails.
    func getSerialNumber() async throws -> String {
        SwiftLogger.debug("Basic Information Cluster - getSerialNumber()")
        let serialNumber = try await readAttributeSerialNumber()
        SwiftLogger.debug("Serial number: \(serialNumber)")
        return serialNumber
    }
}

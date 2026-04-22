//
//  LocalMatterCustomClusterController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 20/04/2026.
//

import Matter
import ComposeApp
import SharedCode
import OSLog

class LocalMatterCustomClusterController: MatterManufacturerCustomDataController {
    
    private let endpointId: NSNumber = 1 //todo: hardcoded
    private let clusterId: NSNumber = 0xFFF1FC01 //todo: hardcoded
    private let commandId: NSNumber = 0xFFF10000 //todo: hardcoded
    private let logger = Logger(subsystem: "nrf.matter", category: "LocalMatterCustomClusterController")
    
    func getData(deviceId: DeviceId, endpoint: Int32) async throws -> ManufacturerSpecificData {
        logger.debug("Getting custom manufacturer data...")
        
        if let attributes = try await readAttributes(deviceId: deviceId, endpoint: endpointId, cluster: clusterId) {
            let name = try readDeviceName(from: attributes)
            let led = try readFirstFlag(from: attributes)
            let button = try readSecondFlag(from: attributes)
            
            let data = ManufacturerSpecificData(
                name: name,
                led: led,
                button: button
            )
            
            logger.debug("Custom manufacturer data: \(data)")
            
            return data
        }
        
        throw OperationError.missingAttribute
    }

    func setLed(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        logger.debug("invoke setLed")
        let controller = try LocalControllerProvider(logTag: "LocalMatterCustomClusterController").getController()!
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)
        let fields: NSDictionary = [
            MTRTypeKey: MTRStructureValueType,
            MTRValueKey: [
                [
                    MTRContextTagKey: 0,
                    MTRDataKey: [
                        MTRTypeKey: MTRUnsignedIntegerValueType,
                        MTRValueKey: 2 // toggle
                    ]
                ]
            ]
        ]
        
        try await baseDevice.invokeCommand(withEndpointID: endpointId, clusterID: clusterId, commandID: commandId, commandFields: fields, timedInvokeTimeout: nil, queue: DispatchQueue.global())
    }

    private func readAttributes(deviceId: DeviceId, endpoint: NSNumber, cluster: NSNumber) async throws -> [[String : Any]]? {
        logger.debug("readAttributes")
        let controller = try LocalControllerProvider(logTag: "LocalMatterCustomClusterController").getController()!
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)
        let result = try? await baseDevice.readAttributes(
            withEndpointID: endpoint,
            clusterID: cluster,
            attributeID: nil,
            params: nil,
            queue: DispatchQueue.global()
        )
        logger.debug("Attirbutes for endpoint: \(endpoint), cluster: \(cluster)")
//        printAttributes(result!)
        return result
    }
    
    private func printAttributes(_ array: [[String: Any]]) {
        for (index, dict) in array.enumerated() {
            logger.debug("Item nr \(index):")
            for (key, value) in dict {
                logger.debug("\(key): \(value as! NSObject)")
            }
        }
    }
    
    private func readDeviceName(from items: [[String: Any]]) throws -> String {
        guard let item = items.first,
              let data = item["data"] as? [String: Any],
              let value = data["value"] as? String
        else {
            throw OperationError.missingAttribute
        }

        return value
    }
    
    private func readFirstFlag(from items: [[String: Any]]) throws -> Bool {
        guard items.count > 1 else { throw OperationError.missingAttribute }
        return try readBool(items[1])
    }

    private func readSecondFlag(from items: [[String: Any]]) throws -> Bool {
        guard items.count > 2 else { throw OperationError.missingAttribute }
        return try readBool(items[2])
    }
    
    private func readBool(_ item: [String: Any]) throws -> Bool {
        guard let data = item["data"] as? [String: Any],
              let value = data["value"] else {
            throw OperationError.missingAttribute
        }

        if let bool = value as? Bool {
            return bool
        }
        if let int = value as? Int {
            return int != 0
        }
        throw OperationError.missingAttribute
    }
}

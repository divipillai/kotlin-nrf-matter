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
    
    private let endpoint: NSNumber = 1 //todo: hardcoded
    private let cluster: NSNumber = 0xFFF1FC01 //todo: hardcoded
    private let logger = Logger(subsystem: "nrf.matter", category: "LocalMatterCustomClusterController")

    func setLed(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        
    }
    
    func readAttributes(deviceId: DeviceId) async throws {
        logger.debug("Specific manufacturer data")
        let name = try await getName(deviceId: deviceId)
        logger.debug("Name: \(name!)")
        let led = try await getLed(deviceId: deviceId)
        logger.debug("Led: \(led!)")
        let button = try await getButton(deviceId: deviceId)
        logger.debug("Button: \(button!)")
    }
    
    private func getName(deviceId: DeviceId) async throws -> String? {
        if let attributes = try await readAttributes(deviceId: deviceId, endpoint: endpoint, cluster: cluster) {
            return readDeviceName(from: attributes)
        }
        return nil
    }
    
    private func getLed(deviceId: DeviceId) async throws -> Bool? {
        if let attributes = try await readAttributes(deviceId: deviceId, endpoint: endpoint, cluster: cluster) {
            return readFirstFlag(from: attributes)
        }
        return nil
    }
    
    private func getButton(deviceId: DeviceId) async throws -> Bool? {
        if let attributes = try await readAttributes(deviceId: deviceId, endpoint: endpoint, cluster: cluster) {
            return readSecondFlag(from: attributes)
        }
        return nil
    }
    
    func readAttributes(deviceId: DeviceId, endpoint: NSNumber, cluster: NSNumber) async throws -> [[String : Any]]? {
        logger.debug("readAttributes")
        let controller = try LocalControllerProvider(logTag: "LocalMatterCustomClusterController").getController()!
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)
        let result = try? await baseDevice.readAttributes(withEndpointID: endpoint, clusterID: cluster, attributeID: nil, params: nil, queue: DispatchQueue.global())
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
    
    func readDeviceName(from items: [[String: Any]]) -> String? {
        guard let item = items.first,
              let data = item["data"] as? [String: Any],
              let value = data["value"] as? String else {
            return nil
        }

        return value
    }
    
    func readFirstFlag(from items: [[String: Any]]) -> Bool? {
        guard items.count > 1 else { return nil }
        return readBool(items[1])
    }

    func readSecondFlag(from items: [[String: Any]]) -> Bool? {
        guard items.count > 2 else { return nil }
        return readBool(items[2])
    }
    
    func readBool(_ item: [String: Any]) -> Bool? {
        guard let data = item["data"] as? [String: Any],
              let value = data["value"] else {
            return nil
        }

        if let bool = value as? Bool {
            return bool
        }
        if let int = value as? Int {
            return int != 0
        }
        return nil
    }
}

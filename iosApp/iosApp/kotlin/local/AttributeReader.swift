//
//  AttributeReader.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/04/2026.
//

import Matter
import SharedCode
import OSLog

class AttributeReader {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "AttributeReader")
    private let baseDevice: MTRBaseDevice
    
    init(deviceId: NSNumber) throws {
        let controller = try LocalControllerProvider(logTag: "LocalMatterCustomClusterController").getController()
        baseDevice = MTRBaseDevice(nodeID: deviceId, controller: controller)
    }
    
    func readAttribute<T: AttributeParser>(endpoint: NSNumber, cluster: NSNumber, attribute: NSNumber) async throws -> T {
        let result = try await readAttribute(endpoint: endpoint, cluster: cluster, attribute: attribute)
        return try T.parse(value: result)
    }
    
    private func readAttribute(endpoint: NSNumber, cluster: NSNumber, attribute: NSNumber) async throws -> Any {
        logger.debug("readAttributes")

        let result = try? await baseDevice.readAttributes(
            withEndpointID: endpoint,
            clusterID: cluster,
            attributeID: attribute,
            params: nil,
            queue: DispatchQueue.global()
        )
        
        guard let result else {
            throw OperationError.missingAttribute
        }
        
        logger.debug("Attirbutes for endpoint: \(endpoint), cluster: \(cluster)")
//        printAttributes(result!)
        return try readAny(result[0])
    }
    
    private func printAttributes(_ array: [[String: Any]]) {
        for (index, dict) in array.enumerated() {
            logger.debug("Item nr \(index):")
            for (key, value) in dict {
                logger.debug("\(key): \(value as! NSObject)")
            }
        }
    }
    
    private func readAny(_ item: [String: Any]) throws -> Any {
        guard let data = item["data"] as? [String: Any],
              let value = data["value"] else {
            throw OperationError.missingAttribute
        }
        return value
    }
}

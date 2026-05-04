//
//  AttributeReader.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/04/2026.
//

import Matter
import SharedCode

class AttributeReader {

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
        SharedLogger.debug("readAttributes")

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
        
        SharedLogger.debug("Attirbutes for endpoint: \(endpoint), cluster: \(cluster)")

        return try result[0].readAny()
    }
    
    private func printAttributes(_ array: [[String: Any]]) {
        for (index, dict) in array.enumerated() {
            SharedLogger.debug("Item nr \(index):")
            for (key, value) in dict {
                SharedLogger.debug("\(key): \(value as! NSObject)")
            }
        }
    }
}

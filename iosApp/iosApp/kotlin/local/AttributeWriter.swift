//
//  AttributeWriter.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/04/2026.
//

import Matter
import SharedCode
import OSLog

class AttributeWriter {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "AttributeWriter")
    private let baseDevice: MTRBaseDevice
    
    init(deviceId: NSNumber) throws {
        let controller = try LocalControllerProvider(logTag: "AttributeWriter").getController()
        baseDevice = MTRBaseDevice(nodeID: deviceId, controller: controller)
    }
    
    func executeCommand(endpoint: NSNumber, cluster: NSNumber, command: NSNumber, type: String, value: Any) async throws {
        logger.debug("Executing command: \(command)")

        let fields: NSDictionary = [
            MTRTypeKey: MTRStructureValueType,
            MTRValueKey: [
                [
                    MTRContextTagKey: 0,
                    MTRDataKey: [
                        MTRTypeKey: type,
                        MTRValueKey: value // toggle
                    ]
                ]
            ]
        ]
        
        try await baseDevice.invokeCommand(withEndpointID: endpoint, clusterID: cluster, commandID: command, commandFields: fields, timedInvokeTimeout: nil, queue: DispatchQueue.global())
        
        logger.debug("Command executed successfully.")
    }
}

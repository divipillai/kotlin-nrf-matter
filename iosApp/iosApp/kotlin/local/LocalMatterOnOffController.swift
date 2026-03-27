//
//  MatterOnOffController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 06/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import OSLog

class LocalMatterOnOffController : MatterOnOffController {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "LocalMatterOnOffController")

    func setDeviceOnOff(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()!
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)

        let cluster = MTRBaseClusterOnOff(device: baseDevice, endpointID: endpoint as NSNumber, queue: DispatchQueue.global())
        logger.debug("Cluster created: \(cluster)")
        if (isOn) {
            try await cluster?.on()
        } else {
            try await cluster?.off()
        }
    }
}

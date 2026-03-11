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

class MatterOnOffControllerImpl : MatterOnOffController {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "MatterOnOffController")

    func setDeviceOnOff(deviceId: Int64, isDeviceOnline: Bool, isOn: Bool, endpoint: Int32) async throws {
        let controller = MatterControllerProviderImpl().getController()!
        let baseDevice = MTRBaseDevice(nodeID: deviceId as NSNumber, controller: controller)

        let cluster = MTRBaseClusterOnOff(device: baseDevice, endpointID: endpoint as NSNumber, queue: DispatchQueue.global())
        logger.debug("Cluster created: \(cluster)")
        if (isOn) {
            cluster?.on { [weak self] error in
                if let error {
                    self?.logger.debug("Error during on")
                } else {
                    self?.logger.debug("Success during on")
                }
            }
        } else {
            cluster?.off { [weak self] error in
                if let error {
                    self?.logger.debug("Error during off")
                } else {
                    self?.logger.debug("Success during off")
                }
            }
        }
        
    }
}

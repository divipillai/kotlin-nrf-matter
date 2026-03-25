//
//  LocalMatterOutletController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 25/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import OSLog

class LocalMatterOutletController : MatterOutletController {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "LocalMatterOutletController")
    
    func handleOutlet(deviceId: DeviceId, isSwitchOn: Bool, endpoint: Int32) async throws {
        let controller = LocalMatterOnOffController()
        try await controller.setDeviceOnOff(deviceId: deviceId, isOn: isSwitchOn, endpoint: endpoint)
    }
}

//
//  HomeKitMatterOnOffController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 13/03/2026.
//

import ComposeApp
import HomeKit
import Matter
import SharedCode
import OSLog

class HomeKitMatterOnOffController : MatterOnOffController {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "HomeKitMatterOnOffController")

    func setDeviceOnOff(deviceId: Int64, isDeviceOnline: Bool, isOn: Bool, endpoint: Int32) async throws {
        let controller = HomeKitController.shared()
        let accessory = controller.getAccesory(nodeId: deviceId)
        
        guard let accessory else { return }
        
        guard let service = accessory.services.first(where: {
            $0.serviceType == HMServiceTypeLightbulb
        }) else { return }

        guard let characteristic = service.characteristics.first(where: {
            $0.characteristicType == HMCharacteristicTypePowerState
        }) else { return }

        try await characteristic.writeValue(isOn)
    }
}


//
//  GoogleHomeOnOffController .swift
//  iosApp
//
//  Created by Sylwester Zielinski on 16/03/2026.
//

import ComposeApp
import HomeKit
import Matter
import SharedCode
import OSLog

class GoogleHomeOnOffController : MatterOnOffController {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "GoogleHomeOnOffController")

    func setDeviceOnOff(deviceId: Int64, isDeviceOnline: Bool, isOn: Bool, endpoint: Int32) async throws {
        let controller = HomeKitController.shared()
        let accessory = controller.getAccessory(nodeId: deviceId)
        
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

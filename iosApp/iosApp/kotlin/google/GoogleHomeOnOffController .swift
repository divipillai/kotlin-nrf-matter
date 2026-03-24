//
//  GoogleHomeOnOffController .swift
//  iosApp
//
//  Created by Sylwester Zielinski on 16/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import OSLog
import GoogleHomeSDK
import GoogleHomeTypes

class GoogleHomeOnOffController : MatterOnOffController {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "GoogleHomeOnOffController")

    func setDeviceOnOff(deviceId: DeviceId, isDeviceOnline: Bool, isOn: Bool, endpoint: Int32) async throws {
        let controller = GoogleHomeController.instance()
        
        let device = await controller.getDevice(id: deviceId.stringValue)
        
        guard let device else { return }
        
        guard let lightType = await device.types.get(OnOffLightDeviceType.self) else {
            print("This device does not support standard On/Off light controls.")
            return
        }
        
        if let onOffTrait = lightType.matterTraits.onOffTrait {
            if isOn {
                try await onOffTrait.on()
            } else {
                try await onOffTrait.off()
            }
            
            print("Successfully updated the bulb state.")
        }
    }
}

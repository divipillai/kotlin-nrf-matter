//
//  GoogleHomeOnOffController .swift
//  iosApp
//
//  Created by Sylwester Zielinski on 16/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import GoogleHomeSDK
import GoogleHomeTypes

class GoogleHomeOnOffController : MatterOnOffController {

    func setDeviceOnOff(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        let controller = GoogleHomeController.instance()
        await controller.initialize()
        let structure = await controller.getStructure()
        
        let device = await controller.getDevice(id: deviceId.stringValue)
        
        guard let device else { return }
        
        guard let lightType = await device.parts.get(OnOffLightDeviceType.self) else {
            return
        }
        
        do {
            if let onOffTrait = lightType.matterTraits.onOffTrait {
                if isOn {
                    try await onOffTrait.on()
                } else {
                    try await onOffTrait.off()
                }
            }
        } catch {
            
        }

    }
}

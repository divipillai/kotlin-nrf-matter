//
//  GoogleHomeLightController .swift
//  iosApp
//
//  Created by Sylwester Zielinski on 16/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import GoogleHomeSDK
import GoogleHomeTypes

/**
 * A helper class from controlling a light type Matter device using Googel Home hub.
 */
class GoogleHomeLightController : MatterLightController {

    /**
     * Set the light on/off on a remote Matter device.
     */
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
    
    /**
     * Set the brightness level on a remote Matter device.
     */
    func setBrightnessLevel(deviceId: DeviceId, level: Int32, endpoint: Int32) async throws {
        //TODO
    }
    
    /**
     * Observe if a device is on changes on a remote Matter device.
     */
    func subscribeToLedChanges(deviceId: DeviceId, endpoint: Int32, onUpdate: @escaping (KotlinBoolean) -> Void) async throws {
        //TODO
    }
    
    /**
     * Observe light level changes on a remote Matter device.
     */
    func subscribeToLightLevelChanges(deviceId: DeviceId, endpoint: Int32, onUpdate: @escaping (KotlinFloat) -> Void) async throws {
        //TODO
    }
}

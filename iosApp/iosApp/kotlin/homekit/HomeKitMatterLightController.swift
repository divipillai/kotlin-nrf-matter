//
//  HomeKitMatterLightController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 13/03/2026.
//

import ComposeApp
import HomeKit
import Matter
import SharedCode

/**
 * A helper class from controlling a light type Matter device using Home Kit app
 */
class HomeKitMatterLightController : MatterLightController {
    

    

    /**
     * Set the light on/off on a remote Matter device.
     */
    func setDeviceOnOff(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        let controller = HomeKitController.shared()
        let accessory = controller.getAccessory(deviceId: deviceId)
        
        guard let accessory else { return }
        
        guard let service = accessory.services.first(where: {
            $0.serviceType == HMServiceTypeLightbulb
        }) else { return }

        guard let characteristic = service.characteristics.first(where: {
            $0.characteristicType == HMCharacteristicTypePowerState
        }) else { return }

        try await characteristic.writeValue(isOn)
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

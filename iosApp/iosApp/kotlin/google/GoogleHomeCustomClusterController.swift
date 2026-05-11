//
//  GoogleHomeCustomClusterController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 08/05/2026.
//

import ComposeApp
import Matter
import SharedCode
import GoogleHomeSDK
import GoogleHomeTypes

enum GoogleHomeCustomClusterError : Error {
    case missingTraits
}

class GoogleHomeCustomClusterController : MatterManufacturerCustomDataController {
    
    private func getTrait(deviceId: DeviceId) async throws -> NordicSemiconductor.NordicCustomClusterTrait {
        let controller = GoogleHomeController.instance()
        await controller.initialize()
        
        let device = await controller.getDevice(id: deviceId.stringValue)
        
        guard let device else { throw GoogleHomeCustomClusterError.missingTraits }
        
        guard let lightType = await device.parts.get(OnOffLightDeviceType.self) else {
            throw GoogleHomeCustomClusterError.missingTraits
        }
        
        guard let trait = lightType.traits[NordicSemiconductor.NordicCustomClusterTrait.self] else {
            throw GoogleHomeCustomClusterError.missingTraits
        }

        return trait
    }
    
    func getData(deviceId: DeviceId, endpoint: Int32) async throws -> ManufacturerSpecificData {
        let trait = try await getTrait(deviceId: deviceId)
        let name = trait.attributes.developmentKitName ?? ""
        let led = trait.attributes.userLed ?? false
        let button = trait.attributes.userButton ?? false
        
        return ManufacturerSpecificData(name: name, led: led, button: button)
    }
    
    
    func setLed(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        let trait = try await getTrait(deviceId: deviceId)
        try await trait.setLed(state: isOn ? 0 : 1)
    }
    
    func subscribeToButtonChanges(deviceId: DeviceId, endpoint: Int32, onUpdate: @escaping (KotlinBoolean) -> Void) async throws {
        //todo
    }
}

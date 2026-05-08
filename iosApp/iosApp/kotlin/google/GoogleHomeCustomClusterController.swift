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

class GoogleHomeCustomClusterController : MatterManufacturerCustomDataController {
    
    func getData(deviceId: DeviceId, endpoint: Int32) async throws -> ManufacturerSpecificData {
        let controller = GoogleHomeController.instance()
        await controller.initialize()
        let structure = await controller.getStructure()
        
        let device = await controller.getDevice(id: deviceId.stringValue)
        
//        guard let device else { return }
        
        
    }
    
    
    func setLed(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        //todo
    }
    
    func subscribeToButtonChanges(deviceId: DeviceId, endpoint: Int32, onUpdate: @escaping (KotlinBoolean) -> Void) async throws {
        //todo
    }
}

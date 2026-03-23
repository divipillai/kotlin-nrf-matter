//
//  HomeKitCommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 12/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import HomeKit

class HomeKitCommissioner : MatterCommissioner {

    func startIosCommissioning(onError: @escaping () -> Void) async throws -> Device? {
        let controller = HomeKitController.shared()
        let uuid = await controller.addAccessory()
        
        guard let uuid else {
            print("Couldn't add accessory.")
            return nil
        }
      
        let accessory = controller.getAccessory(uuid: uuid)
        
        guard let accessory else {
            print("Couldn't find accessory.")
            return nil
        }
        
        let nodeId = accessory.matterNodeID
        
        guard let nodeId else { return nil }
        
        return Device(
            deviceId: DeviceId(value: String(nodeId)),
            dateCommissioned: KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000)),
            vendorId: nil,
            productId: nil,
            deviceType: .lightOnOff,
            name: accessory.name,
            productName: accessory.model,
            vendorName: accessory.manufacturer,
            deviceMatterInfo: []
        )
    }
}

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

/**
 * Starts commissioning of a new device to Home Kit app.
 * Newly added deviecs should be also visible in Home Kit app.
 * This approach seems to be able to utilise build-in Thread network available
 * in newer iPhone's.
 */
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
            vendorId: nil, //todo
            productId: nil, //todo
            deviceType: .lightOnOff,
            name: accessory.name,
            productName: accessory.model,
            vendorName: accessory.manufacturer,
            deviceMatterInfo: [] //todo
        )
    }
}

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

    func startIosCommissioning(deviceId: DeviceId) async throws -> Device { // TODO: use deviceID
        let controller = HomeKitController.shared()
        let uuid = await controller.addAccessory()
        
        guard let uuid else {
            print("Couldn't add accessory.")
            throw OperationError.unknown
        }
      
        let accessory = controller.getAccessory(uuid: uuid)
        
        guard let accessory else {
            print("Couldn't find accessory.")
            throw OperationError.unknown
        }
        
        let nodeId = accessory.matterNodeID
        
        guard let nodeId else { throw OperationError.unknown }
        
        return Device(
            deviceId: DeviceId(value: String(nodeId)),
            dateCommissioned: KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000)),
            vendorId: nil, //todo
            productId: nil, //todo
            deviceType: .lightOnOff,
            name: accessory.name,
            productName: accessory.model,
            vendorName: accessory.manufacturer,
            uniqueId: "", //todo
            softwareVersion: "", //todo
            specificationVersion: KotlinLong(value: 0),
            serialNumer: "", //todo
            deviceMatterInfo: [] //todo
        )
    }
}

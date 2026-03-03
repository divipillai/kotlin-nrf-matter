//
//  MatterControllerProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 03/03/2026.
//

import Matter

class MatterControllerProvider {
    
    func getController() throws -> MTRDeviceController? {
        let nodeID: NSNumber = 1 // todo
        
        let factory = MTRDeviceControllerFactory.sharedInstance()

        let storage = MatterStorage()
        let factoryParams = MTRDeviceControllerFactoryParams(storage: storage)
        
        try factory.start(factoryParams)
        
        guard let ipk = NSMutableData(length: 16) else {
            return nil
        }

        let status = SecRandomCopyBytes(kSecRandomDefault, ipk.count, ipk.mutableBytes)
        
        if status != errSecSuccess {
            return nil
        } else {
            // Save the IPK in the keychain.
        }
        
        let params = MTRDeviceControllerStartupParams(
            ipk: ipk as Data,
            fabricID: 1,
            nocSigner: MatterKeypair(),
        )
        params.vendorID = 0xFFF1
        
        var controller: MTRDeviceController? = nil
        do {
            print("Controller from existing fabric")
            controller = try factory.createController(onExistingFabric: params)
        } catch {
            do {
                print("Controller from new fabric")
                controller = try factory.createController(onNewFabric: params)
            } catch {
                return nil
            }
        }
        return controller
    }
}

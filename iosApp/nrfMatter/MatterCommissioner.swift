//
//  MatterCommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter

class MatterCommissioner {
    
    func commision(payload: String) throws {
        let nodeID: NSNumber = 1 // todo
        
        let factory = MTRDeviceControllerFactory.sharedInstance()

        let storage = MatterStorage()
        let factoryParams = MTRDeviceControllerFactoryParams(storage: storage)
        
        try factory.start(factoryParams)
        
        guard let ipk = NSMutableData(length: 16) else {
            return
        }

        let status = SecRandomCopyBytes(kSecRandomDefault, ipk.count, ipk.mutableBytes)
        
        if status != errSecSuccess {
            // Handle any errors.
        } else {
            // Save the IPK in the keychain.
        }
        
        let params = MTRDeviceControllerStartupParams(
            ipk: ipk as Data,
            fabricID: 1,
            nocSigner: MatterKeypair(),
        )
        
        var controller: MTRDeviceController? = nil
        do {
            controller = try factory.createController(onNewFabric: params)
        } catch {
            do {
                controller = try factory.createController(onExistingFabric: params)
            } catch {
                // Handle errors.
            }
        }
        guard let controller = controller else { return }
        
        let delegate = MatterControllerDelegate(nodeID: nodeID) //todo nodeID
        controller.setDeviceControllerDelegate(delegate, queue: DispatchQueue.main)

        let payload = MTRSetupPayload(payload: payload)!
        try! controller.setupCommissioningSession(with: payload, newNodeID: nodeID)
    }
}

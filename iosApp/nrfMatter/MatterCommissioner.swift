//
//  MatterCommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter

class MatterCommissioner {
    
    func commision(payload: String) async throws {
        let nodeID: NSNumber = 1 // todo
        
        let provider = MatterControllerProvider()
        guard let controller = try? provider.getController() else { return }
        
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
     
            let delegate = MatterControllerDelegate(nodeID: nodeID, continuation: continuation, threadNetwork: nil) //todo nodeID
            controller.setDeviceControllerDelegate(delegate, queue: DispatchQueue.main)

            let payload = MTRSetupPayload(payload: payload)!
            try! controller.setupCommissioningSession(with: payload, newNodeID: nodeID)
        }
    }
}

//
//  MatterCommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter
import SharedCode

class MatterCommissioner {
    
    let provider = LocalControllerProvider(logTag: "MatterCommissioner")
    
    func commision(payload: String, nodeID: NSNumber) async throws {
        guard let controller = try? provider.getController() else { return }
        
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
     
            let delegate = MatterControllerDelegate(nodeID: nodeID, continuation: continuation) //todo nodeID
            controller.setDeviceControllerDelegate(delegate, queue: DispatchQueue.main)

            let payload = MTRSetupPayload(payload: payload)!
            try! controller.setupCommissioningSession(with: payload, newNodeID: nodeID)
        }
    }
    
    func release() {
        provider.release()
    }
}

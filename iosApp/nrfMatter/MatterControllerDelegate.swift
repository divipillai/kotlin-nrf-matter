//
//  MatterControllerDelegate.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter

class MatterControllerDelegate : NSObject, MTRDeviceControllerDelegate {
    
    let nodeID: NSNumber
    let continuation: CheckedContinuation<Void, Never>
    
    init(nodeID: NSNumber, continuation: CheckedContinuation<Void, Never>) {
        self.nodeID = nodeID
        self.continuation = continuation
    }
    
    func controller(_ controller: MTRDeviceController, statusUpdate status: MTRCommissioningStatus) {
        // Check for `MTRCommissioningStatus.failed`, and if so, handle it.
        print("MatterControllerDelegate - statusUpdate: \(status).")
    }

    func controller(_ controller: MTRDeviceController, commissioningSessionEstablishmentDone error: Error?) {
        // Check for error and handle it.
        print("MatterControllerDelegate - commissioningSessionEstablishmentDone.")
        do {
            let commissioningParams = MTRCommissioningParameters()
            commissioningParams.deviceAttestationDelegate = MatterAttestationDelegate()
            
            // `myDesiredNodeID` must match the node ID passed to
            // `setupCommissioningSessionWithPayload`.
            try controller.commissionNode(
                withID: nodeID,
                commissioningParams: commissioningParams,
            )
            print("Succcessfully commissioned device.")
        } catch {
            // Handle failure to start commissioning the node.
            print("Commissioning node failed.")
        }


        // Keep waiting for `commissioningComplete`.
    }


    func controller(_ controller: MTRDeviceController, commissioningComplete error: Error?, nodeID: NSNumber?) {
        print("MatterControllerDelegate - commissioningComplete.")
        continuation.resume()
        // Check for error and handle it.
        // If no error, node is commissioned with `nodeID` as its node ID.
    }
}

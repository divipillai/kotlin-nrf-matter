//
//  MatterControllerDelegate.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter

class MatterControllerDelegate : NSObject, MTRDeviceControllerDelegate {
    
    let nodeID: NSNumber
    
    init(nodeID: NSNumber) {
        self.nodeID = nodeID
    }
    
    func controller(_ controller: MTRDeviceController, statusUpdate status: MTRCommissioningStatus) {
        // Check for `MTRCommissioningStatus.failed`, and if so, handle it.
        print("MatterControllerDelegate - statusUpdate: \(status).")
    }


    func controller(_ controller: MTRDeviceController, commissioningSessionEstablishmentDone error: Error?) {
        // Check for error and handle it.
        print("MatterControllerDelegate - commissioningSessionEstablishmentDone.")
        do {
            // `myDesiredNodeID` must match the node ID passed to
            // `setupCommissioningSessionWithPayload`.
            try controller.commissionNode(
                withID: nodeID,
                commissioningParams: MTRCommissioningParameters()
            )
            print("Succcessfully commissioned device.")
        } catch {
            // Handle failure to start commissioning the node.
            print("Commissioning node failed.")
        }


        // Keep waiting for `commissioningComplete`.
    }


    func controller(_ controller: MTRDeviceController, commissioningComplete error: Error?, nodeID: NSNumber?) {
        print("MatterControllerDelegate - statusUpdate: \(status).")
        // Check for error and handle it.
        // If no error, node is commissioned with `nodeID` as its node ID.
    }
}

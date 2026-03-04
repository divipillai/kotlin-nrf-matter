//
//  MatterControllerDelegate.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter
import ThreadNetwork

class MatterControllerDelegate : NSObject, MTRDeviceControllerDelegate {
    
    let nodeID: NSNumber
    let continuation: CheckedContinuation<Void, Never>
    let threadNetwork: THCredentials?
    
    init(nodeID: NSNumber, continuation: CheckedContinuation<Void, Never>, threadNetwork: THCredentials?) {
        self.nodeID = nodeID
        self.continuation = continuation
        self.threadNetwork = threadNetwork
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
            if let threadNetwork {
                commissioningParams.threadOperationalDataset = threadNetwork.activeOperationalDataSet
            }
       
            if #available(iOS 26.2, *) {
                commissioningParams.forceThreadScan = true
            } else {
                // Fallback on earlier versions
            }
            
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
        controller.shutdown()
        // Check for error and handle it.
        // If no error, node is commissioned with `nodeID` as its node ID.
    }
}

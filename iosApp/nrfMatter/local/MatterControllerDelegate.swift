//
//  MatterControllerDelegate.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter
import SharedCode

class MatterControllerDelegate : NSObject, MTRDeviceControllerDelegate {
    
    let nodeID: NSNumber
    let continuation: CheckedContinuation<Void, Never>
    
    init(nodeID: NSNumber, continuation: CheckedContinuation<Void, Never>) {
        self.nodeID = nodeID
        self.continuation = continuation
    }
    
    func controller(_ controller: MTRDeviceController, statusUpdate status: MTRCommissioningStatus) {
        SharedLogger.debug("Status update: \(status.rawValue).")
    }

    func controller(_ controller: MTRDeviceController, commissioningSessionEstablishmentDone error: Error?) {
        SharedLogger.debug("Commissioning session establishement done.")
        do {
            let commissioningParams = MTRCommissioningParameters()
            commissioningParams.deviceAttestationDelegate = MatterAttestationDelegate()
       
            if #available(iOS 26.2, *) {
                commissioningParams.forceThreadScan = true
            }
            
            try controller.commissionNode(
                withID: nodeID,
                commissioningParams: commissioningParams,
            )
            SharedLogger.debug("Succcessfully commissioned device.")
        } catch {
            SharedLogger.debug("Commissioning node failed.")
        }
    }

    func controller(_ controller: MTRDeviceController, commissioningComplete error: Error?, nodeID: NSNumber?) {
        SharedLogger.debug("Commissioning complete.")
        continuation.resume()
        controller.shutdown()
    }
}

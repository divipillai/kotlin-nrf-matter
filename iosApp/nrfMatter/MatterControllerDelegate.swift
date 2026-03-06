//
//  MatterControllerDelegate.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter
import ThreadNetwork
import OSLog

class MatterControllerDelegate : NSObject, MTRDeviceControllerDelegate {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "MatterControllerDelegate")
    
    let nodeID: NSNumber
    let continuation: CheckedContinuation<Void, Never>
    let threadNetwork: THCredentials?
    
    init(nodeID: NSNumber, continuation: CheckedContinuation<Void, Never>, threadNetwork: THCredentials?) {
        self.nodeID = nodeID
        self.continuation = continuation
        self.threadNetwork = threadNetwork
    }
    
    func controller(_ controller: MTRDeviceController, statusUpdate status: MTRCommissioningStatus) {
        logger.debug("Status update: \(status.rawValue).")
    }

    func controller(_ controller: MTRDeviceController, commissioningSessionEstablishmentDone error: Error?) {
        logger.debug("Commissioning session establishement done.")
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
            
            try controller.commissionNode(
                withID: nodeID,
                commissioningParams: commissioningParams,
            )
            logger.debug("Succcessfully commissioned device.")
        } catch {
            logger.debug("Commissioning node failed.")
        }
    }

    func controller(_ controller: MTRDeviceController, commissioningComplete error: Error?, nodeID: NSNumber?) {
        logger.debug("Commissioning complete.")
        continuation.resume()
        controller.shutdown()
    }
}

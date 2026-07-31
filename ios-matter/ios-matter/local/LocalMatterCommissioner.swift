//
//  MyMatterSupport.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import Matter
import MatterSupport

enum Stage {
    case commissioning
    case readBaseInfo
    case readDescriptorCluster
}

struct CommissioningException : Error {
    let deviceId: NSNumber
    let stage: Stage
    let errorCode: Int?
    let displayMessage: String
    let fabricId: Int
}

/// Keeps the failure message readable once the error is flattened into an `NSError` on its way
/// through the Objective-C bridge into Kotlin.
extension CommissioningException: LocalizedError {
    var errorDescription: String? { displayMessage }
}

/// Commissions a new Matter device into the local fabric.
///
/// A new device can be commissioned over Wi-Fi or Thread. Commissioning a Thread device
/// requires a Thread Border Router available on the local network and Thread network
/// credentials stored on the phone. Once commissioned, the device is added to the local fabric
/// and managed by the phone.
@objc public final class LocalMatterCommissioner: NSObject {

    /// Commissions a new Matter device into the local fabric using Apple's MatterSupport
    /// add-device flow.
    ///
    /// During the process, control moves to the system app extension, which provides the UI
    /// for scanning the QR code and choosing among available Thread networks. The local fabric
    /// is shared between the main app and the app extension via App Groups.
    ///
    /// After successful commissioning, the descriptor clusters for all available endpoints are
    /// read and the resulting device metadata is returned.
    ///
    /// - Parameter deviceId: The Matter node ID to assign to the newly commissioned device.
    /// - Returns: An `OperationResultSuccess` containing the discovered `Device` on success, or
    ///   an `OperationResultError` describing the failure.
    /// - Throws: An error if the local controller needed for post-commissioning cluster
    ///   discovery cannot be obtained.
    @objc public func startIosCommissioning(deviceId: NSNumber) async throws -> Device {
        let homes = [MatterAddDeviceRequest.Home(displayName: "Nordic Home")]
        let topology = MatterAddDeviceRequest.Topology(ecosystemName: "Nordic Ecosystem", homes: homes)
        
        let request = MatterAddDeviceRequest(topology: topology, shouldScanNetworks: true)
        
        let storage = SharedStorage(suitName: SharedConsts.sharedStorage)
        let _ = storage.removeStorageData(forKey: SharedConsts.resultKey)
        storage.storeString(key: SharedConsts.matterEnvStorageKey, value: MatterEnv.local.rawValue)
        storage.storeNumber(key: SharedConsts.nodeIdKey, value: deviceId)
        
        do {
            try await request.perform()
        } catch {
            let error = error as NSError
            throw CommissioningException(
                deviceId: deviceId,
                stage: Stage.commissioning,
                errorCode: error.code,
                displayMessage: error.localizedDescription,
                fabricId: 1
            )
        }
        
        let result = storage.getBool(key: SharedConsts.resultKey) ?? false
        guard result else {
            throw CommissioningException(
                deviceId: deviceId,
                stage: Stage.commissioning,
                errorCode: nil,
                displayMessage: "Cancelled.",
                fabricId: 1
            )
        }
        
        let descriptorCluster = try LocalMatterClusterDiscovery(nodeId: deviceId)
        
        do {
            let device = try await descriptorCluster.discoverClusters()
            return device
        } catch {
            let error = error as NSError
            throw CommissioningException(
                deviceId: deviceId,
                stage: Stage.readDescriptorCluster, //todo
                errorCode: error.code,
                displayMessage: error.localizedDescription,
                fabricId: 1
            )
        }
    }
}

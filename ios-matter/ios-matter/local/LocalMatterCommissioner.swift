//
//  MyMatterSupport.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import Matter
import MatterSupport

@objc public enum CommissioningStage: Int {
    case commissioning
    case readBaseInfo
    case readDescriptorCluster
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
        
        let storage = SharedStorage()
        let _ = storage.removeStorageData(forKey: SharedConsts.resultKey)
        storage.storeString(key: SharedConsts.matterEnvStorageKey, value: MatterEnv.local.rawValue)
        storage.storeNumber(key: SharedConsts.nodeIdKey, value: deviceId)
        
        do {
            try await request.perform()
        } catch {
            let nsError = error as NSError
            throw nsError.withMoreUserInfo(
                deviceId: deviceId,
                stage: CommissioningStage.commissioning
            )
        }
        
        let result = storage.getBool(key: SharedConsts.resultKey) ?? false
        guard result else {
            let nsError = NSError()
            throw nsError.withMoreUserInfo(
                deviceId: deviceId,
                stage: CommissioningStage.commissioning,
                displayMessage: "Cancelled.",
            )
        }
        
        let descriptorCluster = try LocalMatterClusterDiscovery(nodeId: deviceId)
        
        let device = try await descriptorCluster.discoverClusters()
        return device
    }
}

extension NSError {
    
    func withMoreUserInfo(
        deviceId: NSNumber,
        stage: CommissioningStage,
        displayMessage: String? = nil,
    ) -> NSError {
        var userInfo = userInfo
        userInfo["deviceId"] = deviceId
        userInfo["stage"] = stage.rawValue
        userInfo["displayMessage"] = displayMessage ?? localizedDescription
        userInfo["fabricId"] = 1

        return NSError(
            domain: domain,
            code: code,
            userInfo: userInfo
        )
    }
}

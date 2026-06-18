//
//  MyMatterSupport.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import ComposeApp
import Matter
import MatterSupport
import nrfMatter
import SharedCode

/**
 * A helper class for commssioning a new matter device.
 * A new device may be commissioned using WiFi or Thread network.
 * A thread device requires Thread Border Router available in the local network
 * and network credentials stored on the phone.
 * The new device is added to a local fabric and managed by the phone.
 */
class LocalMatterCommissioner : MatterCommissioner {

    /**
     * Commission a new Matter device to a local fabric.
     *
     * During the process a logic moves to the app extension.
     * The system's app extension is responsible for providing UI for scanning QR code and providing
     * the list of available Thread networks.
     * The local fabric is shared between main app and app exensions by App Groups.
     *
     * After successful commissioning, descriptor clusters for all available endpoint are read and
     * all the meta data is returned.
     */
    func startIosCommissioning(deviceId: DeviceId) async throws -> any OperationResult {
        let homes = [MatterAddDeviceRequest.Home(displayName: "Nordic Home")]
        let topology = MatterAddDeviceRequest.Topology(ecosystemName: "Nordic Ecosystem", homes: homes)
        
        let request = MatterAddDeviceRequest(topology: topology, shouldScanNetworks: true)
        
        let storage = SharedStorage(suitName: SharedConsts.sharedStorage)
        storage.storeString(key: SharedConsts.matterEnvStorageKey, value: MatterEnv.local.rawValue)
        storage.storeNumber(key: SharedConsts.nodeIdKey, value: deviceId.nsNumber())
        
        do {
            try await request.perform()
        } catch {
            let error = error as NSError
            return OperationResultError(t: CommissioningException(
                deviceId: deviceId,
                stage: Stage.commissioning,
                errorCode: KotlinInt(int: Int32(error.code)),
                displayMessage: error.localizedDescription,
                fabricId: 1
            ))
        }
        
        let descriptorCluster = try LocalMatterClusterDiscovery(nodeId: deviceId.nsNumber())
        
        do {
            let device = try await descriptorCluster.discoverClusters()
            return OperationResultSuccess(data: device)
        } catch {
            let error = error as NSError
            return OperationResultError(t: CommissioningException(
                deviceId: deviceId,
                stage: descriptorCluster.stage,
                errorCode: KotlinInt(int: Int32(error.code)),
                displayMessage: error.localizedDescription,
                fabricId: 1
            ))
        }
    }
}

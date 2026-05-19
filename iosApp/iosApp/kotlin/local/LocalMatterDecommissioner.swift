//
//  MatterDecommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 11/03/2026.
//

import ComposeApp
import SharedCode

/**
 * A helper class for decommissioning a device.
 * It is removed from local fabric after that.
 */
class LocalMatterDecommissioner : MatterDecommissioner {
    
    /**
     * Decommission a device and remove it from a local fabric.
     */
    func decommission(deviceId: DeviceId) async {
        let controller = try! LocalControllerProvider(logTag: "LocalControllerProvider").getController()

        controller.forgetDevice(withNodeID: deviceId.nsNumber())
    }
}

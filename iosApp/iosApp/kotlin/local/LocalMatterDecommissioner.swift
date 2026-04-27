//
//  MatterDecommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 11/03/2026.
//

import ComposeApp
import SharedCode

class LocalMatterDecommissioner : MatterDecommissioner {
    
    func decommission(deviceId: DeviceId) async {
        let controller = try! LocalControllerProvider(logTag: "LocalControllerProvider").getController()

        controller.forgetDevice(withNodeID: deviceId.nsNumber())
    }
}

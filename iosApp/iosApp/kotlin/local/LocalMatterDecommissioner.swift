//
//  MatterDecommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 11/03/2026.
//

import ComposeApp
import SharedCode

class LocalMatterDecommissioner : MatterDecommissioner {
    
    func decommission(nodeId: Int64) async {
        let controller = try! LocalControllerProvider(logTag: "LocalControllerProvider").getController()!

        controller.forgetDevice(withNodeID: nodeId as NSNumber)
    }
}

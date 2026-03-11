//
//  MatterDecommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 11/03/2026.
//

import ComposeApp

class MatterDecommissionerImpl : MatterDecommissioner {
    
    func decommission(nodeId: Int64) {
        let controller = MatterControllerProviderImpl().getController()!

        controller.forgetDevice(withNodeID: nodeId as NSNumber)
    }
}

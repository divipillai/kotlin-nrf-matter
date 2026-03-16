//
//  HomeKitDecommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 16/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import HomeKit

class HomeKitDecommissioner : MatterDecommissioner {
    
    func decommission(nodeId: Int64) async {
        let controller = HomeKitController.shared()
        await controller.removeAccessory(nodeId: nodeId)
    }
}

//
//  LocalMatterOutletController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 25/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import OSLog

class LocalMatterOutletController : MatterOutletController {
    
    func handleOutlet(deviceId: DeviceId, isSwitchOn: Bool, endpoint: Int32) async throws {
        let controller = LocalMatterOnOffController()
        // TODO: not implemented yet
    }
}

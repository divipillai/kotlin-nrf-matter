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

/**
 * A helper class from controlling a on/off switch devices..
 */
class LocalMatterOutletController : MatterOutletController {
    
    /**
     * Set the state of a switch.
     */
    func handleOutlet(deviceId: DeviceId, isSwitchOn: Bool, endpoint: Int32) async throws {
        let controller = LocalMatterOnOffController()
        // TODO: not implemented yet
    }
}

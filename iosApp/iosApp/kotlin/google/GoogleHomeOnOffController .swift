//
//  GoogleHomeOnOffController .swift
//  iosApp
//
//  Created by Sylwester Zielinski on 16/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import OSLog

class GoogleHomeOnOffController : MatterOnOffController {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "GoogleHomeOnOffController")

    func setDeviceOnOff(deviceId: DeviceId, isDeviceOnline: Bool, isOn: Bool, endpoint: Int32) async throws {

    }
}

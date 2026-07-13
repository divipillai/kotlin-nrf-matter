//
//  SwiftCodeProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import Matter
import ComposeApp
import SharedCode

/// Native iOS implementation of the Kotlin `SwiftCodeProvider` protocol.
///
/// Supplies the shared Compose Multiplatform code with concrete, platform-specific
/// implementations of the Matter controllers, built on Apple's Matter framework.
@MainActor
class SwiftCodeProviderImpl : @MainActor SwiftCodeProvider {

    /// - Returns: The native Matter commissioner used to commission new devices.
    func getMatterCommissioner() -> any MatterCommissioner {
        return LocalMatterCommissioner()
    }

    /// - Returns: The native Matter controller used to control on/off (light) devices.
    func getMatterOnOffController() -> any MatterLightController {
        return LocalMatterLightController()
    }

    /// - Returns: The native Matter controller used to decommission devices.
    func getDecommissioner() -> any MatterDecommissioner {
        return LocalMatterDecommissioner()
    }

    /// - Returns: The native Matter binder used to manage device bindings.
    func getMatterBinder() -> any MatterBinder {
        return LocalMatterBinder()
    }

    /// - Returns: The native Matter controller used to control door lock devices.
    func getMatterDoorController() -> any MatterDoorController {
        return LocalMatterDoorController()
    }

    /// - Returns: The native Matter controller used to read/write manufacturer-specific custom cluster data.
    func getMatterManufacturerCustomDataController() -> any MatterManufacturerCustomDataController {
        return LocalMatterCustomClusterController()
    }

    /// - Returns: The native Matter controller used to invoke custom cluster extension commands.
    func getMatterClusterExtensionController() -> any MatterClusterExtensionController {
        return LocalMatterClusterExtController()
    }

    /// - Returns: The native logger implementation used to bridge logs to the shared Kotlin logger.
    func getLogger() -> IOSLogger {
        return IOSLoggerImpl()
    }
}

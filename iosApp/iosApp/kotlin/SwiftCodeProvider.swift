//
//  SwiftCodeProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import Matter
import ComposeApp
import SharedCode

@MainActor
class SwiftCodeProviderImpl : @MainActor SwiftCodeProvider {
    
    func getMatterCommissioner() -> any MatterCommissioner {
        return LocalMatterCommissioner()
    }
    
    func getMatterOnOffController() -> any MatterLightController {
        return LocalMatterLightController()
    }
    
    func getDecommissioner() -> any MatterDecommissioner {
        return LocalMatterDecommissioner()
    }
    
    func getMatterBinder() -> any MatterBinder {
        return LocalMatterBinder()
    }
    
    func getMatterDoorController() -> any MatterDoorController {
        return LocalMatterDoorController()
    }
    
    func getMatterManufacturerCustomDataController() -> any MatterManufacturerCustomDataController {
        return LocalMatterCustomClusterController()
    }
    
    func getMatterClusterExtensionController() -> any MatterClusterExtensionController {
        return LocalMatterClusterExtController()
    }
    
    func getLogger() -> IOSLogger {
        return IOSLoggerImpl()
    }
}

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
//        return HomeKitCommissioner()
//        return GoogleHomeCommissioner()
    }
    
    func getMatterOnOffController() -> any MatterLightController {
        return LocalMatterLightController()
//        return HomeKitMatterOnOffController()
//        return GoogleHomeOnOffController()
    }
    
    func getDecommissioner() -> any MatterDecommissioner {
        return LocalMatterDecommissioner()
//        return HomeKitDecommissioner()
//        return GoogleHomeDecommissioner()
    }
    
    func getMatterBinder() -> any MatterBinder {
        return LocalMatterBinder()
    }
    
    func getMatterDoorController() -> any MatterDoorController {
        return LocalMatterDoorController()
    }
    
    func getMatterManufacturerCustomDataController() -> any MatterManufacturerCustomDataController {
        return LocalMatterCustomClusterController()
//        return GoogleHomeCustomClusterController()
    }
    
    func getMatterClusterExtensionController() -> any MatterClusterExtensionController {
        return LocalMatterClusterExtController()
    }
    
    func getLogger() -> any IOSLogger {
        return IOSLoggerImpl()
    }
}

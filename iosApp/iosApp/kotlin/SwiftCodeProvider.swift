//
//  SwiftCodeProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import Matter
import ComposeApp
import SharedCode

class SwiftCodeProviderImpl : SwiftCodeProvider {
    func getMatterCommissioner() -> any MatterCommissioner {
        return LocalMatterCommissioner()
//        return GoogleHomeCommissioner()
    }
    
    func getMatterOnOffController() -> any MatterOnOffController {
        return LocalMatterOnOffController()
//        return HomeKitMatterOnOffController()
    }
    
    func getDecommissioner() -> any MatterDecommissioner {
        return LocalMatterDecommissioner()
//        return HomeKitDecommissioner()
    }
}

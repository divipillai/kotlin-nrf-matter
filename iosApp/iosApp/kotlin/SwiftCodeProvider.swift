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
        return HomeKitCommissioner()
    }
    
    func getMatterOnOffController() -> any MatterOnOffController {
        return LocalMatterOnOffController()
    }
    
    func getDecommissioner() -> any MatterDecommissioner {
        return LocalMatterDecommissioner()
    }
}

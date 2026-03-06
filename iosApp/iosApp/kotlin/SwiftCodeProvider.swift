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
    func getMatterOnOffController() -> any MatterOnOffController {
        return MatterOnOffControllerImpl()
    }
    
    func getMatterControllerProvider() -> any MatterControllerProvider {
        return MatterControllerProviderImpl()
    }
    
    func getMatterSupport() -> any MatterSupportKt {
        return MatterSupportForKotlin()
    }

    func getThreadNetworkProvider() -> any ThreadNetworkProvider {
        return ThreadNetworkProviderImpl()
    }
    
    func getKeypair() -> any MTRKeypair {
        return MatterKeypair()
    }
}

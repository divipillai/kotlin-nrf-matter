//
//  SwiftCodeProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import ComposeApp

class SwiftCodeProviderImpl : SwiftCodeProvider {
    
    func getMatterSupport() -> any MatterSupportKt {
        return MatterSupportForKotlin()
    }
    
    
    func getThreadNetworkProvider() -> any ThreadNetworkProvider {
        return ThreadNetworkProviderImpl()
    }
}

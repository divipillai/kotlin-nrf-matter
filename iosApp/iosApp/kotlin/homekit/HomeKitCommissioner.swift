//
//  HomeKitCommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 12/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import HomeKit

class HomeKitCommissioner : MatterCommissioner {

    func startIosCommissioning(onError: @escaping () -> Void) async throws -> Device? {
        print("AAATESTAAA - aaa")
        let accessoryManager = HMAccessorySetupManager()
        print("AAATESTAAA - bbb")
        let request = HMAccessorySetupRequest()
        print("AAATESTAAA - ccc")
        let result = try! await accessoryManager.performAccessorySetup(using: request)
        
        print("AAATESTAAA - result: \(result)")
        return nil
    }
}

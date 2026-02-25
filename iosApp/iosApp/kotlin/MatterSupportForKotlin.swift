//
//  MyMatterSupport.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/02/2026.
//

import ComposeApp
import Matter
import MatterSupport
import os.log
import nrfMatter

class MatterSupportForKotlin : MatterSupportKt {
    
    func startIosCommissioning(code: String, onError: @escaping () -> Void) async throws -> Device? {
        commission(payload: code)
        return nil
    }
    
    
    func commission(payload: String) {
        print("AAATESTAAA - commission device: \(payload)")
        let homes = [MatterAddDeviceRequest.Home(displayName: "My Home")]
        let topology = MatterAddDeviceRequest.Topology(ecosystemName: "MyEcosystemName", homes: homes)
        
        var request = MatterAddDeviceRequest(topology: topology)

//        request.setupPayload = MTRSetupPayload(payload: payload)
        
        Task {
            do {
                print("AAATESTAAA - Start a request")
                try await request.perform()
                print("Successfully set up device!")
                
                
                // Handle the success full setup request and update your app's UI, register the device in your database, or set up any default automations.
            } catch {
                // Handle other errors.
                print("Failed to set up device with error: \(error.localizedDescription).")
            }
        }

    }
}


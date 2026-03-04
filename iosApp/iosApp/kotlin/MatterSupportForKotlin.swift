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
        return await commission(payload: code)
    }
    
    func commission(payload: String) async -> Device? {
        print("AAATESTAAA - commission device: \(payload)")
        
        let key = MatterKeypair()
        let homes = [MatterAddDeviceRequest.Home(displayName: "My Home")]
        let topology = MatterAddDeviceRequest.Topology(ecosystemName: "MyEcosystemName", homes: homes)
        
        var request = MatterAddDeviceRequest(topology: topology, shouldScanNetworks: true)

//        request.setupPayload = MTRSetupPayload(payload: payload)
        
        do {
            print("AAATESTAAA - Start a request")
            try await request.perform()
            print("AAATESTAAA - Successfully set up device!")
            
            let nodeID: NSNumber = 1 // todo
            
            let provider = MatterControllerProvider()
            
            print("AAATESTAAA - Create controller.")
            guard let controller = try? provider.getController() else { return nil }
            
            print("AAATESTAAA - Creating device")
            
            let device = MTRDevice(nodeID: nodeID, controller: controller)
            
            print("AAATESTAAA - storing device")
//            MatterDevicesProvider.shared.saveDevice(device: device)
            
            let result = Device(
                dateCommissioned: nil,
                vendorId: device.vendorID?.stringValue,
                productId: device.productID?.stringValue,
                deviceType: DeviceType.lightOnOff,
                deviceId: nodeID.int64Value,
                name: "Matter device",
                productName: "nRF54",
                vendorName: "Nordic Semiconductor",
                deviceMatterInfo: []
            )
            
            print("AAATESTAAA - Returning a device!")
            
            return result
            
            // Handle the success full setup request and update your app's UI, register the device in your database, or set up any default automations.
        } catch {
            // Handle other errors.
            print("Failed to set up device with error: \(error.localizedDescription).")
        }
        return nil
    }
}


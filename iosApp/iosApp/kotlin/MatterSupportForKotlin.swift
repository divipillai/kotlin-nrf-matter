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
import SharedCode

class MatterSupportForKotlin : MatterSupportKt {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "DeviceSetup")
    
    func startIosCommissioning(onError: @escaping () -> Void) async throws -> Device? {
        return await commission()
    }
    
    func commission() async -> Device? {
        let homes = [MatterAddDeviceRequest.Home(displayName: "My Home")]
        let topology = MatterAddDeviceRequest.Topology(ecosystemName: "MyEcosystemName", homes: homes)
        
        var request = MatterAddDeviceRequest(topology: topology, shouldScanNetworks: true)
        
        let sharedStorage = MatterStorage()
        sharedStorage.setKey("Hello".data(using: .utf8)!, forKey: "Hello")

//        request.setupPayload = MTRSetupPayload(payload: payload)
        
        do {
            logger.info("EEETESTEEE - Start a request")
            try await request.perform()
            logger.info("EEETESTEEE - Successfully set up device!")
            
            let nodeID: NSNumber = 1 // todo
            
//            let provider = MatterControllerProvider(logTag: "EEETESTEEE")
//            
//            logger.info("EEETESTEEE - Create controller.")
//            guard let controller = try? provider.getController() else { return nil }
//            
//            logger.info("EEETESTEEE - Creating device")
//            
//            let device = MTRDevice(nodeID: nodeID, controller: controller)
//            
            logger.info("EEETESTEEE - storing device")
//            MatterDevicesProvider.shared.saveDevice(device: device)
            
            let result = Device(
                dateCommissioned: nil,
                vendorId: "TODO",
                productId: "TODO",
                deviceType: DeviceType.lightOnOff,
                deviceId: nodeID.int64Value,
                name: "Matter device",
                productName: "nRF54",
                vendorName: "Nordic Semiconductor",
                deviceMatterInfo: []
            )
            
            logger.info("EEETESTEEE - Returning a device!")
            
            return result
            
            // Handle the success full setup request and update your app's UI, register the device in your database, or set up any default automations.
        } catch {
            // Handle other errors.
            logger.info("Failed to set up device with error: \(error.localizedDescription).")
        }
        return nil
    }
}


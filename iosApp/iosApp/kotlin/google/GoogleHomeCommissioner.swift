//
//  GoogleCommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 12/03/2026.
//

import ComposeApp
import Matter
import MatterSupport
import os.log
import nrfMatter
import SharedCode
import GoogleHomeSDK
import GoogleHomeTypes

enum PairingError: Error {
    case test
}

class GoogleHomeCommissioner : MatterCommissioner {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "GoogleHomeCommissioner")
    
    func startIosCommissioning(onError: @escaping () -> Void) async throws -> Device? {
        return await commission()
    }
    
    func commission() async -> Device? {
        let controller = GoogleHomeController.instance()
        await controller.initialize() //todo
        let structure = await controller.getStructure()
        
        do {
            print("AAATESTAAA - structure not null")
            
            let topology = MatterAddDeviceRequest.Topology(
              ecosystemName: "Google Home",
              homes: [MatterAddDeviceRequest.Home(displayName: structure.name)]
            )
            
            let request = MatterAddDeviceRequest(topology: topology)
            
            print("AAATESTAAA - structure.prepareForMatterCommissioning()")
            try await structure.prepareForMatterCommissioning()
            
            let storage = SharedStorage()
            storage.storeString(key: SharedConsts.matterEnvStorageKey, value: MatterEnv.google.rawValue)
            
            print("AAATESTAAA - structure.perform()")
            
            try await request.perform()
            
            print("AAATESTAAA - structure.completeMatterCommissioning()")
            
            guard let commissionedDeviceID = (try await structure.completeMatterCommissioning()).first else {
                print("AAATESTAAA - commissionedDeviceID is nil")
                return nil
            }
            
            print("AAATESTAAA - id: \(commissionedDeviceID)")
            print("AAATESTAAA - structure.devices()")
            
            guard let device = await controller.getDevice(id: commissionedDeviceID) else {
                print("AAATESTAAA - device is nil")
                return nil
            }
            
            print("AAATESTAAA - parts")
            
            let rootDevice = await device.parts.get(RootNodeDeviceType.self)
            if let basicInformationTrait = rootDevice?.traits[Matter.BasicInformationTrait.self] {
                let vendorName = basicInformationTrait.attributes.vendorName
                let productName = basicInformationTrait.attributes.productName
                let productID = basicInformationTrait.attributes.productID
                let vendorID = basicInformationTrait.attributes.vendorID
                let softwareVersionString = basicInformationTrait.attributes.softwareVersionString
                
                return Device(
                    deviceId: DeviceId(value: commissionedDeviceID),
                    dateCommissioned: KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000)),
                    vendorId: vendorID != nil ? String(vendorID!) : "unknown",
                    productId: productID != nil ? String(productID!) : "unknown",
                    deviceType: .lightOnOff,
                    name: device.name,
                    productName: productName,
                    vendorName: vendorName,
                    deviceMatterInfo: [] //todo
                )
            }
        } catch {
            print("AAATESTAAA - error: \(error)")
            let result = structure.markMatterCommissioningFailed(error: error)
            Logger().error("Failed to complete MatterAddDeviceRequest: \(result.detailedError).")
        }
        return nil
    }
}

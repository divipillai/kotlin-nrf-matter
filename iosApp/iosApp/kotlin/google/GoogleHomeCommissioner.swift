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

class GoogleHomeCommissioner : MatterCommissioner {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "GoogleHomeCommissioner")
    
    func startIosCommissioning(onError: @escaping () -> Void) async throws -> Device? {
        return await commission()
    }
    
    func commission() async -> Device? {
        var structure: Structure? = nil
        var home: Home? = nil
        
        do {
            print("AAATESTAAA - Home.connect()")
            
            home = try await Home.connect()
            
            print("AAATESTAAA - home.structures()")
            
            let allStructuresChanges = home!.structures()
            let allStructures = try await allStructuresChanges.list()
            structure = allStructures.first
        } catch {
            print("AAATESTAAA - error")
        }
        
        guard let structure, let home else {
            print("AAATESTAAA - structures are null")
            return nil
        }
        
        do {
            print("AAATESTAAA - structure not null")
            
            let topology = MatterAddDeviceRequest.Topology(
              ecosystemName: "Google Home",
              homes: [MatterAddDeviceRequest.Home(displayName: structure.name)]
            )
            
            let request = MatterAddDeviceRequest(topology: topology, shouldScanNetworks: true)
            
            print("AAATESTAAA - structure.prepareForMatterCommissioning()")
            
            try await structure.prepareForMatterCommissioning()
            
            let storage = MatterStorage()
            storage.storeString(key: SharedConsts.matterEnvStorageKey, value: MatterEnv.google.rawValue)
            
            print("AAATESTAAA - structure.perform()")
            
            try await request.perform()
            
            print("AAATESTAAA - structure.completeMatterCommissioning()")
            
            guard let commissionedDeviceID = (try await structure.completeMatterCommissioning()).first else {
                return nil
            }
            
            print("AAATESTAAA - id: \(commissionedDeviceID)")
            print("AAATESTAAA - structure.devices()")
            
            guard let device = try await home.devices().list().first(where: { $0.id == commissionedDeviceID }) else {
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
                    dateCommissioned: KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000)),
                    vendorId: vendorID != nil ? String(vendorID!) : "unknown",
                    productId: productID != nil ? String(productID!) : "unknown",
                    deviceType: .lightOnOff,
                    deviceId: Int64(commissionedDeviceID)!,
                    name: device.name,
                    productName: productName,
                    vendorName: vendorName,
                    deviceMatterInfo: [] //todo
                )
            }
        } catch {
            print("AAATESTAAA - error")
            let result = structure.markMatterCommissioningFailed(error: error)
            Logger().error("Failed to complete MatterAddDeviceRequest: \(result.detailedError).")
        }
        return nil
    }
}

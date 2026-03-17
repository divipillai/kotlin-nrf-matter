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
        let home = try? await Home.connect()
        
        guard let home else { return nil }
        
        let allStructuresChanges = home.structures()
        let allStructures = (try? await allStructuresChanges.list()) ?? []
        let structure = allStructures.first

        guard let structure else { return nil }
        
        let homes = [MatterAddDeviceRequest.Home(displayName: "Nordic Home")]
        let topology = MatterAddDeviceRequest.Topology(ecosystemName: "Nordic Ecosystem", homes: homes)
        
        let request = MatterAddDeviceRequest(topology: topology, shouldScanNetworks: true)
        
        do {
            try await structure.prepareForMatterCommissioning()
            
            let storage = MatterStorage()
            storage.storeString(key: SharedConsts.matterEnvStorageKey, value: MatterEnv.google.rawValue)
            
            try await request.perform()
            
            guard let commissionedDeviceID = (try await structure.completeMatterCommissioning()).first else {
                return nil
            }
            
            print("AAATESTAAA - id: \(commissionedDeviceID)")
            
            guard let device = try await home.devices().list().first(where: { $0.id == commissionedDeviceID }) else {
                return nil
            }
            
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
            let result = structure.markMatterCommissioningFailed(error: error)
            Logger().error("Failed to complete MatterAddDeviceRequest: \(result.detailedError).")
        }
        return nil
    }
}

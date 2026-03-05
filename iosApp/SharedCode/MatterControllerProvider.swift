//
//  MatterControllerProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 05/03/2026.
//

import Matter
import os.log

public class MatterControllerProvider {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "DeviceSetup")
    private let logTag: String
    private let factory = MTRDeviceControllerFactory.sharedInstance()
    
    public init(logTag: String) {
        self.logTag = logTag
    }
    
    public func release() {
        factory.stop()
    }
    
    public func getController() throws -> MTRDeviceController? {
        let nodeID: NSNumber = 1 // todo

        let storage = MatterStorage()
        let factoryParams = MTRDeviceControllerFactoryParams(storage: storage)
        
        try factory.start(factoryParams)
        
        logger.debug("\(self.logTag) - known fabrics: \(factory.knownFabrics?.count ?? 0)")
        factory.knownFabrics?.forEach { fabric in
            logger.debug("\(self.logTag) - fabric id: \(fabric.fabricID)")
        }
        
        guard let ipk = loadOrCreateIPK(storage: storage) else {
            return nil
        }
        
        let params = MTRDeviceControllerStartupParams(
            ipk: ipk as Data,
            fabricID: 1,
            nocSigner: MatterKeypair(),
        )
        params.vendorID = 0xFFF1
        
        var controller: MTRDeviceController? = nil
        do {
            logger.debug("\(self.logTag) - Controller from existing fabric")
            controller = try factory.createController(onExistingFabric: params)
        } catch {
            do {
                logger.debug("\(self.logTag) - Controller from new fabric")
                controller = try factory.createController(onNewFabric: params)
            } catch {
                return nil
            }
        }
        return controller
    }
    
    func loadOrCreateIPK(storage: MatterStorage) -> Data? {
        if let storedIpk = storage.storageData(forKey: "MatterIPK") {
            return storedIpk as Data
        }

        guard let ipkMutable = NSMutableData(length: 16) else {
            logger.debug("\(self.logTag) Coulnd't create NSMutableData dla IPK")
            return nil
        }

        let status = SecRandomCopyBytes(kSecRandomDefault, ipkMutable.length, ipkMutable.mutableBytes)
        guard status == errSecSuccess else {
            logger.debug("\(self.logTag) Error during generating IPK: \(status)")
            return nil
        }

        _ = storage.setStorageData(ipkMutable as Data, forKey: "MatterIPK")

        return ipkMutable as Data
    }
}

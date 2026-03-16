//
//  MatterControllerProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 05/03/2026.
//

import Matter
import os.log

public class LocalControllerProvider {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "MatterControllerProviderCore")
    private let logTag: String
    private let factory = MTRDeviceControllerFactory.sharedInstance()
    
    private static var controller: MTRDeviceController? = nil
    
    public init(logTag: String) {
        self.logTag = logTag
    }
    
    public func release() {
        factory.stop()
    }
    
    public func getController() throws -> MTRDeviceController? {
        if (Self.controller != nil && Self.controller?.isRunning == true) {
            return Self.controller
        }

        let storage = MatterStorage()
        let factoryParams = MTRDeviceControllerFactoryParams(storage: storage)
        
        if (!factory.isRunning) {
            try factory.start(factoryParams)
        }
        
        guard let ipk = loadOrCreateIPK(storage: storage) else {
            return nil
        }
        
        let params = MTRDeviceControllerStartupParams(
            ipk: ipk as Data,
            fabricID: 1, // todo
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
        
        Self.controller = controller
        return controller
    }
    
    private func loadOrCreateIPK(storage: MatterStorage) -> Data? {
        if let storedIpk = storage.getKey(forKey: "MatterIPK") {
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

        _ = storage.setKey(ipkMutable as Data, forKey: "MatterIPK")

        return ipkMutable as Data
    }
}

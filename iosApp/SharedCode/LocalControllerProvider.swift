//
//  MatterControllerProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 05/03/2026.
//

import Matter
import os.log

enum ControllerError : Error {
    case initializationError
}

/**
 * A helper class for managing access to ``MTRDeviceController``.
 * The controller is shared between the app extension and the main app.
 *
 * A local fabric is identify by fabric id, ipk and private/public keys used in noc signer.
 * If those data don't match then the controller may return a fabric with no devices even if the
 * fabric id matches with the previously used one.
 *
 * The controller takes care so the only one active controller is in use at the same time.
 * If an instance of a controller already exists then it is returned from the cache value.
 * If a new instance needs to be created then firstly it tries to create a controller on a fabric assuming that
 * the fabric already exists. If ipk, fabric id and noc signer matches then the new instance of the controller
 * will be created and stored locally in cached field for later used.
 * If creation of the existing fabric fails then the controller on a new fabric will be created.
 * It can happen even when fabric id was previously used for creating a fabric but ipk and noc signer doesn't match.
 */
public class LocalControllerProvider {

    private let logTag: String
    private let factory = MTRDeviceControllerFactory.sharedInstance()
    
    private static var controller: MTRDeviceController? = nil
    
    public init(logTag: String) {
        self.logTag = logTag
    }
    
    /**
     * Releases ``MTRDeviceControllerFactory`` and all created by it ``MTRDeviceController``.
     * After that ``MTRDeviceController`` cannot be used.
     */
    public func release() {
        factory.stop()
    }
    
    public func getController() throws -> MTRDeviceController {
        if let controller = Self.controller, controller.isRunning {
            return controller
        }

        let storage = SharedStorage(suitName: SharedConsts.localStorage)
        let factoryParams = MTRDeviceControllerFactoryParams(storage: storage)
        
        if (!factory.isRunning) {
            try factory.start(factoryParams)
        }
        
        guard let ipk = loadOrCreateIPK(storage: storage) else {
            throw ControllerError.initializationError
        }
        
        let params = MTRDeviceControllerStartupParams(
            ipk: ipk as Data,
            fabricID: 1, // todo
            nocSigner: MatterKeypair(),
        )
        params.vendorID = 0xFFF1
        
        let controller: MTRDeviceController

        do {
            SharedLogger.debug("\(self.logTag) - Controller from existing fabric")
            controller = try factory.createController(onExistingFabric: params)
        } catch {
            SharedLogger.debug("\(self.logTag) - Controller from new fabric")
            controller = try factory.createController(onNewFabric: params)
        }

        Self.controller = controller
        return controller
    }
    
    /**
     * Loads or creates IPK.
     * IPK needs to be uniqu per fabric.
     * It is used for CASE (Certificate Authenticated Session Establishment).
     */
    private func loadOrCreateIPK(storage: SharedStorage) -> Data? {
        if let storedIpk = storage.getKey(forKey: "MatterIPK") {
            return storedIpk as Data
        }

        guard let ipkMutable = NSMutableData(length: 16) else {
            SharedLogger.debug("\(self.logTag) Coulnd't create NSMutableData dla IPK")
            return nil
        }

        let status = SecRandomCopyBytes(kSecRandomDefault, ipkMutable.length, ipkMutable.mutableBytes)
        guard status == errSecSuccess else {
            SharedLogger.debug("\(self.logTag) Error during generating IPK: \(status)")
            return nil
        }

        _ = storage.setKey(ipkMutable as Data, forKey: "MatterIPK")

        return ipkMutable as Data
    }
}

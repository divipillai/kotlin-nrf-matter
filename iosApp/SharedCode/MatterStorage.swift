//
//  MatterStorage.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 05/03/2026.
//

import Matter
import os.log

public class MatterStorage : NSObject, MTRStorage {
    
    private let defaults = UserDefaults(suiteName: "group.nordicsemi.nrf.matter")!
    private let logger = Logger(subsystem: "nrf.matter", category: "DeviceSetup")
    
    public func storageData(forKey key: String) -> Data? {
        return defaults.data(forKey: key)
    }


    public func setStorageData(_ value: Data, forKey key: String) -> Bool {
        defaults.setValue(value, forKey: key)
        return true
    }

    public func removeStorageData(forKey key: String) -> Bool {
        defaults.removeObject(forKey: key)
        return true
    }
    
    public func getKey(forKey key: String) -> Data? {
        logger.info("AAATESTAAA - get data \(key, privacy: .public)")
        let object = defaults.object(forKey: key)
        if object != nil {
            logger.info("AAATESTAAA - object found")
        } else {
            logger.info("AAATESTAAA - object not found")
        }
        let data = defaults.data(forKey: key)
        if data != nil {
            let output = data!.map { String(format: "%02x", $0) }.joined()
            logger.info("AAATESTAAA - get data: \(output, privacy: .public) ")
        } else {
            logger.info("AAATESTAAA - get data: null")
        }
    
        return defaults.data(forKey: key)
    }
    
    
    public func setKey(_ value: Data, forKey key: String) -> Bool {
        logger.info("AAATESTAAA - setStorageData \(key, privacy: .public)")
        defaults.setValue(value, forKey: key)
        return true
    }
}

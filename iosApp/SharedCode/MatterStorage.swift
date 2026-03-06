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
    private let logger = Logger(subsystem: "nrf.matter", category: "MatterStorage")
    
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
        return defaults.data(forKey: key)
    }
    
    
    public func setKey(_ value: Data, forKey key: String) -> Bool {
        defaults.setValue(value, forKey: key)
        return true
    }
}

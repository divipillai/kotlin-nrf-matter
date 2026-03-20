//
//  SharedStorage.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 20/03/2026.
//

import Matter
import os.log

public class SharedStorage : NSObject, MTRStorage {
    
    private let defaults = UserDefaults(suiteName: SharedConsts.sharedGroup)!
    private let logger = Logger(subsystem: "nrf.matter", category: "MatterStorage")
    
    public func storeString(key: String, value: String) {
        defaults.set(key, forKey: key)
    }
    
    public func getString(key: String) -> String? {
        defaults.string(forKey: key)
    }
    
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

//
//  SharedStorage.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 20/03/2026.
//

import Matter

public class SharedStorage : NSObject, MTRStorage {
    
    private let defaults: UserDefaults
    
    public init(suitName: String) {
        defaults = UserDefaults(suiteName: suitName)!
    }
    
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

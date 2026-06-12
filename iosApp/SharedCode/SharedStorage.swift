//
//  SharedStorage.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 20/03/2026.
//

import Matter

/**
 * A Matter storage class required by FW for storing fabric (for local fabric) and
 * all related data.
 * It uses ``UserDefaults`` under the hood and app groups for sharing
 * data between the main app and the extension.
 */
public class SharedStorage : NSObject, MTRStorage {
    
    private let defaults: UserDefaults
    
    public init(suitName: String) {
        defaults = UserDefaults(suiteName: suitName)!
    }
    
    public func storeString(key: String, value: String) {
        defaults.set(value, forKey: key)
    }
    
    public func getString(key: String) -> String? {
        defaults.string(forKey: key)
    }
    
    public func storeNumber(key: String, value: NSNumber) {
        defaults.set(value, forKey: key)
    }
    
    public func getNumber(key: String) -> NSNumber? {
        defaults.object(forKey: key) as? NSNumber
    }
    
    public func storeBool(key: String, value: Bool) {
        defaults.set(value, forKey: key)
    }
    
    public func getBool(key: String) -> Bool? {
        defaults.bool(forKey: key)
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

//
//  MatterStorage.swift
//  ios-matter
//
//  Created by Sylwester Zielinski on 04/08/2026.
//

import Matter

public final class MatterStorage : NSObject, MTRStorage {

    private let defaults: UserDefaults = UserDefaults(suiteName: SharedConsts.localStorage)!
    
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
    
    /// Returns the raw data stored for the given key, if any.
    @objc public func getKey(forKey key: String) -> Data? {
        return defaults.data(forKey: key)
    }

    /// Stores raw data for the given key.
    ///
    /// - Returns: `true` once the data has been stored.
    @objc public func setKey(_ value: Data, forKey key: String) -> Bool {
        defaults.setValue(value, forKey: key)
        return true
    }
}

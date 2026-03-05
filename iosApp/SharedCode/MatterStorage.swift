//
//  MatterStorage.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 05/03/2026.
//

import Matter

public class MatterStorage : NSObject, MTRStorage {
    
    private let defaults = UserDefaults(suiteName: "group.P3R8YQEV4L.nordicsemi.nrf.matter")!
    
    public func storageData(forKey key: String) -> Data? {
        return defaults.data(forKey: key)
    }


    public func setStorageData(_ value: Data, forKey key: String) -> Bool {
        defaults.setValue(value, forKey: key)
        return defaults.synchronize()
    }


    public func removeStorageData(forKey key: String) -> Bool {
        defaults.removeObject(forKey: key)
        return defaults.synchronize()
    }
}

//
//  MatterStorage.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 26/02/2026.
//

import Matter

class MatterStorage : NSObject, MTRStorage {
    
    private let defaults = UserDefaults.standard
    
    func storageData(forKey key: String) -> Data? {
        return defaults.data(forKey: key)
    }


    func setStorageData(_ value: Data, forKey key: String) -> Bool {
        defaults.setValue(value, forKey: key)
        return defaults.synchronize()
    }


    func removeStorageData(forKey key: String) -> Bool {
        defaults.removeObject(forKey: key)
        return defaults.synchronize()
    }
}

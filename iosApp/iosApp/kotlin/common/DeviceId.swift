//
//  DeviceId.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/03/2026.
//

import ComposeApp

extension DeviceId {
    
    func uInt64() -> UInt64 {
        UInt64(self.stringValue)!
    }
    
    func nsNumber() -> NSNumber {
        NSNumber(value: self.longValue)
    }
}

//
//  DeviceId.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/03/2026.
//

import ComposeApp

/// Conversion helpers for interop with native Matter APIs that expect Foundation
/// numeric types rather than the Kotlin `DeviceId` type.
extension DeviceId {

    /// - Returns: The device ID parsed from its string representation as a `UInt64`.
    func uInt64() -> UInt64 {
        UInt64(self.stringValue)!
    }

    /// - Returns: The device ID as an `NSNumber`, for use with Objective-C-based APIs.
    func nsNumber() -> NSNumber {
        NSNumber(value: self.longValue)
    }
}

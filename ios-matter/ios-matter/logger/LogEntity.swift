//
//  LogEntity.swift
//  ios-matter
//
//  Created by Sylwester Zielinski on 28/07/2026.
//

import Foundation

/// A single log entry, as handed to Kotlin.
///
/// Declared as an `@objc` class rather than a struct so it can cross the Objective-C bridge into
/// Kotlin/Native, where `IOSLoggerImpl` converts it to the shared `LogEntity` model.
@objc public final class LogEntity: NSObject {
    /// Milliseconds since the Unix epoch.
    ///
    /// Set to the current time when the entry is constructed. Entries replayed from the store by
    /// ``SwiftLogger/logs()`` are therefore all stamped with the time of the *read*, not the time
    /// they were logged — only entries delivered live through ``SwiftLogger/callback`` carry a
    /// meaningful timestamp.
    @objc public let date: Int64
    @objc public let level: LogLevel
    /// Identifies the source of the message; carried as the Pulse store's label.
    @objc public let tag: String
    @objc public let message: String

    public init(date: Int64, level: LogLevel, tag: String, message: String) {
        self.date = date
        self.level = level
        self.tag = tag
        self.message = message
    }
}

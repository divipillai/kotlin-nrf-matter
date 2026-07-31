//
//  LogEntity.swift
//  ios-matter
//
//  Created by Sylwester Zielinski on 28/07/2026.
//

import Foundation

@objc public final class LogEntity: NSObject {
    @objc public let date: Int64
    @objc public let level: LogLevel
    @objc public let tag: String
    @objc public let message: String

    public init(date: Int64, level: LogLevel, tag: String, message: String) {
        self.date = date
        self.level = level
        self.tag = tag
        self.message = message
    }
}

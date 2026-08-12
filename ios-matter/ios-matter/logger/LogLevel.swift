//
//  LogLevel.swift
//  ios-matter
//
//  Created by Sylwester Zielinski on 28/07/2026.
//

import Foundation

/// Severity of a ``LogEntity``.
///
/// Kotlin maps these to `no.nordicsemi.nrf.matter.logger.LogLevel` by matching the generated
/// `LogLevelInfo`/`LogLevelDebug` constants in `IOSLoggerImpl`, treating anything else as an error.
@objc public enum LogLevel: Int {
    case info
    case debug
    case error
}

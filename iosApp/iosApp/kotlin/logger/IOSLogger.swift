//
//  IOSLoggerImpl.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 04/05/2026.
//

import ComposeApp
import SharedCode
import Combine

/// Native iOS implementation of the Kotlin `IOSLogger` class.
///
/// Forwards log calls to the shared `SharedLogger` and republishes its log stream
/// on `logsChannel` so native and shared code observe the same log entries.
class IOSLoggerImpl : IOSLogger {

    private var cancellables = Set<AnyCancellable>()

    /// Subscribes to `SharedLogger`'s publisher so logs emitted from shared code
    /// are also forwarded to `logsChannel`.
    override init() {
        super.init()
        SharedLogger.logPublisher
            .sink { [weak self] log in
                self?.logsChannel.trySend(element: log.message)
            }
            .store(in: &cancellables)
    }

    /// Logs an info-level message through the shared logger and `logsChannel`.
    ///
    /// - Parameters:
    ///   - tag: The subsystem or component identifier the message is associated with.
    ///   - message: The message to log.
    override func info(tag: String, message: String) {
        SharedLogger.info(tag: tag, message)
        logsChannel.trySend(element: message)
    }

    /// Logs a debug-level message through the shared logger and `logsChannel`.
    ///
    /// - Parameters:
    ///   - tag: The subsystem or component identifier the message is associated with.
    ///   - message: The message to log.
    override func debug(tag: String, message: String) {
        SharedLogger.debug(tag: tag, message)
        logsChannel.trySend(element: message)
    }

    /// Logs an error-level message through the shared logger and `logsChannel`.
    ///
    /// - Parameters:
    ///   - tag: The subsystem or component identifier the message is associated with.
    ///   - message: The message to log.
    override func error(tag: String, message: String) {
        SharedLogger.error(tag: tag, message)
        logsChannel.trySend(element: message)
    }

    /// Retrieves the persisted log history from the shared logger and delivers it asynchronously.
    ///
    /// - Parameter onReady: Called with the list of log entries once they have been loaded and mapped.
    override func getLogs(onReady: @escaping ([LogEntity]) -> Void) {
        let logs = try? SharedLogger.logs()
        
        let result: [LogEntity] = logs?.compactMap { item in
            let level: LogLevel = switch item.level {
            case Level.debug: LogLevel.debug
            case Level.info: LogLevel.info
            case Level.error: LogLevel.error
            @unknown default: LogLevel.info
            }
            
            return LogEntity(
                date: Int64(item.createdAt.timeIntervalSince1970 * 1000),
                level: level,
                tag: item.tag,
                message: item.message,
            )
        } ?? []
        
        onReady(result)
    }
}

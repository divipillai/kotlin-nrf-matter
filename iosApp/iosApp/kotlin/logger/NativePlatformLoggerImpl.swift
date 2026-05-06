//
//  NativePlatformLoggerImpl.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 04/05/2026.
//

import ComposeApp
import SharedCode

class NativePlatformLoggerImpl : NativePlatformLogger {
    
    func info(tag: String, message: String) {
        SharedLogger.info(tag: tag, message)
    }
    
    func debug(tag: String, message: String) {
        SharedLogger.debug(tag: tag, message)
    }
    
    func error(tag: String, message: String) {
        SharedLogger.error(tag: tag, message)
    }
    
    func getLogs(onReady: @escaping ([LogEntity]) -> Void) {
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

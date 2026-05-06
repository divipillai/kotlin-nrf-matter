//
//  SharedLogger.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 04/05/2026.
//

internal import Pulse
import Foundation

public enum Level {
    case debug
    case info
    case error
}

public struct LogItem {
    public let createdAt: Date
    public let level: Level
    public let tag: String
    public let message: String
}

public class SharedLogger {
    
    private static let store: LoggerStore = {
        let containerURL = FileManager.default.containerURL(
            forSecurityApplicationGroupIdentifier: SharedConsts.sharedStorage
        )!
        let url = containerURL.appendingPathComponent("pulse.sqlite")
        return try! LoggerStore(storeURL: url)
    }()

    public static func logs() throws -> [LogItem] {
        let result = try store.messages(sortDescriptors: [SortDescriptor(\.createdAt, order: .reverse)])
        
        return result.map { item in
            let level: Level = switch item.level {
            case LoggerStore.Level.debug.rawValue: Level.debug
            case LoggerStore.Level.info.rawValue: Level.info
            case LoggerStore.Level.error.rawValue: Level.error
            default: Level.debug
            }
            
            return LogItem(
                createdAt: item.createdAt,
                level: level,
                tag: item.label,
                message: item.text,
            )
        }
    }
    
    public static func debug(tag: String = "nRF Matter", _ message: String) {
        store.storeMessage(
            label: tag,
            level: .debug,
            message: message,
        )
    }
    
    public static func info(tag: String = "nRF Matter", _ message: String) {
        store.storeMessage(
            label: tag,
            level: .info,
            message: message,
        )
    }
    
    public static func error(tag: String = "nRF Matter", _ message: String) {
        store.storeMessage(
            label: tag,
            level: .error,
            message: message,
        )
    }
}

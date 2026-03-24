//
//  GoogleHomeProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 24/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import OSLog
import GoogleHomeSDK

class GoogleHomeController {

    nonisolated(unsafe) private static var controller: GoogleHomeController? = nil
    
    private var structure: Structure? = nil
    private var home: Home? = nil
    
    public static func instance() -> GoogleHomeController {
        if let controller = Self.controller {
            return controller
        } else {
            let controller = GoogleHomeController()
            Self.controller = controller
            return controller
        }
    }
    
    func initialize() async {
        do {
            print("AAATESTAAA - Home.connect()")
            
            home = try await Home.connect()
            
            print("AAATESTAAA - home.structures()")
            
            let allStructuresChanges = home!.structures()
            let allStructures = try await allStructuresChanges.list()
            structure = allStructures.first
        } catch {
            print("AAATESTAAA - error")
        }
    }
    
    func getStructure() -> Structure? {
        return structure
    }
    
    func getDevice(id: String) async -> HomeDevice? {
        try? await home?.devices().list().first(where: { $0.id == id })
    }
}

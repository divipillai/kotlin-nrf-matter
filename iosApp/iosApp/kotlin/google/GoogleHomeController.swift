//
//  GoogleHomeProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 24/03/2026.
//

import Combine
import ComposeApp
import Matter
import SharedCode
import OSLog
import GoogleHomeSDK

enum GoogleHomeControllerError: Error {
    case noStructureFound
}

class GoogleHomeController {

    nonisolated(unsafe) private static var controller: GoogleHomeController? = nil
    
    private var structure: Structure? = nil
    private var home: Home? = nil
    private var cancellables: Set<AnyCancellable> = []
    
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
        guard home == nil else {
            return
        }
        
        do {
            home = try await Home.connect()
            
            let allStructuresChanges = home!.structures()
            let allStructures = try await allStructuresChanges.list()
            structure = allStructures.first
        } catch {
        }
    }
    
    func getStructure() async -> Structure {
        try! await withCheckedThrowingContinuation { continuation in
            var cancellable: AnyCancellable?

            cancellable = home!
                .structures()
                .batched()
                .receive(on: DispatchQueue.main)
                .map { Array($0) }
                .filter { !$0.isEmpty }
                .prefix(1)
                .sink(
                    receiveCompletion: { completion in
                        if case .failure(let error) = completion {
                            continuation.resume(throwing: error)
                        }
                        cancellable?.cancel()
                    },
                    receiveValue: { structures in
                        if let first = structures.first {
                            continuation.resume(returning: first)
                        } else {
                            continuation.resume(throwing: GoogleHomeControllerError.noStructureFound)
                        }
                        cancellable?.cancel()
                    }
                )
        }
    }
    
    func getDevice(id: String) async -> HomeDevice? {
        try? await home?.devices().list().first(where: { $0.id == id })
    }
}

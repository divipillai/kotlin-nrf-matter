//
//  HomeKitControllerProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 13/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import HomeKit

class HomeKitController: NSObject, ObservableObject, HMHomeManagerDelegate {
    
    var homes: [HMHome] = []
    var primaryHome: HMHome?
    
    private var manager: HMHomeManager!
    
    nonisolated(unsafe) private static var instance: HomeKitController? = nil
    
    static func shared() -> HomeKitController {
        if let existingInstance = HomeKitController.instance {
            return existingInstance
        } else {
            let newInstance = HomeKitController()
            HomeKitController.instance = newInstance
            return newInstance
        }
    }
    
    override init() {
        super.init()
        manager = HMHomeManager()
        manager.delegate = self
    }
    
    func homeManagerDidUpdateHomes(_ manager: HMHomeManager) {
        print("DEBUG: Homes Updated")
        self.homes = manager.homes
        self.primaryHome = manager.primaryHome
        
        let home = homes[0]
    }
    
    func homeManagerDidUpdatePrimaryHome(_ manager: HMHomeManager) {
        self.primaryHome = manager.primaryHome
    }
    
    func getHomesCount() -> Int {
        return manager.homes.count
    }
    
    func getAccessoriesCount() -> Int {
        return manager.homes.reduce(0) { $0 + $1.accessories.count }
    }
    
    func getAccesory(nodeId: Int64) -> HMAccessory? {
        return manager.homes
            .flatMap(\.accessories)
            .filter { $0.matterNodeID != nil }
            .first { $0.matterNodeID! == nodeId }
    }
    
    func getAccessory(uuid: UUID) -> HMAccessory? {
        return manager.homes
            .flatMap(\.accessories)
            .first { $0.uniqueIdentifier == uuid }
    }
    
    func addAccessory() async -> UUID? {
        let accessoryManager = HMAccessorySetupManager()
        let request = HMAccessorySetupRequest()
        let result = try? await accessoryManager.performAccessorySetup(using: request)
        return result?.accessoryUniqueIdentifiers.first
    }
}

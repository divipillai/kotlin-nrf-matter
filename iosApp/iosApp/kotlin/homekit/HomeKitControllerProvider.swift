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

class HomeKitControllerProvider: NSObject, ObservableObject, HMHomeManagerDelegate {
    
    @Published var homes: [HMHome] = []
    @Published var primaryHome: HMHome?
    
    private var manager: HMHomeManager!
    
    nonisolated(unsafe) private static var instance: HomeKitControllerProvider? = nil
    
    static func shared() -> HomeKitControllerProvider {
        if let existingInstance = HomeKitControllerProvider.instance {
            return existingInstance
        } else {
            let newInstance = HomeKitControllerProvider()
            HomeKitControllerProvider.instance = newInstance
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
    
    func getAccessory(id: Int64) -> HMAccessory {
        let accesory = manager.homes[0].accessories[0] // todo
        return accesory
    }
    
    func addAccessory() async {
        let accessoryManager = HMAccessorySetupManager()
        let request = HMAccessorySetupRequest()
        let result = try? await accessoryManager.performAccessorySetup(using: request)
    }
}

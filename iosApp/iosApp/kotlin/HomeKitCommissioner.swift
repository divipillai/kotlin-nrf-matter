//
//  HomeKitCommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 12/03/2026.
//

import Matter
import SharedCode
import HomeKit

class HomeKitCommissioner {
    
    let provider = LocalControllerProvider(logTag: "MatterCommissioner")
    
    func commision(payload: String, nodeID: NSNumber) async throws {
        let homeManager = HMHomeManager()
        
        var homeDelegates = Set<NSObject>()
        
        var accessoryDelegates = Set<NSObject>()
    }
    
    func release() {
    }
}

class HomeStore: NSObject, ObservableObject, HMHomeManagerDelegate {
    
    @Published var homes: [HMHome] = []
    @Published var primaryHome: HMHome?
    
    private var manager: HMHomeManager!
    
    override init() {
        super.init()
        manager = HMHomeManager()
        manager.delegate = self
    }
    
    func homeManagerDidUpdateHomes(_ manager: HMHomeManager) {
        print("DEBUG: Homes Updated")
        self.homes = manager.homes
        self.primaryHome = manager.primaryHome
    }
    
    func homeManagerDidUpdatePrimaryHome(_ manager: HMHomeManager) {
        self.primaryHome = manager.primaryHome
    }
}

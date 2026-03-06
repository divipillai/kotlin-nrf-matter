//
//  MatterOnOffController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 06/03/2026.
//

import ComposeApp
import Matter
import SharedCode

class MatterOnOffControllerImpl : MatterOnOffController {
    
    private let device: MTRDevice
    private let baseDevice: MTRBaseDevice
    
    init() {
        let controller = MatterControllerProviderImpl().getController()!
        device = MTRDevice(nodeID: NodeIdProvider.id, controller: controller)
        baseDevice = MTRBaseDevice(nodeID: NodeIdProvider.id, controller: controller)
    }
    
    func turnOn() {
        let cluster = MTRBaseClusterOnOff(device: baseDevice, endpointID: 13, queue: DispatchQueue.global())
        print("AAATESTAAA - Cluster created: \(cluster)")
        cluster?.on { error in
            if let error {
                print("AAATESTAAA - error during on")
            } else {
                print("AAATESTAAA - success during on")
            }
        }
    }
    
    func turnOff() {
        let cluster = MTRBaseClusterOnOff(device: baseDevice, endpointID: 13, queue: DispatchQueue.global())
        print("AAATESTAAA - Cluster created: \(cluster)")
        cluster?.off { error in
            if let error {
                print("AAATESTAAA - error during off")
            } else {
                print("AAATESTAAA - success during off")
            }
        }
    }
}

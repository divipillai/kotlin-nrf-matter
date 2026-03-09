//
//  MatterOnOffController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 06/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import OSLog

class MatterOnOffControllerImpl : MatterOnOffController {
    
    private let device: MTRDevice
    private let baseDevice: MTRBaseDevice
    
    private let logger = Logger(subsystem: "nrf.matter", category: "MatterOnOffController")
    
    init() {
        let controller = MatterControllerProviderImpl().getController()!
        device = MTRDevice(nodeID: NodeIdProvider.id, controller: controller)
        baseDevice = MTRBaseDevice(nodeID: NodeIdProvider.id, controller: controller)
    }
    
    func turnOn() {
        let cluster = MTRBaseClusterOnOff(device: baseDevice, endpointID: 1, queue: DispatchQueue.global())
        logger.debug("Cluster created: \(cluster)")
        cluster?.on { [weak self] error in
            if let error {
                self?.logger.debug("Error during on")
            } else {
                self?.logger.debug("Success during on")
            }
        }
    }
    
    func turnOff() {
        let cluster = MTRBaseClusterOnOff(device: baseDevice, endpointID: 1, queue: DispatchQueue.global())
        logger.debug("Cluster created: \(cluster)")
        cluster?.off { [weak self] error in
            if let error {
                self?.logger.debug("Error during off")
            } else {
                self?.logger.debug("Success during off")
            }
        }
    }
    
    private func getDescriptors() {
        let descriptor = MTRBaseClusterDescriptor(device: baseDevice, endpointID: 0, queue: DispatchQueue.global())
        logger.debug("Descriptor: \(descriptor)")
        descriptor?.readAttributePartsList { [weak self] (partsList: [Any]?, error: Error?) in
            if let endpoints = partsList {
                self?.logger.debug("Supported Endpoints: \(endpoints)")
            }
        }
    }
}

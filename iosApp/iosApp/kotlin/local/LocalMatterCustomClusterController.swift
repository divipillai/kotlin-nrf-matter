//
//  LocalMatterCustomClusterController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 20/04/2026.
//

import Matter
import ComposeApp
import SharedCode

class LocalMatterCustomClusterController {
    
    func decommission(deviceId: DeviceId) async throws {
        let controller = try LocalControllerProvider(logTag: "LocalMatterCustomClusterController").getController()!
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)
        
//        baseDevice.readAttributes(withEndpointID: <#T##NSNumber?#>, clusterID: <#T##NSNumber?#>, attributeID: <#T##NSNumber?#>, params: <#T##MTRReadParams?#>, queue: <#T##dispatch_queue_t#>, completion: <#T##MTRDeviceResponseHandler#>)
//        baseDevice.writeAttribute(withEndpointID: <#T##NSNumber#>, clusterID: <#T##NSNumber#>, attributeID: <#T##NSNumber#>, value: <#T##Any#>, timedWriteTimeout: <#T##NSNumber?#>, queue: <#T##dispatch_queue_t#>, completion: <#T##MTRDeviceResponseHandler#>)
    }
}

//
//  MatterControllerProvider.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 06/03/2026.
//

import ComposeApp
import Matter
import SharedCode

class MatterControllerProviderImpl : MatterControllerProvider {
    
    private let controllerProvider = MatterControllerProviderCore(logTag: "ControllerProvider")
    
    func getController() -> MTRDeviceController? {
        try? controllerProvider.getController()
    }
    
    func release() {
        controllerProvider.release()
    }
}

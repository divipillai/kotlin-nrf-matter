package no.nordicsemi.nrf.matter.commission

//public func addMatterDevice(
//to structure: Structure,
//add3PFabricFirst: Bool,
//commissioningFacade: CommissioningFacade
//) async throws -> Set<String> {
//
//    let userDefaults = UserDefaults(
//            suiteName: CommissioningManager.appGroup)
//    userDefaults?.set(
//        add3PFabricFirst,
//        forKey: CommissioningUserDefaultsKeys.shouldPerform3PFabricCommissioning
//    )
//
//    do {
//        try await structure.prepareForMatterCommissioning()
//        } catch {
//            throw error
//        }
//
//        let topology = MatterAddDeviceRequest.Topology(
//                ecosystemName: "Google Home",
//        homes: [MatterAddDeviceRequest.Home(displayName: structure.name)]
//        )
//        let request = MatterAddDeviceRequest(topology: topology)
//
//        do {
//            try await request.perform()
//                let commissionedDeviceIDs =
//                try await structure.completeMatterCommissioning()
//
//                    // 🔑 Bridge to KMP
//                    let ids = commissionedDeviceIDs.compactMap { Long($0) }
//
//                    try await commissioningFacade.onSuccess(deviceIds: ids)
//
//                        return commissionedDeviceIDs
//                    } catch {
//                        try await commissioningFacade.onFailure()
//                            throw error
//                        }
//                }

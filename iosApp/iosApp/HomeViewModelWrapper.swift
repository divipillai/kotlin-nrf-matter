//
// Created by Himali Aryal on 27/01/2026.
//

import Foundation
import SwiftUI
import Combine
import ComposeApp

class HomeViewModelWrapper: ObservableObject {
    private let viewModel: HomeViewModel
    private var cancellables = Set<AnyCancellable>()

    @Published var devices: [DeviceUiModel] = []

    init(devicesRepository: DevicesRepository,
         devicesStateRepository: DevicesStateRepository,
         userPreferencesRepository: UserPreferencesRepository) {
        self.viewModel = HomeViewModel(
            devicesRepository: devicesRepository,
            devicesStateRepository: devicesStateRepository,
            userPreferencesRepository: userPreferencesRepository
        )

//        viewModel.devicesUiModelFlow
    }

    func updateDeviceState(deviceId: Int64, isOn: Bool, isOnline: Bool) {
        viewModel.updateDeviceState(deviceId: deviceId, isOnline: isOnline, isOn: isOn)
    }
}

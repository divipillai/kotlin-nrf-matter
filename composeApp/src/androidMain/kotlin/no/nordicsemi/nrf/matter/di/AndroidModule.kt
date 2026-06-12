package no.nordicsemi.nrf.matter.di

import no.nordicsemi.nrf.matter.HomeViewModel
import no.nordicsemi.nrf.matter.binding.BindingViewModel
import no.nordicsemi.nrf.matter.binding.DataStoreProvider
import no.nordicsemi.nrf.matter.chip.BindingManager
import no.nordicsemi.nrf.matter.chip.ChipClient
import no.nordicsemi.nrf.matter.chip.ClustersHelper
import no.nordicsemi.nrf.matter.chip.MatterBasicInfoProvider
import no.nordicsemi.nrf.matter.datasource.DeviceStateDataSource
import no.nordicsemi.nrf.matter.datasource.DevicesDataSource
import no.nordicsemi.nrf.matter.home.HomeViewModelAndroid
import no.nordicsemi.nrf.matter.logger.LoggerViewModel
import no.nordicsemi.nrf.matter.model.AndroidDeviceController
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.repository.AndroidDeviceStateDataSource
import no.nordicsemi.nrf.matter.repository.AndroidDevicesDataSource
import no.nordicsemi.nrf.matter.repository.BindingRepository
import no.nordicsemi.nrf.matter.repository.DevicesRepository
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.ui.light.LightCommandHandler
import no.nordicsemi.nrf.matter.ui.lock.LockCommandHandler
import no.nordicsemi.nrf.matter.ui.manspec.ManufacturerSpecCommandHandler
import no.nordicsemi.nrf.matter.ui.switch.SwitchCommandHandler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/*
 * Copyright (c) 2025, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
val androidModule = module {

    single<DevicesDataSource> {
        AndroidDevicesDataSource(androidContext())
    }
    single<DeviceStateDataSource> {
        AndroidDeviceStateDataSource(
            context = androidContext()
        )
    }
    single {
        DataStoreProvider(androidContext()).createDataStore()
    }

    single<ChipClient> { ChipClient(context = androidContext()) }
    single<ClustersHelper> { ClustersHelper(chipClient = get()) }
    single { BindingManager(chipClient = get()) }
    single<MatterBasicInfoProvider> { MatterBasicInfoProvider(chipClient = get()) }

    single<DevicesRepository> { DevicesRepository(dataSource = get()) }
    single<DevicesStateRepository> { DevicesStateRepository(dataSource = get()) }
    single<BindingRepository> { BindingRepository(get()) }

    // Inject DeviceController
    single<DeviceController> { AndroidDeviceController(get(), get(), get()) }

    factory { LightCommandHandler(get()) }
    factory { LockCommandHandler(get(), get()) }
    factory { ManufacturerSpecCommandHandler(get(), get()) }
    factory { SwitchCommandHandler(get(), get()) }

    // Binding Viewmodel
    viewModelOf(::HomeViewModel)
    viewModelOf(::HomeViewModelAndroid)
    viewModelOf(::BindingViewModel)
    viewModel { LoggerViewModel() }
}

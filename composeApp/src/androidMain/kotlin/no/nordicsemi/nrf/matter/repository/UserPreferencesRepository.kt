package no.nordicsemi.nrf.matter.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.IOException
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import no.nordicsemi.nrf.matter.data.UserPreferences
import no.nordicsemi.nrf.matter.data.userPreferencesDataStore

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

class UserPreferencesRepository(
    context: Context
) {

    // The datastore managed by UserPreferencesRepository.
    private val userPreferencesDataStore = context.userPreferencesDataStore

    // The Flow to read data from the DataStore.
    val userPreferencesFlow: Flow<UserPreferences> =
        userPreferencesDataStore.data.catch { exception ->
            if (exception is IOException) {
                Log.e("AAA", "Error reading user preferences with exception", exception)
                emit(UserPreferences())
            } else {
                throw exception
            }
        }

    val userPreferencesLiveData = userPreferencesFlow.asLiveData()

    suspend fun updateHideCodelabInfo(hide: Boolean) {
        Log.d("AAA", "updateHideCodelabInfo [$hide]")
        userPreferencesDataStore.updateData { prefs ->
            prefs.copy(hideCodelabInfo = hide)
        }
    }

    suspend fun updateHideOfflineDevices(hide: Boolean) {
        Log.d("AAA", "updateHideOfflineDevices [$hide]")
        userPreferencesDataStore.updateData { prefs ->
            prefs.copy(hideOfflineDevices = hide)
        }
    }

    suspend fun shouldShowHalfsheetNotification(): Boolean {
        Log.d("AAA", "shouldShowHalfsheetNotification")
        return userPreferencesFlow.first().showHalfsheetNotification
    }

    suspend fun updateShowHalfsheetNotification(show: Boolean) {
        Log.d("AAA", "updateShowHalfsheetNotification [$show]")
        userPreferencesDataStore.updateData { prefs ->
            prefs.copy(showHalfsheetNotification = show)
        }
    }

    suspend fun isHideCodelabInfo(): Boolean {
        return userPreferencesFlow.first().hideCodelabInfo
    }

    suspend fun getData(): UserPreferences {
        return userPreferencesFlow.first()
    }
}

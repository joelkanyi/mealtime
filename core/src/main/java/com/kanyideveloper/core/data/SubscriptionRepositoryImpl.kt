/*
 * Copyright 2023 Joel Kanyi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kanyideveloper.core.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import com.kanyideveloper.core.domain.SubscriptionRepository
import com.kanyideveloper.core.util.Constants
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QEntitlement
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class SubscriptionRepositoryImpl(
    private val context: Context,
) : SubscriptionRepository {
    override val isSubscribed: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            // preferences[Constants.SUBSCRIPTION] == true
            true
        }

    private var _subscriptionStatusUiState by mutableStateOf(false)
    override val subscriptionStatusUiState: Boolean
        get() = _subscriptionStatusUiState

    override suspend fun updateSubscriptionStatus() {
        this@SubscriptionRepositoryImpl._subscriptionStatusUiState = true

        Qonversion.shared.checkEntitlements(object : QonversionEntitlementsCallback {
            override fun onError(error: QonversionError) {
                Timber.e("Error: ${error.description}")
                this@SubscriptionRepositoryImpl._subscriptionStatusUiState = false
            }

            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                Timber.e("Success: ${entitlements["Premium"]?.isActive}")

                CoroutineScope(Dispatchers.IO).launch {
                    context.dataStore.edit { preferences ->
                        preferences[Constants.SUBSCRIPTION] =
                            entitlements["Premium"]?.isActive == true
                    }

                    this@SubscriptionRepositoryImpl._subscriptionStatusUiState = false
                }
            }
        })
    }
}

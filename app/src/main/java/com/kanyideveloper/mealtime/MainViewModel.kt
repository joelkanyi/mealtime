/*
 * Copyright 2022 Joel Kanyi.
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
package com.kanyideveloper.mealtime

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kanyideveloper.core.domain.SubscriptionRepository
import com.kanyideveloper.core.domain.UserDataRepository
import com.kanyideveloper.core.state.SubscriptionStatusUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel constructor(
    private val userDataRepository: UserDataRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    val user = mutableStateOf(Firebase.auth.currentUser)

    val theme = userDataRepository.themeStream

    val isSubscribed: StateFlow<SubscriptionStatusUiState> =
        subscriptionRepository.isSubscribed
            .map(SubscriptionStatusUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SubscriptionStatusUiState.Loading
            )

    val subscriptionStatusUiState = subscriptionRepository.subscriptionStatusUiState
    fun updateSubscriptionStatus() {
        viewModelScope.launch {
            subscriptionRepository.updateSubscriptionStatus()
        }
    }

    init {
        updateSubscriptionStatus()
    }
}

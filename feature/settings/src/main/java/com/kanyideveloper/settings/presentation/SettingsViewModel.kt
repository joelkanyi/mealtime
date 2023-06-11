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
package com.kanyideveloper.settings.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kanyideveloper.core.analytics.AnalyticsUtil
import com.kanyideveloper.core.domain.SubscriptionRepository
import com.kanyideveloper.core.domain.UserDataRepository
import com.kanyideveloper.core.state.SubscriptionStatusUiState
import com.kanyideveloper.core.state.TextFieldState
import com.kanyideveloper.core.util.UiEvents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel constructor(
    private val userDataRepository: UserDataRepository,
    subscriptionRepository: SubscriptionRepository,
    private val analyticsUtil: AnalyticsUtil,
) : ViewModel() {
    fun analyticsUtil() = analyticsUtil

    val isSubscribed: StateFlow<SubscriptionStatusUiState> =
        subscriptionRepository.isSubscribed
            .map(SubscriptionStatusUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SubscriptionStatusUiState.Loading
            )

    private val _eventsFlow = MutableSharedFlow<UiEvents>()
    val eventsFlow = _eventsFlow

    private val _shouldShowThemesDialog = mutableStateOf(false)
    val shouldShowThemesDialog: State<Boolean> = _shouldShowThemesDialog
    fun setShowThemesDialogState(value: Boolean) {
        _shouldShowThemesDialog.value = value
    }

    private val _shouldShowSubscriptionDialog = mutableStateOf(false)
    val shouldShowSubscriptionDialog: State<Boolean> = _shouldShowSubscriptionDialog
    fun setShowSubscriptionDialogState(value: Boolean) {
        _shouldShowSubscriptionDialog.value = value
    }

    private val _shouldShowFeedbackDialog = mutableStateOf(false)
    val shouldShowFeedbackDialog: State<Boolean> = _shouldShowFeedbackDialog
    fun setShowFeedbackDialogState(value: Boolean) {
        _shouldShowFeedbackDialog.value = value
    }

    private val _feedback = mutableStateOf(TextFieldState())
    val feedback: State<TextFieldState> = _feedback
    fun setFeedbackState(value: String) {
        _feedback.value = feedback.value.copy(
            text = value,
            error = null
        )
    }

    fun updateTheme(themeValue: Int) {
        viewModelScope.launch {
            userDataRepository.setTheme(themeValue = themeValue)
            setShowThemesDialogState(false)
        }
    }

    fun validateFeedbackTextfield(message: String) {
        if (message.isEmpty()) {
            _feedback.value = feedback.value.copy(
                error = "Feedback cannot be empty"
            )
        }
    }

    private val _logoutState = mutableStateOf(LogoutState())
    val logoutState: State<LogoutState> = _logoutState
    fun logoutUser() {
        viewModelScope.launch {
            _logoutState.value = LogoutState(isLoading = true)
            Firebase.auth.signOut()
            val user = Firebase.auth.currentUser
            if (user == null) {
                _logoutState.value = LogoutState(isLoading = false)
                _eventsFlow.emit(UiEvents.NavigationEvent(""))
            }
        }
    }
}

data class LogoutState(
    val isLoading: Boolean = false
)

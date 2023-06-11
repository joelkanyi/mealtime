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
package com.joelkanyi.auth.presentation.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joelkanyi.auth.domain.repository.AuthRepository
import com.joelkanyi.auth.presentation.destinations.SignInScreenDestination
import com.joelkanyi.auth.presentation.state.RegisterState
import com.joelkanyi.shared.data.network.utils.Resource
import com.kanyideveloper.core.analytics.AnalyticsUtil
import com.kanyideveloper.core.state.PasswordTextFieldState
import com.kanyideveloper.core.state.TextFieldState
import com.kanyideveloper.core.util.UiEvents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SignUpViewModel constructor(
    private val authRepository: AuthRepository,
    private val analyticsUtil: AnalyticsUtil,
) : ViewModel() {
    fun analyticsUtil() = analyticsUtil

    private val _registerState = mutableStateOf(RegisterState())
    val registerState: State<RegisterState> = _registerState

    private val _emailState = mutableStateOf(TextFieldState())
    val emailState: State<TextFieldState> = _emailState
    fun setEmail(value: String) {
        _emailState.value = emailState.value.copy(text = value)
    }

    private val _usernameState = mutableStateOf(TextFieldState())
    val usernameState: State<TextFieldState> = _usernameState
    fun setUsername(value: String) {
        _usernameState.value = usernameState.value.copy(text = value)
    }

    private val _passwordState = mutableStateOf(PasswordTextFieldState())
    val passwordState: State<PasswordTextFieldState> = _passwordState
    fun setPassword(value: String) {
        _passwordState.value = _passwordState.value.copy(text = value)
    }

    private val _confirmPasswordState = mutableStateOf(PasswordTextFieldState())
    val confirmPasswordState: State<PasswordTextFieldState> = _confirmPasswordState
    fun setConfirmPassword(value: String) {
        _confirmPasswordState.value = _confirmPasswordState.value.copy(text = value)
    }

    private val _eventsFlow = MutableSharedFlow<UiEvents>()
    val eventsFlow: SharedFlow<UiEvents> = _eventsFlow.asSharedFlow()

    fun registerUser() {
        viewModelScope.launch {
            if (emailState.value.text.isEmpty()) {
                _eventsFlow.emit(
                    UiEvents.SnackbarEvent(message = "Please input your email")
                )
                return@launch
            }

            if (usernameState.value.text.isEmpty()) {
                _eventsFlow.emit(
                    UiEvents.SnackbarEvent(message = "Please input your name")
                )
                return@launch
            }

            if (passwordState.value.text.isEmpty()) {
                _eventsFlow.emit(
                    UiEvents.SnackbarEvent(message = "Please input your password")
                )
                return@launch
            }

            if (confirmPasswordState.value.text.isEmpty()) {
                _eventsFlow.emit(
                    UiEvents.SnackbarEvent(message = "Please confirm your password")
                )
                return@launch
            }

            if (passwordState.value.text != confirmPasswordState.value.text) {
                _eventsFlow.emit(
                    UiEvents.SnackbarEvent(message = "Passwords do not match")
                )
                return@launch
            }

            _registerState.value = registerState.value.copy(isLoading = true)

            when (
                val result = authRepository.registerUser(
                    email = emailState.value.text,
                    name = usernameState.value.text,
                    password = passwordState.value.text
                )
            ) {
                is Resource.Error -> {
                    _registerState.value = registerState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Unknown Error Occurred"
                    )

                    _eventsFlow.emit(
                        UiEvents.SnackbarEvent(message = result.message ?: "Unknown Error Occurred")
                    )
                }

                is Resource.Success -> {
                    _registerState.value = registerState.value.copy(
                        isLoading = false,
                        data = result.data
                    )
                    _eventsFlow.emit(
                        UiEvents.SnackbarEvent("Account created successfully, check your email for verification link")
                    )
                    _eventsFlow.emit(
                        UiEvents.NavigationEvent(SignInScreenDestination.route)
                    )
                }

                else -> {
                    registerState
                }
            }
        }
    }

    fun togglePasswordVisibility() {
        _passwordState.value = passwordState.value.copy(
            isPasswordVisible = !passwordState.value.isPasswordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _confirmPasswordState.value = confirmPasswordState.value.copy(
            isPasswordVisible = !confirmPasswordState.value.isPasswordVisible
        )
    }
}

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

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.joelkanyi.auth.presentation.landing.AuthNavigator
import com.joelkanyi.auth.presentation.state.RegisterState
import com.kanyideveloper.compose_ui.theme.poppins
import com.kanyideveloper.core.state.PasswordTextFieldState
import com.kanyideveloper.core.state.TextFieldState
import com.kanyideveloper.core.util.UiEvents
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SignUpScreen(navigator: AuthNavigator, viewModel: SignUpViewModel = koinViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val registerState by viewModel.registerState
    val userName by viewModel.usernameState
    val email by viewModel.emailState
    val password by viewModel.passwordState
    val confirmPassword by viewModel.confirmPasswordState
    val keyboardController = LocalSoftwareKeyboardController.current
    val analyticsUtils = viewModel.analyticsUtil()

    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collectLatest { event ->
            when (event) {
                is UiEvents.SnackbarEvent -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }
                is UiEvents.NavigationEvent -> {
                    navigator.popBackStack()
                    navigator.openSignIn()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Top) {
                Text(
                    text = "Getting Started",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Create an account to continue with the app",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    ) {
        SignUpScreenContent(
            userName = userName,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            registerState = registerState,
            onCurrentEmailChange = {
                viewModel.setEmail(it)
            },
            onCurrentNameChange = {
                viewModel.setUsername(it)
            },
            onCurrentPasswordChange = {
                viewModel.setPassword(it)
            },
            onCurrentConfirmPasswordChange = {
                viewModel.setConfirmPassword(it)
            },
            onClickHaveAccount = {
                analyticsUtils.trackUserEvent("on click have account")
                navigator.openSignIn()
            },
            onClickSignUp = {
                analyticsUtils.trackUserEvent("on click sign up")
                keyboardController?.hide()
                viewModel.registerUser()
            },
            onPasswordToggle = {
                viewModel.togglePasswordVisibility()
            },
            onConfirmPasswordToggle = {
                viewModel.toggleConfirmPasswordVisibility()
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SignUpScreenContent(
    userName: TextFieldState,
    email: TextFieldState,
    password: PasswordTextFieldState,
    confirmPassword: PasswordTextFieldState,
    registerState: RegisterState,
    onCurrentNameChange: (String) -> Unit,
    onCurrentEmailChange: (String) -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onCurrentConfirmPasswordChange: (String) -> Unit,
    onClickSignUp: () -> Unit,
    onClickHaveAccount: () -> Unit,
    onPasswordToggle: (Boolean) -> Unit,
    onConfirmPasswordToggle: (Boolean) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(64.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userName.text,
                onValueChange = {
                    onCurrentNameChange(it)
                },
                label = {
                    Text(text = "Name")
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = true,
                    keyboardType = KeyboardType.Email
                )
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email.text,
                onValueChange = {
                    onCurrentEmailChange(it)
                },
                label = {
                    Text(text = "Email")
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                    keyboardType = KeyboardType.Email
                )
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password.text,
                onValueChange = {
                    onCurrentPasswordChange(it)
                },
                label = {
                    Text(text = "Password")
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (password.isPasswordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onPasswordToggle(!password.isPasswordVisible)
                        },
                        modifier = Modifier.semantics {
                            testTag = "PasswordToggle"
                        }
                    ) {
                        Icon(
                            imageVector = if (password.isPasswordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (password.isPasswordVisible) {
                                "Hide Password"
                            } else {
                                "Show Password"
                            }
                        )
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = confirmPassword.text,
                onValueChange = {
                    onCurrentConfirmPasswordChange(it)
                },
                label = {
                    Text(text = "Confirm Password")
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (confirmPassword.isPasswordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onConfirmPasswordToggle(!confirmPassword.isPasswordVisible)
                        },
                        modifier = Modifier.semantics {
                            testTag = "PasswordToggle"
                        }
                    ) {
                        Icon(
                            imageVector = if (confirmPassword.isPasswordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (confirmPassword.isPasswordVisible) {
                                "Hide Password"
                            } else {
                                "Show Password"
                            }
                        )
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onClickSignUp,
                shape = RoundedCornerShape(8)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    text = "Sign Up",
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onClickHaveAccount,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Already have an account?")
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Sign In")
                        }
                    },
                    fontFamily = poppins,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                if (registerState.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}

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
package com.joelkanyi.auth.presentation.signin

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.joelkanyi.auth.presentation.landing.AuthNavigator
import com.joelkanyi.auth.presentation.state.LoginState
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
fun SignInScreen(navigator: AuthNavigator, viewModel: SignInViewModel = koinViewModel()) {
    val emailState = viewModel.emailState.value
    val passwordState = viewModel.passwordState.value
    val keyboardController = LocalSoftwareKeyboardController.current
    val loginState = viewModel.loginState.value
    val snackbarHostState = remember { SnackbarHostState() }
    val analyticsUtils = viewModel.analyticsUtil()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvents.SnackbarEvent -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }

                is UiEvents.NavigationEvent -> {
                    navigator.switchNavGraphRoot()
                    snackbarHostState.showSnackbar(
                        message = "Login successful",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Login to your account to continue enjoying our services",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    ) {
        SignInScreenContent(
            emailState = emailState,
            passwordState = passwordState,
            loginState = loginState,
            onCurrentEmailTextChange = {
                viewModel.setUsername(it)
            },
            onCurrentPasswordTextChange = {
                viewModel.setPassword(it)
            },
            onClickSignIn = {
                analyticsUtils.trackUserEvent("sign_in_clicked")
                keyboardController?.hide()
                viewModel.loginUser()
            },
            onClickForgotPassword = {
                analyticsUtils.trackUserEvent("forgot_password_clicked")
                navigator.openForgotPassword()
            },
            onClickDontHaveAccount = {
                analyticsUtils.trackUserEvent("dont_have_account_clicked")
                navigator.openSignUp()
            },
            onConfirmPasswordToggle = {
                viewModel.togglePasswordVisibility()
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SignInScreenContent(
    emailState: TextFieldState,
    passwordState: PasswordTextFieldState,
    loginState: LoginState,
    onCurrentEmailTextChange: (String) -> Unit,
    onCurrentPasswordTextChange: (String) -> Unit,
    onClickSignIn: () -> Unit,
    onClickForgotPassword: () -> Unit,
    onClickDontHaveAccount: () -> Unit,
    onConfirmPasswordToggle: (Boolean) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Spacer(modifier = Modifier.height(64.dp))

            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = emailState.text,
                    onValueChange = {
                        onCurrentEmailTextChange(it)
                    },
                    label = {
                        Text(text = "Email")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    isError = emailState.error != null

                )
                if (emailState.error != "") {
                    Text(
                        text = emailState.error ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = passwordState.text,
                    onValueChange = {
                        onCurrentPasswordTextChange(it)
                    },
                    label = {
                        Text(text = "Password")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    isError = passwordState.error != null,
                    visualTransformation = if (passwordState.isPasswordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                onConfirmPasswordToggle(!passwordState.isPasswordVisible)
                            },
                            modifier = Modifier.semantics {
                                testTag = "PasswordToggle"
                            }
                        ) {
                            Icon(
                                imageVector = if (passwordState.isPasswordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (passwordState.isPasswordVisible) {
                                    "Hide Password"
                                } else {
                                    "Show Password"
                                }
                            )
                        }
                    }
                )
                if (passwordState.error != "") {
                    Text(
                        text = passwordState.error ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClickForgotPassword) {
                    Text(text = "Forgot password?")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClickSignIn,
                shape = RoundedCornerShape(8)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    text = "Sign In",
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onClickDontHaveAccount,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Don't have an account?")
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Sign Up")
                        }
                    },
                    fontFamily = poppins,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Spacer(modifier = Modifier.height(16.dp))
                if (loginState.isLoading) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

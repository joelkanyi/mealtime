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
package com.joelkanyi.auth.presentation.forgotpassword

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.joelkanyi.auth.presentation.landing.AuthNavigator
import com.joelkanyi.auth.presentation.state.LoginState
import com.kanyideveloper.core.util.UiEvents
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ForgotPasswordScreen(
    navigator: AuthNavigator,
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val forgotPasswordState = viewModel.forgotPasswordState.value

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Forgot Password",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Please enter an email address that you had registered with, so that we can send you a password reset link",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    ) {
        LaunchedEffect(key1 = true) {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is UiEvents.SnackbarEvent -> {
                        snackbarHostState.showSnackbar(
                            message = event.message
                        )
                    }

                    is UiEvents.NavigationEvent -> {
                        navigator.popBackStack()
                    }
                }
            }
        }

        ForgotPasswordScreenContent(
            currentEmailText = viewModel.emailState.value.text,
            forgotPasswordState = forgotPasswordState,
            onCurrentEmailTextChange = {
                viewModel.setEmailState(it)
            },
            onClickSend = {
                keyboardController?.hide()
                viewModel.sendPasswordResetLink()
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ForgotPasswordScreenContent(
    currentEmailText: String,
    forgotPasswordState: LoginState,
    onCurrentEmailTextChange: (String) -> Unit,
    onClickSend: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(100.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentEmailText,
                onValueChange = {
                    onCurrentEmailTextChange(it)
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
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onClickSend,
                shape = RoundedCornerShape(8)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    text = "Continue",
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            if (forgotPasswordState.isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

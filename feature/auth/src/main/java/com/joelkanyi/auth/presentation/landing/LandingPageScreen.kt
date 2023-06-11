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
package com.joelkanyi.auth.presentation.landing

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kanyideveloper.core.util.Constants.CLIENT_ID
import com.kanyideveloper.mealtime.core.R
import com.ramcosta.composedestinations.annotation.Destination
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import com.stevdzasan.onetap.rememberOneTapSignInState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel

interface AuthNavigator {
    fun openForgotPassword()
    fun openSignUp()
    fun openSignIn()
    fun switchNavGraphRoot()

    fun popBackStack()
}

@Destination
@Composable
fun LandingPageScreen(
    navigator: AuthNavigator,
    viewModel: LandingPageViewModel = koinViewModel(),
) {
    val auth = Firebase.auth
    val context = LocalContext.current
    val analyticsUtil = viewModel.analyticsUtil()

    val oneTapSignInState = rememberOneTapSignInState()
    OneTapSignInWithGoogle(
        state = oneTapSignInState,
        clientId = CLIENT_ID,
        onTokenIdReceived = { tokenId ->
            val firebaseCredential = GoogleAuthProvider.getCredential(tokenId, null)
            CoroutineScope(Dispatchers.Main).launch {
                val result = auth.signInWithCredential(firebaseCredential).await()
                if (result.user != null) {
                    analyticsUtil.setUserProfile(
                        userID = result.user?.uid ?: "",
                        userProperties = JSONObject()
                            .put("name", "${result?.user?.displayName}")
                            .put("email", "${result?.user?.email}")

                    )
                    navigator.switchNavGraphRoot()
                }
            }
        },
        onDialogDismissed = { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier
                .fillMaxSize(),
            painter = painterResource(id = R.drawable.banner),
            contentDescription = "Landing Page Food Image",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(
                    Alignment.BottomCenter
                )
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    analyticsUtil.trackUserEvent("Google Sign In Clicked")
                    oneTapSignInState.open()
                },
                enabled = !oneTapSignInState.opened,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Image(
                    modifier = Modifier.padding(6.dp),
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Icon"
                )

                Text(
                    text = "Sign In with Google",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    analyticsUtil.trackUserEvent("Email Sign In Clicked")
                    navigator.openSignUp()
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Sign Up with Email Icon",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                    Text(
                        text = "Sign Up with Email",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    analyticsUtil.trackUserEvent("Already have an account? Sign In Clicked")
                    navigator.openSignIn()
                }
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            append("Already have an account? ")
                        }

                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            append("Sign In")
                        }
                    }
                )
            }
        }
    }
}

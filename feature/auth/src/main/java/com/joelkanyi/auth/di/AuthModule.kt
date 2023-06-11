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
package com.joelkanyi.auth.di

import com.joelkanyi.auth.data.repository.AuthRepositoryImpl
import com.joelkanyi.auth.domain.repository.AuthRepository
import com.joelkanyi.auth.presentation.forgotpassword.ForgotPasswordViewModel
import com.joelkanyi.auth.presentation.landing.LandingPageViewModel
import com.joelkanyi.auth.presentation.signin.SignInViewModel
import com.joelkanyi.auth.presentation.signup.SignUpViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun authModule() = module {
    single<AuthRepository> {
        AuthRepositoryImpl(
            get(),
            get()
        )
    }

    viewModel {
        LandingPageViewModel(analyticsUtil = get())
    }

    viewModel {
        ForgotPasswordViewModel(
            authenticationRepository = get(),
        )
    }

    viewModel {
        SignInViewModel(
            authRepository = get(),
            analyticsUtil = get(),
        )
    }

    viewModel {
        SignUpViewModel(
            authRepository = get(),
            analyticsUtil = get(),
        )
    }
}

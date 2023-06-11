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
package com.joelkanyi.auth.domain.repository

import com.google.firebase.auth.AuthResult
import com.joelkanyi.shared.data.network.utils.Resource

interface AuthRepository {
    suspend fun registerUser(email: String, password: String, name: String): Resource<AuthResult>
    suspend fun loginUser(email: String, password: String): Resource<AuthResult>
    suspend fun forgotPassword(email: String): Resource<Any>
    suspend fun userAutologin(): Boolean
}

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
package com.joelkanyi.kitchen_timer.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.joelkanyi.kitchen_timer.domain.repository.KitchenTimerRepository
import com.kanyideveloper.core.analytics.AnalyticsUtil
import com.kanyideveloper.core.util.minutesToMilliseconds

class KitchenTimerViewModel constructor(
    private val repository: KitchenTimerRepository,
    private val analyticsUtil: AnalyticsUtil,
) : ViewModel() {
    fun analyticsUtil() = analyticsUtil
    val percentage = repository.percentage

    private val _currentTimerValue = mutableStateOf(0)
    val currentTimerValue: State<Int> = _currentTimerValue
    fun setCurrentTimerValue(value: Int) {
        _currentTimerValue.value = value
        repository.timeRemaining = minutesToMilliseconds(currentTimerValue.value)
    }

    private val _showHowLongDialog = mutableStateOf(false)
    val showHowLongDialog: State<Boolean> = _showHowLongDialog
    fun setShowHowLongDialog(value: Boolean) {
        _showHowLongDialog.value = value
    }

    val remainingTimerValue = repository.remainingTimerValue
    val isTimerRunning = repository.isTimerRunning

    fun startTimer() {
        repository.startTimer()
    }

    fun pauseTimer() {
        repository.pauseTimer()
    }

    fun stopTimer() {
        _currentTimerValue.value = 0
        if (repository.isTimerRunning.value == true) {
            repository.stopTimer()
        }
    }
}

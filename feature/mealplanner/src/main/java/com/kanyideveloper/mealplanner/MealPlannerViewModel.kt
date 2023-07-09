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
package com.kanyideveloper.mealplanner

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joelkanyi.shared.core.data.network.utils.Resource
import com.joelkanyi.shared.core.domain.CoreMeal
import com.kanyideveloper.core.analytics.AnalyticsUtil
import com.kanyideveloper.core.domain.HomeRepository
import com.kanyideveloper.core.domain.SubscriptionRepository
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.core.state.SubscriptionStatusUiState
import com.kanyideveloper.core.util.UiEvents
import com.kanyideveloper.core.util.getTodaysDate
import com.kanyideveloper.mealplanner.domain.repository.MealPlannerRepository
import com.kanyideveloper.mealplanner.model.MealPlan
import com.kanyideveloper.mealplanner.presentation.state.SearchMealState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class MealPlannerViewModel constructor(
    private val mealPlannerRepository: MealPlannerRepository,
    private val homeRepository: HomeRepository,
    subscriptionRepository: SubscriptionRepository,
    private val analyticsUtil: AnalyticsUtil
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

    private val _types = mutableStateOf<List<String>>(emptyList())
    val types: State<List<String>> = _types

    private val _allergies = mutableStateOf<List<String>>(emptyList())
    val allergies: State<List<String>> = _allergies

/*     private val _hasMealPlanPrefs = mutableStateOf(false)
    val hasMealPlanPrefs: State<Boolean> = _hasMealPlanPrefs */

    private val _selectedDate = mutableStateOf(getTodaysDate())
    val selectedDate: State<String> = _selectedDate
    fun setSelectedDateState(value: String) {
        _selectedDate.value = value
    }

    private val _mealsInPlanState = mutableStateOf(MealsInPlanState())
    val mealsInPlanState: State<MealsInPlanState> = _mealsInPlanState

    fun getPlanMeals(filterDay: String = selectedDate.value, isSubscribed: Boolean) {
        viewModelScope.launch {
            when (
                val result = mealPlannerRepository.getMealsInMyPlan(
                    filterDay = filterDay,
                    isSubscribed = isSubscribed
                )
            ) {
                is Resource.Error -> {
                    _eventsFlow.emit(
                        UiEvents.SnackbarEvent(
                            message = result.message ?: "An error occurred"
                        )
                    )
                }
                is Resource.Success -> {
                    result.data?.collectLatest { meals ->
                        _mealsInPlanState.value = MealsInPlanState(
                            isLoading = false,
                            error = null,
                            meals = meals
                        )
                    }
                }
                else -> {
                    _mealsInPlanState
                }
            }
        }
    }

    private fun getMealsTypes(isSubscribed: Boolean) {
        viewModelScope.launch {
            mealPlannerRepository.hasMealPlanPref(isSubscribed = isSubscribed)
                .collectLatest { result ->
                    _types.value = result?.dishTypes ?: emptyList()
                    _allergies.value = result?.allergies ?: emptyList()
                }
        }
    }

    private val _mealType = mutableStateOf("")
    val mealType: State<String> = _mealType
    fun setMealTypeState(value: String) {
        _mealType.value = value
    }

    private val _searchString = mutableStateOf("")
    val searchString: State<String> = _searchString
    fun setSearchStringState(value: String) {
        _searchString.value = value
    }

    private val _source = mutableStateOf("")
    val source: State<String> = _source
    fun setSourceState(value: String) {
        _source.value = value
        _searchMeals.value = searchMeals.value.copy(
            isLoading = false,
            error = null,
            meals = emptyList()
        )
    }

    private val _searchBy = mutableStateOf("")
    private val searchBy: State<String> = _searchBy
    fun setSearchByState(value: String) {
        _searchBy.value = value
    }

    private val _shouldShowMealsDialog = mutableStateOf(false)
    val shouldShowMealsDialog: State<Boolean> = _shouldShowMealsDialog
    fun setShouldShowMealsDialogState(value: Boolean) {
        _shouldShowMealsDialog.value = value
    }

    fun insertMealToPlan(meal: CoreMeal, mealTypePlan: String, date: String, isSubscribed: Boolean) {
        viewModelScope.launch {
            val existingMeals =
                mealPlannerRepository.getExistingMeals(mealType = mealTypePlan, date = date)
                    .toMutableList()
            existingMeals.add(element = meal)

            val newMealsList = existingMeals.toList()

            val plan = MealPlan(
                mealTypeName = mealTypePlan,
                date = selectedDate.value,
                meals = newMealsList,
                id = UUID.randomUUID().toString()
            )
            mealPlannerRepository.saveMealToPlan(mealPlan = plan, isSubscribed = isSubscribed)
        }
    }

    private val _searchMeals = mutableStateOf(SearchMealState())
    val searchMeals: State<SearchMealState> = _searchMeals

    private val _eventsFlow = MutableSharedFlow<UiEvents>()
    val eventsFlow = _eventsFlow

    fun searchMeal(isSubscribed: Boolean) {
        _searchMeals.value = searchMeals.value.copy(
            isLoading = true,
            meals = emptyList(),
            error = null
        )

        viewModelScope.launch {
            when (
                val result = mealPlannerRepository.searchMeal(
                    source = source.value,
                    searchBy = searchBy.value,
                    searchString = searchString.value.trim(),
                    isSubscribed = isSubscribed
                )
            ) {
                is Resource.Error -> {
                    eventsFlow.emit(
                        UiEvents.SnackbarEvent(
                            message = result.message ?: "Unknown Error Occurred"
                        )
                    )
                    _searchMeals.value = searchMeals.value.copy(
                        isLoading = false,
                        error = result.message ?: "Unknown Error Occurred"
                    )
                }
                is Resource.Success -> {
                    result.data?.collectLatest { meals ->
                        _searchMeals.value = searchMeals.value.copy(
                            isLoading = false,
                            meals = meals
                        )
                    }
                }
                else -> {
                    searchMeals
                }
            }
        }
    }

    fun removeMealFromPlan(id: String, isSubscribed: Boolean) {
        viewModelScope.launch {
            mealPlannerRepository.removeMealFromPlan(id = id, isSubscribed = isSubscribed)
        }
    }

    private val _singleMeal = MutableLiveData<LiveData<Meal?>>()
    val singleMeal: LiveData<LiveData<Meal?>> = _singleMeal
    fun getASingleMeal(id: String) {
        _singleMeal.value = homeRepository.getMealById(id = id)
    }

    private val _hasMealPlanPrefs = mutableStateOf(false)
    val hasMealPlanPrefs: State<Boolean> = _hasMealPlanPrefs

    fun getMealPlanPrefs(isSubscribed: Boolean) {
        Timber.e("Getting meal plan prefs")
        viewModelScope.launch {
            mealPlannerRepository.hasMealPlanPref(isSubscribed = isSubscribed)
                .collectLatest { result ->
                    Timber.e("Meal plan prefs: $result")
                    _hasMealPlanPrefs.value = result?.numberOfPeople != "0"

                    if (hasMealPlanPrefs.value) {
                        getMealsTypes(isSubscribed = isSubscribed)
                    }
                }
        }
    }
}

data class MealsInPlanState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val meals: List<MealPlan> = emptyList()
)

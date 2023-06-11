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
package com.kanyideveloper.presentation.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joelkanyi.shared.data.network.utils.Resource
import com.kanyideveloper.core.analytics.AnalyticsUtil
import com.kanyideveloper.core.domain.FavoritesRepository
import com.kanyideveloper.core.domain.HomeRepository
import com.kanyideveloper.core.domain.SubscriptionRepository
import com.kanyideveloper.core.model.Favorite
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.core.state.SubscriptionStatusUiState
import com.kanyideveloper.core.util.UiEvents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel constructor(
    private val homeRepository: HomeRepository,
    private val favoritesRepository: FavoritesRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val analyticsUtil: AnalyticsUtil
) : ViewModel() {
    fun analyticsUtil() = analyticsUtil

    private val _eventsFlow = MutableSharedFlow<UiEvents>()
    val eventsFlow = _eventsFlow.asSharedFlow()

    val isSubscribed: StateFlow<SubscriptionStatusUiState> =
        subscriptionRepository.isSubscribed
            .map(SubscriptionStatusUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SubscriptionStatusUiState.Loading
            )

    private val _shouldShowSubscriptionDialog = mutableStateOf(false)
    val shouldShowSubscriptionDialog: State<Boolean> = _shouldShowSubscriptionDialog
    fun setShowSubscriptionDialogState(value: Boolean) {
        _shouldShowSubscriptionDialog.value = value
    }

    private val _myMealsState = mutableStateOf(MyMealState())
    val myMealsState: State<MyMealState> = _myMealsState

    fun getMyMeals(filterCategory: String = "All") {
        _myMealsState.value = MyMealState(isLoading = true, error = null, meals = emptyList())
        viewModelScope.launch {
            when (val result = homeRepository.getMyMeals(true)) {
                is Resource.Error -> {
                    _eventsFlow.emit(
                        UiEvents.SnackbarEvent(
                            result.message ?: "An unexpected error occurred"
                        )
                    )

                    result.data?.collectLatest { meals ->
                        _myMealsState.value = myMealsState.value.copy(
                            isLoading = false,
                            meals = if (filterCategory == "All") {
                                meals
                            } else {
                                meals.filter { it.category == filterCategory }
                            }
                        )
                    }
                }

                is Resource.Success -> {
                    result.data?.collectLatest { meals ->
                        _myMealsState.value = myMealsState.value.copy(
                            isLoading = false,
                            meals = if (filterCategory == "All") {
                                meals
                            } else {
                                meals.filter { it.category == filterCategory }
                            }
                        )
                    }
                }

                else -> {
                    myMealsState
                }
            }
        }
    }

    private val _isMyMeal = mutableStateOf(true)
    val isMyMeal: State<Boolean> = _isMyMeal
    fun setIsMyMeal(value: Boolean) {
        _isMyMeal.value = value
    }

    private val _selectedCategory = mutableStateOf("All")
    val selectedCategory: State<String> = _selectedCategory
    fun setSelectedCategory(value: String) {
        _selectedCategory.value = value
    }

    fun inFavorites(id: String): LiveData<Boolean> {
        return favoritesRepository.isLocalFavorite(id = id)
    }

    fun insertAFavorite(
        isOnline: Boolean,
        onlineMealId: String? = null,
        localMealId: String,
        mealImageUrl: String,
        mealName: String,
        isSubscribed: Boolean
    ) {
        viewModelScope.launch {
            val favorite = Favorite(
                onlineMealId = onlineMealId,
                localMealId = localMealId,
                mealName = mealName,
                mealImageUrl = mealImageUrl,
                online = isOnline,
                favorite = true
            )
            when (
                val result = favoritesRepository.insertFavorite(
                    favorite = favorite,
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
                    _eventsFlow.emit(
                        UiEvents.SnackbarEvent(
                            message = "Added to favorites"
                        )
                    )
                }

                else -> {}
            }
        }
    }

    fun deleteALocalFavorite(localMealId: String, isSubscribed: Boolean) {
        viewModelScope.launch {
            favoritesRepository.deleteALocalFavorite(
                localMealId = localMealId,
                isSubscribed = isSubscribed
            )
        }
    }
}

data class MyMealState(
    val meals: List<Meal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

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
package com.kanyideveloper.favorites.presentation.favorites.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

class FavoritesViewModel constructor(
    private val favoritesRepository: FavoritesRepository,
    private val homeRepository: HomeRepository,
    subscriptionRepository: SubscriptionRepository,
    private val analyticsUtil: AnalyticsUtil,
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

    private val _eventsFlow = MutableSharedFlow<UiEvents>()
    val eventsFlow = _eventsFlow.asSharedFlow()

    private val _favoritesUiState = mutableStateOf(FavoritesUiState())
    val favoritesUiState = _favoritesUiState

    fun getFavorites(isSubscribed: Boolean) {
        _favoritesUiState.value =
            _favoritesUiState.value.copy(
                isLoading = true,
                favorites = emptyList(),
                error = null
            )
        viewModelScope.launch {
            when (val result = favoritesRepository.getFavorites(isSubscribed = isSubscribed)) {
                is Resource.Error -> {
                    _eventsFlow.emit(
                        UiEvents.SnackbarEvent(
                            message = result.message ?: "An unexpected error occurred"
                        )
                    )

                    result.data?.collectLatest { favorites ->
                        _favoritesUiState.value = _favoritesUiState.value.copy(
                            isLoading = false,
                            favorites = favorites,
                            error = result.message
                        )
                    }
                }
                is Resource.Success -> {
                    result.data?.collectLatest { favorites ->
                        _favoritesUiState.value = _favoritesUiState.value.copy(
                            isLoading = false,
                            favorites = favorites,
                            error = null
                        )
                    }
                }
                else -> {
                    favoritesUiState
                }
            }
        }
    }

    private val _singleMeal = MutableLiveData<LiveData<Meal?>>()
    val singleMeal: LiveData<LiveData<Meal?>> = _singleMeal
    fun getASingleMeal(id: String) {
        _singleMeal.value = homeRepository.getMealById(id = id)
    }

    fun deleteAFavorite(favorite: Favorite, isSubscribed: Boolean) {
        viewModelScope.launch {
            favoritesRepository.deleteOneFavorite(
                favorite = favorite,
                isSubscribed = isSubscribed
            )
        }
    }

    fun deleteAllFavorites() {
        viewModelScope.launch {
            favoritesRepository.deleteAllFavorites()
        }
    }
}

data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

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
package com.kanyideveloper.search.presentation.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joelkanyi.shared.core.data.network.utils.Resource
import com.kanyideveloper.core.analytics.AnalyticsUtil
import com.kanyideveloper.core.domain.FavoritesRepository
import com.kanyideveloper.core.model.Favorite
import com.kanyideveloper.core.util.UiEvents
import com.kanyideveloper.search.domain.SearchRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel constructor(
    private val searchRepository: SearchRepository,
    private val favoritesRepository: FavoritesRepository,
    private val analyticsUtil: AnalyticsUtil,
) : ViewModel() {
    fun analyticsUtil() = analyticsUtil
    private val _eventsFlow = MutableSharedFlow<UiEvents>()
    val eventsFlow = _eventsFlow

    private val _selectedSearchOption = mutableStateOf("Meal Name")
    val selectedSearchOption: State<String> = _selectedSearchOption
    fun setSelectedSearchOption(value: String) {
        _selectedSearchOption.value = value
        _searchState.value = searchState.value.copy(
            error = null,
            searchData = emptyList()
        )
    }

    private val _searchString = mutableStateOf("")
    val searchString: State<String> = _searchString
    fun setSearchString(value: String) {
        _searchString.value = value
        _searchState.value = searchState.value.copy(
            error = null,
            searchData = emptyList()
        )
    }

    private val _searchState = mutableStateOf(SearchState())
    val searchState: State<SearchState> = _searchState

    fun search(searchParam: String) {
        viewModelScope.launch {
            if (searchParam.isBlank()) {
                Timber.e("Empty Search String")
                _eventsFlow.emit(
                    UiEvents.SnackbarEvent(
                        message = "Please input a search term"
                    )
                )

                return@launch
            }

            _searchState.value = searchState.value.copy(
                isLoading = true
            )

            when (
                val result = searchRepository.search(
                    searchOption = selectedSearchOption.value,
                    searchParam = searchParam.trim()
                )
            ) {
                is Resource.Error -> {
                    _searchState.value = searchState.value.copy(
                        isLoading = false,
                        error = result.error?.message ?: "Unknown Error Occurred"
                    )
                }
                is Resource.Success -> {
                    _searchState.value = searchState.value.copy(
                        isLoading = false,
                        searchData = result.data ?: emptyList()
                    )
                }
                else -> {
                    searchState
                }
            }
        }
    }

    fun inOnlineFavorites(id: String): LiveData<Boolean> {
        return favoritesRepository.isOnlineFavorite(id = id)
    }

    fun insertAFavorite(
        isOnline: Boolean,
        onlineMealId: String? = null,
        localMealId: String? = null,
        mealImageUrl: String,
        mealName: String
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
                    isSubscribed = true
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

    fun deleteAnOnlineFavorite(onlineMealId: String) {
        viewModelScope.launch {
            favoritesRepository.deleteAnOnlineFavorite(
                onlineMealId = onlineMealId,
                isSubscribed = true
            )
        }
    }
}

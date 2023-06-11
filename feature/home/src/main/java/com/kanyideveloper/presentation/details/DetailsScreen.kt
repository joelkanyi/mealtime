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
package com.kanyideveloper.presentation.details

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.core.util.UiEvents
import com.kanyideveloper.presentation.details.common.DetailsCollapsingToolbar
import com.kanyideveloper.presentation.home.HomeNavigator
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Destination
@Composable
fun DetailsScreen(
    meal: Meal,
    navigator: HomeNavigator,
    viewModel: DetailsViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true, block = {
        viewModel.eventsFlow.collectLatest { event ->
            when (event) {
                is UiEvents.SnackbarEvent -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                else -> {}
            }
        }
    })

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                snackbarHostState
            )
        }
    ) {
        DetailsScreenContent(
            meal = meal,
            navigateBack = {
                navigator.popBackStack()
            },
            onRemoveFavorite = { localMealId, _ ->
                viewModel.deleteALocalFavorite(localMealId = localMealId)
            },
            addToFavorites = { mealId, imageUrl, name ->
                viewModel.insertAFavorite(
                    localMealId = mealId,
                    mealImageUrl = imageUrl,
                    mealName = name,
                    isOnline = false
                )
            }
        )
    }
}

@Composable
private fun DetailsScreenContent(
    meal: Meal,
    navigateBack: () -> Unit,
    onRemoveFavorite: (String, String) -> Unit,
    addToFavorites: (String, String, String) -> Unit
) {
    DetailsCollapsingToolbar(
        meal = meal,
        navigateBack = {
            navigateBack()
        },
        onRemoveFavorite = onRemoveFavorite,
        isOnlineMeal = false,
        addToFavorites = addToFavorites
    )
}

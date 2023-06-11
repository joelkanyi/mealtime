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
package com.kanyideveloper.presentation.details.randommeal

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.kanyideveloper.core.components.EmptyStateComponent
import com.kanyideveloper.core.components.ErrorStateComponent
import com.kanyideveloper.core.components.LoadingStateComponent
import com.kanyideveloper.presentation.details.DetailsState
import com.kanyideveloper.presentation.details.DetailsViewModel
import com.kanyideveloper.presentation.details.common.DetailsCollapsingToolbar
import com.kanyideveloper.presentation.home.HomeNavigator
import com.ramcosta.composedestinations.annotation.Destination
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Destination
@Composable
fun RandomOnlineMealDetailsScreen(
    navigator: HomeNavigator,
    viewModel: DetailsViewModel = koinViewModel()
) {
    LaunchedEffect(key1 = true) {
        viewModel.getRandomMeal()
    }

    val randomMealState = viewModel.randomMeal.value

    RandomOnlineMealScreenContent(
        randomMealState = randomMealState,
        navigateBack = {
            navigator.popBackStack()
        },
        onRemoveFavorite = { _, onlineMealId ->
            viewModel.deleteAnOnlineFavorite(onlineMealId = onlineMealId)
        },
        addToFavorites = { onlineMealId, imageUrl, name ->
            viewModel.insertAFavorite(
                onlineMealId = onlineMealId,
                mealImageUrl = imageUrl,
                mealName = name,
                isOnline = true
            )
        }
    )
}

@Composable
private fun RandomOnlineMealScreenContent(
    randomMealState: DetailsState,
    navigateBack: () -> Unit = {},
    onRemoveFavorite: (String, String) -> Unit,
    addToFavorites: (String, String, String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Data has been loaded successfully
        if (!randomMealState.isLoading && randomMealState.mealDetails.isNotEmpty()) {
            val meal = randomMealState.mealDetails.first()
            DetailsCollapsingToolbar(
                meal = meal,
                navigateBack = {
                    navigateBack()
                },
                onRemoveFavorite = onRemoveFavorite,
                addToFavorites = addToFavorites,
                isOnlineMeal = true
            )
        }

        // Loading data
        if (randomMealState.isLoading) {
            LoadingStateComponent()
        }

        // An Error has occurred
        if (!randomMealState.isLoading && randomMealState.error != null) {
            ErrorStateComponent(errorMessage = randomMealState.error)
        }

        // Loaded Data but the list is empty
        if (!randomMealState.isLoading && randomMealState.error == null && randomMealState.mealDetails.isEmpty()) {
            EmptyStateComponent()
        }
    }
}

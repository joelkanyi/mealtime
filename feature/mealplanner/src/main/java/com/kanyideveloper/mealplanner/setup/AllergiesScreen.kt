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
package com.kanyideveloper.mealplanner.setup

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kanyideveloper.compose_ui.components.StandardToolbar
import com.kanyideveloper.compose_ui.theme.PrimaryColor
import com.kanyideveloper.core.analytics.AnalyticsUtil
import com.kanyideveloper.core.components.EmptyStateComponent
import com.kanyideveloper.core.components.ErrorStateComponent
import com.kanyideveloper.core.components.LoadingStateComponent
import com.kanyideveloper.core.util.UiEvents
import com.kanyideveloper.mealplanner.MealPlannerNavigator
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Destination
@Composable
fun AllergiesScreen(
    editMealPlanPreference: Boolean = false,
    navigator: MealPlannerNavigator,
    viewModel: SetupViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val analyticsUtil = viewModel.analyticsUtil()

    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collectLatest { event ->
            when (event) {
                is UiEvents.SnackbarEvent -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    AllergiesScreenContent(
        navigator = navigator,
        analyticsUtil = analyticsUtil,
        ingredientsState = viewModel.ingredients.value,
        allergies = viewModel.gson.toJson(viewModel.allergicTo),
        editMealPlanPreference = editMealPlanPreference,
        onCheck = { allergy ->
            analyticsUtil.trackUserEvent("User allergic to $allergy")
            viewModel.insertAllergicTo(allergy)
        },
        isChecked = { allergy ->
            viewModel.allergicTo.contains(allergy)
        }
    )
}

@Composable
private fun AllergiesScreenContent(
    navigator: MealPlannerNavigator,
    allergies: String,
    ingredientsState: IngredientsState,
    isChecked: (String) -> Boolean,
    onCheck: (String) -> Unit,
    editMealPlanPreference: Boolean,
    analyticsUtil: AnalyticsUtil,
) {
    Column(Modifier.fillMaxSize()) {
        StandardToolbar(
            navigate = {
                navigator.popBackStack()
            },
            title = {},
            showBackArrow = true,
            navActions = {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable {
                            analyticsUtil.trackUserEvent("Allergies screen next button clicked")
                            navigator.openNoOfPeopleScreen(
                                allergies = allergies,
                                editMealPlanPreference = editMealPlanPreference
                            )
                        },
                    text = "Next",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryColor
                )
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            IngredientsLoadingState(state = ingredientsState)

            IngredientsErrorState(state = ingredientsState)

            IngredientsEmptyState(state = ingredientsState)

            IngredientsSuccessState(
                state = ingredientsState,
                isChecked = isChecked,
                onCheck = onCheck
            )
        }
    }
}

@Composable
private fun BoxScope.IngredientsEmptyState(state: IngredientsState) {
    if (!state.isLoading && state.error == null && state.ingredients.isEmpty()) {
        EmptyStateComponent()
    }
}

@Composable
private fun BoxScope.IngredientsErrorState(state: IngredientsState) {
    if (!state.isLoading && state.error != null) {
        ErrorStateComponent(errorMessage = state.error)
    }
}

@Composable
private fun BoxScope.IngredientsLoadingState(state: IngredientsState) {
    if (state.isLoading) {
        LoadingStateComponent()
    }
}

@Composable
fun IngredientsSuccessState(
    state: IngredientsState,
    isChecked: (String) -> Boolean,
    onCheck: (String) -> Unit
) {
    if (!state.isLoading && state.error == null && state.ingredients.isNotEmpty()) {
        LazyVerticalGrid(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Fixed(3)
        ) {
            item(span = { GridItemSpan(currentLineSpan = 3) }) {
                Text(
                    text = "Allergies/ dietary restrictions",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item(span = { GridItemSpan(currentLineSpan = 3) }) {
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(state.ingredients) { allergy ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isChecked(allergy),
                        onCheckedChange = {
                            onCheck(allergy)
                        }
                    )
                    Text(
                        text = allergy,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

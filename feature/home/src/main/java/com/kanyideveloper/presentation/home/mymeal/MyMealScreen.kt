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
package com.kanyideveloper.presentation.home.mymeal

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kanyideveloper.compose_ui.theme.Shapes
import com.kanyideveloper.core.analytics.AnalyticsUtil
import com.kanyideveloper.core.components.LoadingStateComponent
import com.kanyideveloper.core.components.SwipeRefreshComponent
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.core.util.LottieAnim
import com.kanyideveloper.core.util.UiEvents
import com.kanyideveloper.domain.model.MealCategory
import com.kanyideveloper.mealtime.core.R
import com.kanyideveloper.presentation.home.HomeNavigator
import com.kanyideveloper.presentation.home.HomeViewModel
import com.kanyideveloper.presentation.home.MyMealState
import com.kanyideveloper.presentation.home.composables.MealItem
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination
@Composable
fun MyMealScreen(
    isSubscribed: Boolean,
    navigator: HomeNavigator,
    viewModel: HomeViewModel = koinViewModel()
) {
    val myMealsState = viewModel.myMealsState.value
    val snackbarHostState = remember { SnackbarHostState() }
    val analyticsUtils = viewModel.analyticsUtil()

    LaunchedEffect(key1 = true, block = {
        viewModel.getMyMeals()

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
    MyMealScreenContent(
        myMealState = myMealsState,
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        analyticsUtils = analyticsUtils,
        openMealDetails = { meal ->
            analyticsUtils.trackUserEvent("Opened My Meal Details")
            navigator.openMealDetails(meal = meal)
        },
        addToFavorites = { localMealId, imageUrl, name ->
            analyticsUtils.trackUserEvent("Added My Meal To Favorites - $name")
            viewModel.insertAFavorite(
                localMealId = localMealId,
                mealImageUrl = imageUrl,
                mealName = name,
                isOnline = false,
                isSubscribed = isSubscribed
            )
        },
        removeFromFavorites = { id ->
            analyticsUtils.trackUserEvent("Removed My Meal From Favorites")
            viewModel.deleteALocalFavorite(
                localMealId = id,
                isSubscribed = isSubscribed
            )
        },
        isSelected = { category ->
            viewModel.selectedCategory.value == category
        },
        onCategoryClick = { category ->
            analyticsUtils.trackUserEvent("Selected My Meal Category - $category")
            viewModel.setSelectedCategory(category)
            viewModel.getMyMeals(viewModel.selectedCategory.value)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyMealScreenContent(
    myMealState: MyMealState,
    viewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState,
    openMealDetails: (Meal) -> Unit = {},
    addToFavorites: (String, String, String) -> Unit,
    removeFromFavorites: (String) -> Unit,
    isSelected: (String) -> Boolean,
    onCategoryClick: (String) -> Unit,
    analyticsUtils: AnalyticsUtil,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                snackbarHostState
            )
        }
    ) { paddingValues ->
        SwipeRefreshComponent(
            isRefreshingState = myMealState.isLoading,
            onRefreshData = {
                analyticsUtils.trackUserEvent("Refreshed My Meals")
                viewModel.getMyMeals()
            }
        ) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Loaded Data and the list is not empty
                if (!myMealState.isLoading) {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                modifier = Modifier.padding(vertical = 5.dp),
                                text = "Categories",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        item(span = { GridItemSpan(2) }) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(mealCategories) { category ->
                                    MyMealsCategoryItem(
                                        category = category,
                                        isSelected = isSelected,
                                        onCategoryClick = onCategoryClick
                                    )
                                }
                            }
                        }
                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (myMealState.meals.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    text = "Meals",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        items(myMealState.meals) { meal ->
                            MealItem(
                                modifier = Modifier.clickable {
                                    openMealDetails(meal)
                                },
                                meal = meal,
                                addToFavorites = addToFavorites,
                                removeFromFavorites = removeFromFavorites,
                                viewModel = viewModel
                            )
                        }

                        if (myMealState.meals.isEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .testTag("Empty State Component"),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    LottieAnim(
                                        resId = R.raw.astronaut,
                                        height = 120.dp
                                    )
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = "You don't have local meals, you can add some.",
                                        style = MaterialTheme.typography.titleSmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Loading data
                if (myMealState.isLoading) {
                    LoadingStateComponent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyMealsCategoryItem(
    category: MealCategory,
    isSelected: (String) -> Boolean,
    onCategoryClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .size(65.dp),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected(category.name)) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = {
            onCategoryClick(category.name)
        }
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp),
                painter = painterResource(id = category.icon),
                contentDescription = null,
                tint = if (isSelected(category.name)) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = category.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected(category.name)) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

private val mealCategories = listOf(
    MealCategory(
        "All",
        R.drawable.fork_knife_thin
    ),
    MealCategory(
        "Food",
        R.drawable.ic_food
    ),
    MealCategory(
        "Breakfast",
        R.drawable.ic_breakfast
    ),
    MealCategory(
        "Drinks",
        R.drawable.ic_drinks
    ),
    MealCategory(
        "Fruits",
        R.drawable.ic_fruit
    ),
    MealCategory(
        "Fast Food",
        R.drawable.ic_pizza_thin
    )
)

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
package com.kanyideveloper.presentation.home.onlinemeal

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kanyideveloper.compose_ui.theme.Shapes
import com.kanyideveloper.core.components.EmptyStateComponent
import com.kanyideveloper.core.components.ErrorStateComponent
import com.kanyideveloper.core.components.LoadingStateComponent
import com.kanyideveloper.core.components.SwipeRefreshComponent
import com.kanyideveloper.core.util.UiEvents
import com.kanyideveloper.core.util.showDayCookMessage
import com.kanyideveloper.domain.model.Category
import com.kanyideveloper.domain.model.OnlineMeal
import com.kanyideveloper.mealtime.core.R
import com.kanyideveloper.presentation.home.HomeNavigator
import com.kanyideveloper.presentation.home.onlinemeal.state.CategoriesState
import com.kanyideveloper.presentation.home.onlinemeal.state.MealState
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination
@Composable
fun OnlineMealScreen(
    isSubscribed: Boolean,
    navigator: HomeNavigator,
    viewModel: OnlineMealViewModel = koinViewModel()
) {
    val mealsState = viewModel.meals.value
    val categoriesState = viewModel.categories.value
    val selectedCategory = viewModel.selectedCategory.value
    val snackbarHostState = remember { SnackbarHostState() }
    val analyticsUtil = viewModel.analyticsUtil()

    LaunchedEffect(key1 = true, block = {
        analyticsUtil.trackUserEvent("open online meals screen")
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

    OnlineMealScreenContent(categoriesState = categoriesState,
        selectedCategory = selectedCategory,
        mealsState = mealsState,
        snackbarHostState = snackbarHostState,
        onMealClick = { mealId ->
            analyticsUtil.trackUserEvent("select online meal - $mealId")
            navigator.openOnlineMealDetails(mealId = mealId)
        },
        onSelectCategory = { categoryName ->
            analyticsUtil.trackUserEvent("select online meal category - $categoryName")
            viewModel.setSelectedCategory(categoryName)
            viewModel.getMeals(viewModel.selectedCategory.value)
        },
        addToFavorites = { onlineMealId, imageUrl, name ->
            analyticsUtil.trackUserEvent("add online meal to favorites - $name")
            viewModel.insertAFavorite(
                onlineMealId = onlineMealId,
                mealImageUrl = imageUrl,
                mealName = name,
                isOnline = true,
                isSubscribed = isSubscribed
            )
        },
        removeFromFavorites = { id ->
            analyticsUtil.trackUserEvent("remove online meal from favorites - $id")
            viewModel.deleteAnOnlineFavorite(
                onlineMealId = id, isSubscribed = isSubscribed
            )
        },
        openRandomMeal = {
            analyticsUtil.trackUserEvent("open random online meal")
            navigator.openRandomMeals()
        },
        onRefreshData = {
            viewModel.getMeals(viewModel.selectedCategory.value)
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@VisibleForTesting
@Composable
fun OnlineMealScreenContent(
    categoriesState: CategoriesState,
    selectedCategory: String,
    mealsState: MealState,
    onSelectCategory: (String) -> Unit,
    onMealClick: (String) -> Unit,
    addToFavorites: (String, String, String) -> Unit,
    removeFromFavorites: (String) -> Unit,
    openRandomMeal: () -> Unit,
    onRefreshData: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(snackbarHost = {
        SnackbarHost(
            snackbarHostState
        )
    }) { paddingValues ->
        SwipeRefreshComponent(
            isRefreshingState = mealsState.isLoading, onRefreshData = onRefreshData
        ) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Data Loaded Successfully
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        CategorySelection(state = categoriesState,
                            selectedCategory = selectedCategory,
                            onClick = { categoryName ->
                                onSelectCategory(categoryName)
                            })
                    }
                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item(span = { GridItemSpan(2) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(180.dp),
                            shape = Shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Box(Modifier.fillMaxSize()) {
                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    painter = painterResource(id = R.drawable.randomize_mealss),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = showDayCookMessage(),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White
                                    )
                                    Button(
                                        onClick = openRandomMeal,
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 30.dp
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_refresh),
                                                contentDescription = "Random meal shuffle icon",
                                            )
                                            Text(
                                                text = "Get a Random Meal",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(mealsState.meals) { meal ->
                        OnlineMealItem(
                            meal = meal,
                            onClick = { mealId ->
                                onMealClick(mealId)
                            },
                            addToFavorites = addToFavorites,
                            removeFromFavorites = removeFromFavorites
                        )
                    }
                }

                // Loading data
                if (mealsState.isLoading) {
                    LoadingStateComponent()
                }

                // An Error has occurred
                if (!mealsState.isLoading && mealsState.error != null && mealsState.meals.isEmpty()) {
                    ErrorStateComponent(errorMessage = mealsState.error)
                }

                // Loaded Data but the list is empty
                if (!mealsState.isLoading && mealsState.error == null && mealsState.meals.isEmpty()) {
                    EmptyStateComponent()
                }
            }
        }
    }
}

@Composable
fun OnlineMealItem(
    meal: OnlineMeal,
    onClick: (String) -> Unit,
    addToFavorites: (String, String, String) -> Unit,
    removeFromFavorites: (String) -> Unit,
    viewModel: OnlineMealViewModel = koinViewModel()
) {
    val isFav = viewModel.isOnlineFavorite(id = meal.mealId).collectAsState().value

    Card(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .padding(vertical = 5.dp)
            .clickable {
                onClick(meal.mealId)
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentDescription = meal.name,
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(data = meal.imageUrl)
                        .apply(block = fun ImageRequest.Builder.() {
                            placeholder(R.drawable.placeholder)
                        }).build()
                ),
                contentScale = ContentScale.Crop
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .padding(vertical = 3.dp),
                    text = meal.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = {
                    if (isFav) {
                        removeFromFavorites(meal.mealId)
                    } else {
                        addToFavorites(meal.mealId, meal.imageUrl, meal.name)
                    }
                }) {
                    Icon(
                        modifier = Modifier.size(30.dp), painter = if (isFav) {
                            painterResource(id = R.drawable.filled_favorite)
                        } else {
                            painterResource(id = R.drawable.heart_plus)
                        }, contentDescription = null, tint = if (isFav) {
                            Color(0xFFfa4a0c)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySelection(state: CategoriesState, onClick: (String) -> Unit, selectedCategory: String) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(state.categories) { category ->
            CategoryItem(
                category = category, onClick = {
                    onClick(category.categoryName)
                }, selectedCategory = selectedCategory
            )
        }
    }
}

@Composable
fun CategoryItem(category: Category, selectedCategory: String, onClick: () -> Unit) {
    val selected = selectedCategory == category.categoryName
    Card(
        Modifier
            .width(100.dp)
            .wrapContentHeight()
            .clickable {
                onClick()
            }, shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .width(100.dp)
                    .height(50.dp),
                contentDescription = null,
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = category.categoryImageUrl)
                        .apply(block = fun ImageRequest.Builder.() {
                            placeholder(R.drawable.food_loading)
                        }).build()
                ),
                contentScale = ContentScale.Inside
            )

            Text(
                text = category.categoryName,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

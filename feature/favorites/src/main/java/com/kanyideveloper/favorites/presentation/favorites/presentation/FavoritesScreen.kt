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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kanyideveloper.compose_ui.components.StandardToolbar
import com.kanyideveloper.compose_ui.theme.Shapes
import com.kanyideveloper.core.components.EmptyStateComponent
import com.kanyideveloper.core.components.LoadingStateComponent
import com.kanyideveloper.core.components.SwipeRefreshComponent
import com.kanyideveloper.core.model.Favorite
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.core.state.SubscriptionStatusUiState
import com.kanyideveloper.core.util.UiEvents
import com.kanyideveloper.mealtime.core.R
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

interface FavoritesNavigator {
    fun openOnlineMealDetails(mealId: String)
    fun openMealDetails(meal: Meal)
}

@Destination
@Composable
fun FavoritesScreen(
    navigator: FavoritesNavigator,
    viewModel: FavoritesViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val favoritesUiState = viewModel.favoritesUiState.value
    val meal = viewModel.singleMeal.observeAsState().value?.observeAsState()?.value
    var mDisplayMenu by remember { mutableStateOf(false) }
    val analyticsUtil = viewModel.analyticsUtil()

    when (val isSubscribed = viewModel.isSubscribed.collectAsState().value) {
        is SubscriptionStatusUiState.Success -> {
            LaunchedEffect(key1 = true, block = {
                analyticsUtil.trackUserEvent("open favorites screen")
                viewModel.getFavorites(
                    isSubscribed = isSubscribed.isSubscribed
                )

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

            FavoritesScreenContent(
                favoritesUiState = favoritesUiState,
                snackbarHostState = snackbarHostState,
                displayMenu = mDisplayMenu,
                onToggleMenu = {
                    mDisplayMenu = it
                },
                onClick = { _, onlineMealId, localMealId, isOnline ->
                    analyticsUtil.trackUserEvent("Open favorite meal details clicked")
                    if (isOnline) {
                        onlineMealId?.let { navigator.openOnlineMealDetails(mealId = it) }
                    } else {
                        if (localMealId != null) {
                            viewModel.getASingleMeal(id = localMealId)

                            if (meal != null) {
                                navigator.openMealDetails(meal = meal)
                            }
                        }
                    }
                },
                onFavoriteClick = { favorite ->
                    analyticsUtil.trackUserEvent("Delete one favorite clicked - ${favorite.mealName}")
                    viewModel.deleteAFavorite(
                        favorite = favorite,
                        isSubscribed = isSubscribed.isSubscribed
                    )
                },
                onRefreshData = {
                    analyticsUtil.trackUserEvent("Refresh Favorites clicked")
                    viewModel.getFavorites(
                        isSubscribed = isSubscribed.isSubscribed
                    )
                },
                onDeleteAllFavs = {
                    analyticsUtil.trackUserEvent("Delete All Favorites clicked")
                    viewModel.deleteAllFavorites()
                    mDisplayMenu = false
                }
            )
        }

        else -> {}
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreenContent(
    modifier: Modifier = Modifier,
    favoritesUiState: FavoritesUiState,
    snackbarHostState: SnackbarHostState,
    onClick: (Int?, String?, String?, Boolean) -> Unit,
    onFavoriteClick: (Favorite) -> Unit,
    onRefreshData: () -> Unit,
    onDeleteAllFavs: () -> Unit,
    displayMenu: Boolean,
    onToggleMenu: (Boolean) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                snackbarHostState
            )
        },
        topBar = {
            StandardToolbar(
                navigate = {},
                title = {
                    Text(text = "Favorite meals", fontSize = 18.sp)
                },
                showBackArrow = false,
                navActions = {
                    IconButton(onClick = { onToggleMenu(!displayMenu) }) {
                        Icon(Icons.Default.MoreVert, "")
                    }

                    DropdownMenu(
                        expanded = displayMenu,
                        onDismissRequest = { onToggleMenu(false) }
                    ) {
                        DropdownMenuItem(
                            onClick = onDeleteAllFavs,
                            text = {
                                Text(text = "Delete All Favorites")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.chevron_right),
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        SwipeRefreshComponent(
            isRefreshingState = favoritesUiState.isLoading,
            onRefreshData = onRefreshData
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Favorites list
                if (!favoritesUiState.isLoading) {
                    LazyColumn {
                        items(
                            favoritesUiState.favorites,
                            key = { favorite -> favorite.id!! }
                        ) { favorite ->
                            FoodItem(
                                modifier = Modifier.animateItemPlacement(),
                                favorite = favorite,
                                onClick = onClick,
                                onFavoriteClick = onFavoriteClick
                            )
                        }
                    }
                }

                // Empty state
                if (favoritesUiState.favorites.isEmpty() && !favoritesUiState.isLoading) {
                    EmptyStateComponent()
                }

                // Loading state
                if (favoritesUiState.isLoading) {
                    LoadingStateComponent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItem(
    favorite: Favorite,
    modifier: Modifier = Modifier,
    onClick: (Int?, String?, String?, Boolean) -> Unit,
    onFavoriteClick: (Favorite) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp, vertical = 5.dp),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = {
            onClick(
                favorite.id,
                favorite.onlineMealId,
                favorite.localMealId,
                favorite.onlineMealId != null
            )
        }
    ) {
        Column {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentDescription = null,
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = favorite.mealImageUrl)
                        .apply(block = fun ImageRequest.Builder.() {
                            placeholder(R.drawable.placeholder)
                        }).build()
                ),
                contentScale = ContentScale.Crop
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .wrapContentHeight(),
                    text = favorite.mealName,
                    style = MaterialTheme.typography.titleMedium
                )

                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            onFavoriteClick(favorite)
                        },
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFfa4a0c)
                )
            }
        }
    }
}

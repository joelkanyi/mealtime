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
package com.kanyideveloper.presentation.details.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.mealtime.core.R
import com.kanyideveloper.presentation.details.DetailsViewModel
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun DetailsCollapsingToolbar(
    meal: Meal,
    navigateBack: () -> Unit,
    onRemoveFavorite: (String, String) -> Unit,
    addToFavorites: (String, String, String) -> Unit,
    isOnlineMeal: Boolean = false,
    viewModel: DetailsViewModel = koinViewModel()
) {
    val state = rememberCollapsingToolbarScaffoldState()
    val textSize = (18 + (30 - 18) * state.toolbarState.progress).sp

    val isFavorite = if (isOnlineMeal) {
        meal.onlineMealId?.let {
            viewModel.isOnlineFavorite(id = it).collectAsState().value
        } != null
    } else {
        meal.localMealId?.let { viewModel.inLocalFavorites(id = it).observeAsState().value } != null
    }

    Timber.e("Meal Details is isFavorite: $isFavorite")

    CollapsingToolbarScaffold(
        modifier = Modifier.fillMaxSize(),
        state = state,
        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
        toolbarModifier = Modifier.background(MaterialTheme.colorScheme.background),
        enabled = true,
        toolbar = {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .height(150.dp)
                    .pin()
            )

            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .parallax(ratio = 0.5f)
                    .graphicsLayer {
                        alpha = if (textSize.value == 18f) 0f else 1f
                    },
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = meal.imageUrl)
                        .apply(block = fun ImageRequest.Builder.() {
                            placeholder(R.drawable.placeholder)
                        }).build()
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Text(
                text = meal.name ?: "",
                modifier = Modifier
                    .road(Alignment.CenterStart, Alignment.BottomEnd)
                    .padding(60.dp, 16.dp, 16.dp, 16.dp),
                color = if (textSize.value >= 19) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                fontSize = textSize
            )

            IconButton(onClick = {
                navigateBack()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) {
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            item {
                if (textSize.value >= 19) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            text = meal.name ?: "",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    if (isFavorite) {
                                        if (isOnlineMeal) {
                                            onRemoveFavorite("", meal.onlineMealId!!)
                                        } else {
                                            onRemoveFavorite(meal.localMealId!!, "")
                                        }
                                    } else {
                                        if (isOnlineMeal) {
                                            meal.onlineMealId?.let {
                                                addToFavorites(
                                                    it,
                                                    meal.imageUrl ?: "",
                                                    meal.name ?: ""
                                                )
                                            }
                                        } else {
                                            meal.localMealId?.let {
                                                addToFavorites(
                                                    it,
                                                    meal.imageUrl ?: "",
                                                    meal.name ?: ""
                                                )
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(30.dp),
                                painter = if (isFavorite) {
                                    painterResource(id = R.drawable.filled_favorite)
                                } else {
                                    painterResource(id = R.drawable.heart_plus)
                                },
                                contentDescription = null,
                                tint = if (isFavorite) {
                                    Color(0xFFfa4a0c)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
            item {
                if (!isOnlineMeal) {
                    Spacer(modifier = Modifier.height(24.dp))
                    MealProperties(meal)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(meal.ingredients) { ingredient ->
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground)
                    )
                    Text(
                        modifier = Modifier.padding(3.dp),
                        text = ingredient,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cooking Instructions",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            itemsIndexed(meal.cookingDirections) { index, instruction ->
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(6.dp),
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        modifier = Modifier.padding(6.dp),
                        text = instruction,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

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
package com.kanyideveloper.search.presentation.search

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kanyideveloper.compose_ui.components.StandardToolbar
import com.kanyideveloper.core.components.EmptyStateComponent
import com.kanyideveloper.core.components.ErrorStateComponent
import com.kanyideveloper.core.components.LoadingStateComponent
import com.kanyideveloper.core.model.OnlineMeal
import com.kanyideveloper.core.util.UiEvents
import com.kanyideveloper.mealtime.core.R
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

interface SearchNavigator {
    fun openOnlineMealDetails(mealId: String)

    fun popBackStack()
}

@Destination
@Composable
fun SearchScreen(navigator: SearchNavigator, viewModel: SearchViewModel = koinViewModel()) {
    val searchState = viewModel.searchState.value
    val context = LocalContext.current
    val analyticsUtil = viewModel.analyticsUtil()

    LaunchedEffect(key1 = true) {
        analyticsUtil.trackUserEvent("open search meals screen")
        viewModel.eventsFlow.collectLatest { event ->
            when (event) {
                is UiEvents.SnackbarEvent -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    SearchScreenContent(
        searchState = searchState,
        currentSearchString = viewModel.searchString.value,
        onSearchStringChange = { newValue ->
            viewModel.setSearchString(newValue)
        },
        onSearch = { searchParam ->
            analyticsUtil.trackUserEvent("search online meals - $searchParam")
            viewModel.search(searchParam)
        },
        onSearchOptionClick = { option ->
            analyticsUtil.trackUserEvent("select search online meals option - $option")
            viewModel.setSelectedSearchOption(option)
            if (viewModel.searchString.value.isNotEmpty()) {
                viewModel.search(viewModel.searchString.value)
            }
        },
        isSelected = { option ->
            viewModel.selectedSearchOption.value == option
        },
        addToFavorites = { onlineMealId, imageUrl, name ->
            analyticsUtil.trackUserEvent("add online meal to favorites - $name")
            viewModel.insertAFavorite(
                onlineMealId = onlineMealId,
                mealImageUrl = imageUrl,
                mealName = name,
                isOnline = true
            )
        },
        removeFromFavorites = { id ->
            analyticsUtil.trackUserEvent("remove online meal from favorites - $id")
            viewModel.deleteAnOnlineFavorite(
                onlineMealId = id
            )
        },
        onMealClick = { mealId ->
            analyticsUtil.trackUserEvent("open online meal details - $mealId")
            navigator.openOnlineMealDetails(mealId = mealId)
        },
        onClickBack = {
            analyticsUtil.trackUserEvent("back to home screen")
            navigator.popBackStack()
        }
    )
}

@Composable
private fun SearchScreenContent(
    searchState: SearchState,
    onMealClick: (String) -> Unit,
    addToFavorites: (String, String, String) -> Unit,
    removeFromFavorites: (String) -> Unit,
    onSearchStringChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    currentSearchString: String,
    isSelected: (String) -> Boolean,
    onSearchOptionClick: (String) -> Unit,
    onClickBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        StandardToolbar(
            navigate = {
                onClickBack()
            },
            title = {
                Text(text = "Search", fontSize = 18.sp)
            },
            showBackArrow = true,
            navActions = {
            }
        )

        SearchOptionsComponent(
            options = searchOptions,
            onClick = onSearchOptionClick,
            isSelected = isSelected
        )

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            currentSearchString = currentSearchString,
            onSearchStringChange = onSearchStringChange,
            onSearch = onSearch
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchState.searchData) { searchData ->
                    OnlineMealItem(
                        meal = searchData,
                        onClick = onMealClick,
                        addToFavorites = addToFavorites,
                        removeFromFavorites = removeFromFavorites
                    )
                }
            }

            // Loading data
            if (searchState.isLoading) {
                LoadingStateComponent()
            }

            // An Error has occurred
            if (!searchState.isLoading && searchState.error != null) {
                ErrorStateComponent(errorMessage = searchState.error)
            }

            // Loaded Data but the list is empty
            if (!searchState.isLoading && searchState.error == null && searchState.searchData.isEmpty()) {
                EmptyStateComponent()
            }
        }
    }
}

private val searchOptions = listOf("Meal Name", "Ingredient", "Meal Category")

@Composable
fun SearchOptionsComponent(
    options: List<String>,
    onClick: (String) -> Unit,
    isSelected: (String) -> Boolean
) {
    LazyRow {
        items(options) { option ->
            SearchOption(
                option = option,
                onClick = onClick,
                isSelected = isSelected
            )
        }
    }
}

@Composable
fun SearchOption(option: String, onClick: (String) -> Unit, isSelected: (String) -> Boolean) {
    Card(
        Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(4.dp)
            .clickable {
                onClick(option)
            },
        shape = RoundedCornerShape(70.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected(option)) {
                androidx.compose.material3.MaterialTheme.colorScheme.primary
            } else {
                androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = option,
                textAlign = TextAlign.Center,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected(option)) {
                    androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                } else {
                    androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {},
    onSearchStringChange: (String) -> Unit,
    currentSearchString: String
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = currentSearchString,
        onValueChange = {
            onSearchStringChange(it)
        },
        placeholder = {
            Text(text = "Search...")
        },
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, CircleShape)
            .background(Color.Transparent, CircleShape),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            autoCorrect = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions {
            keyboardController?.hide()
            onSearch(currentSearchString)
        },
        colors = TextFieldDefaults.textFieldColors(
            disabledTextColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        maxLines = 1,
        singleLine = true,
        trailingIcon = {
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    onSearch(currentSearchString)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
fun OnlineMealItem(
    meal: OnlineMeal,
    onClick: (String) -> Unit,
    addToFavorites: (String, String, String) -> Unit,
    removeFromFavorites: (String) -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val isFavorite = viewModel.isOnlineFavorite(id = meal.mealId).collectAsState().value

    Card(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .padding(vertical = 5.dp)
            .clickable {
                onClick(meal.mealId)
            },
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentDescription = meal.name,
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = meal.imageUrl)
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
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                androidx.compose.material3.IconButton(
                    onClick = {
                        if (isFavorite) {
                            removeFromFavorites(meal.mealId)
                        } else {
                            addToFavorites(meal.mealId, meal.imageUrl, meal.name)
                        }
                    }
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
                            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

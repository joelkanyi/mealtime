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
package com.kanyideveloper.addmeal.presentation.addmeal

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kanyideveloper.addmeal.presentation.addmeal.destinations.NextAddMealScreenDestination
import com.kanyideveloper.compose_ui.components.StandardToolbar
import com.kanyideveloper.core.state.SubscriptionStatusUiState
import com.kanyideveloper.core.state.TextFieldState
import com.kanyideveloper.core.util.UiEvents
import com.kanyideveloper.mealtime.core.R
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Destination
@Composable
fun NextAddMealScreen(
    imageUri: Uri,
    mealName: String,
    category: String,
    complexity: String,
    cookingTime: Int,
    servingPeople: Int,
    navigator: AddMealNavigator,
    viewModel: AddMealsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scaffoldState = rememberScaffoldState()

    val direction = viewModel.direction.value
    val ingredient = viewModel.ingredient.value

    val isSubscribed = viewModel.isSubscribed.collectAsState().value
    val analyticsUtil = viewModel.analyticsUtil()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvents.SnackbarEvent -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = event.message)
                }
                is UiEvents.NavigationEvent -> {
                    navigator.navigateBackToHome()
                }
            }
        }
    }

    when (isSubscribed) {
        is SubscriptionStatusUiState.Success -> {
            Column(Modifier.fillMaxSize()) {
                StandardToolbar(
                    navigate = {
                        analyticsUtil.trackUserEvent("Next Add Meal Screen -Back Button clicked")
                        navigator.popBackStack()
                    },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Add meal", fontSize = 18.sp)
                            SaveTextButtonContent(
                                isLoading = viewModel.saveMeal.value.isLoading,
                                onClick = {
                                    analyticsUtil.trackUserEvent("save my meal button clicked - $mealName")
                                    keyboardController?.hide()
                                    viewModel.saveMeal(
                                        imageUri = imageUri,
                                        mealName = mealName,
                                        cookingTime = cookingTime,
                                        servingPeople = servingPeople,
                                        complexity = complexity,
                                        category = category,
                                        isSubscribed = true // isSubscribed.isSubscribed
                                    )
                                }
                            )
                        }
                    },
                    showBackArrow = true
                )

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        IngredientComponent(ingredient, viewModel, keyboardController)
                    }

                    items(viewModel.ingredientsList) { ingredient ->
                        IngredientItem(
                            ingredient = ingredient,
                            viewModel = viewModel
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        DirectionComponent(direction, viewModel, keyboardController)
                    }

                    items(viewModel.directionsList) { direction ->
                        DirectionItem(
                            direction = direction,
                            viewModel = viewModel,
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Feature in development",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun SaveTextButtonContent(isLoading: Boolean, onClick: () -> Unit) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp)
        )
    } else {
        Text(
            modifier = Modifier.clickable {
                onClick()
            },
            text = "Save",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DirectionComponent(
    direction: TextFieldState,
    viewModel: AddMealsViewModel,
    keyboardController: SoftwareKeyboardController?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Cooking Instructions",
            style = MaterialTheme.typography.labelMedium
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = direction.text,
            onValueChange = {
                viewModel.setDirectionState(it)
            },
            placeholder = {
                Text(text = "Enter instruction")
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            ),
            trailingIcon = {
                IconButton(onClick = {
                    keyboardController?.hide()
                    viewModel.insertDirections(direction.text)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            ),
            isError = direction.error != null
        )

        if (direction.error != null) {
            Text(
                text = direction.error ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun IngredientComponent(
    ingredient: TextFieldState,
    viewModel: AddMealsViewModel,
    keyboardController: SoftwareKeyboardController?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.labelMedium
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = ingredient.text,
            onValueChange = {
                viewModel.setIngredientState(it)
            },
            placeholder = {
                Text(text = "Enter ingredient")
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            ),
            trailingIcon = {
                IconButton(onClick = {
                    keyboardController?.hide()
                    viewModel.insertIngredients(ingredient.text)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            ),
            isError = ingredient.error != null
        )
        if (ingredient.error != null) {
            Text(
                text = ingredient.error ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun IngredientItem(ingredient: String, viewModel: AddMealsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(.6f)
                    .padding(8.dp),
                text = ingredient
            )

            IconButton(onClick = {
                viewModel.removeIngredient(ingredient)
            }) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun DirectionItem(direction: String, viewModel: AddMealsViewModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(.6f)
                    .padding(8.dp),
                text = direction
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = {
                    viewModel.removeDirections(direction)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null
                    )
                }

                IconButton(onClick = {
                    onClick()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_drag_handle),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

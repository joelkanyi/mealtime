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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kanyideveloper.compose_ui.components.StandardToolbar
import com.kanyideveloper.compose_ui.theme.PrimaryColor
import com.kanyideveloper.core.analytics.AnalyticsUtil
import com.kanyideveloper.mealplanner.MealPlannerNavigator
import com.ramcosta.composedestinations.annotation.Destination
import org.koin.androidx.compose.koinViewModel

@Destination
@Composable
fun NumberOfPeopleScreen(
    editMealPlanPreference: Boolean = false,
    allergies: String,
    navigator: MealPlannerNavigator,
    viewModel: SetupViewModel = koinViewModel()
) {
    val analyticsUtil = viewModel.analyticsUtil()
    NumberOfPeopleScreenContent(
        navigator = navigator,
        allergies = allergies,
        analyticsUtil = analyticsUtil,
        numberOfPeople = viewModel.selectedNumberOfPeople.value,
        numberOfPeopleChoices = viewModel.numberOfPeople,
        editMealPlanPreference = editMealPlanPreference,
        onNumberClick = { number ->
            analyticsUtil.trackUserEvent("Number of people selected - $number")
            viewModel.setSelectedNumberOfPeople(number)
        },
        isSelected = { number ->
            viewModel.selectedNumberOfPeople.value == number
        }
    )
}

@Composable
private fun NumberOfPeopleScreenContent(
    navigator: MealPlannerNavigator,
    allergies: String,
    numberOfPeople: String,
    numberOfPeopleChoices: List<String>,
    onNumberClick: (String) -> Unit,
    isSelected: (String) -> Boolean,
    editMealPlanPreference: Boolean,
    analyticsUtil: AnalyticsUtil,
) {
    Column(Modifier.fillMaxSize()) {
        StandardToolbar(
            navigate = {
                analyticsUtil.trackUserEvent("Back button clicked from number of people screen")
                navigator.popBackStack()
            },
            title = {},
            showBackArrow = true,
            navActions = {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable {
                            analyticsUtil.trackUserEvent("Next button clicked from number of people screen")
                            navigator.openMealTypesScreen(
                                allergies = allergies,
                                noOfPeople = numberOfPeople,
                                editMealPlanPreference = editMealPlanPreference
                            )
                        },
                    text = "Next",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryColor
                )
            }
        )

        LazyVerticalGrid(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            columns = GridCells.Fixed(3)
        ) {
            item(span = { GridItemSpan(currentLineSpan = 3) }) {
                Text(
                    text = "Select Number of People",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item(span = { GridItemSpan(currentLineSpan = 3) }) {
                Text(
                    text = "How many people you are planning for",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            item(span = { GridItemSpan(currentLineSpan = 3) }) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item(span = { GridItemSpan(currentLineSpan = 3) }) {
                LazyRow(contentPadding = PaddingValues(8.dp)) {
                    items(numberOfPeopleChoices) { number ->
                        NumberOfPeopleItemCard(
                            numberOfPeople = number,
                            isSelected = isSelected,
                            onClick = {
                                onNumberClick(number)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumberOfPeopleItemCard(
    numberOfPeople: String,
    onClick: () -> Unit,
    isSelected: (String) -> Boolean
) {
    Card(
        Modifier
            .width(70.dp)
            .height(70.dp)
            .padding(4.dp)
            .clickable {
                onClick()
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected(numberOfPeople)) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numberOfPeople,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected(numberOfPeople)) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

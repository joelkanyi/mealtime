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
package com.kanyideveloper.mealplanner.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.joelkanyi.shared.core.domain.CoreMeal
import com.kanyideveloper.compose_ui.theme.PrimaryColor
import com.kanyideveloper.compose_ui.theme.Shapes
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.mealtime.core.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanMealItem(
    meal: CoreMeal,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 160.dp,
    imageHeight: Dp = 120.dp,
    isAddingToPlan: Boolean = false,
    type: String = "",
    onMealClick: (String?, String?, Boolean) -> Unit,
    onClickAdd: (CoreMeal, String) -> Unit,
    onRemoveClick: (String?) -> Unit
) {
    Box(
        modifier = Modifier
            .width(cardWidth)
            .padding(
                horizontal = if (isAddingToPlan) {
                    2.dp
                } else {
                    0.dp
                }
            )
    ) {
        Card(
            modifier = modifier
                .width(cardWidth)
                .wrapContentHeight(),
            shape = Shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            onClick = {
                onMealClick(meal.localMealId, meal.onlineMealId, meal.onlineMealId != null)
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Image(
                    modifier = Modifier
                        .width(cardWidth)
                        .height(imageHeight),
                    contentDescription = null,
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = meal.imageUrl)
                            .apply(block = fun ImageRequest.Builder.() {
                                placeholder(R.drawable.placeholder)
                            }).build()
                    ),
                    contentScale = ContentScale.Crop
                )

                Text(
                    modifier = Modifier.padding(4.dp),
                    text = meal.name ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Box(
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.TopEnd)
                .background(
                    color = PrimaryColor,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 12.dp,
                        topEnd = 12.dp,
                        bottomEnd = 0.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = {
                if (isAddingToPlan) {
                    onClickAdd(meal, type)
                } else {
                    onRemoveClick(meal.mealPlanId)
                }
            }) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = if (isAddingToPlan) {
                        Icons.Default.Add
                    } else {
                        Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

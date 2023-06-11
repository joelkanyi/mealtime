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
package com.kanyideveloper.presentation.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.kanyideveloper.compose_ui.components.StandardToolbar
import com.kanyideveloper.compose_ui.theme.Shapes
import com.kanyideveloper.core.components.PremiumCard
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.core.state.SubscriptionStatusUiState
import com.kanyideveloper.mealtime.core.R
import com.kanyideveloper.presentation.home.composables.TabItem
import com.kanyideveloper.presentation.home.composables.Tabs
import com.ramcosta.composedestinations.annotation.Destination
import org.koin.androidx.compose.koinViewModel

interface HomeNavigator {
    fun openHome()
    fun openMealDetails(meal: Meal)
    fun openAddMeal()
    fun popBackStack()
    fun openOnlineMealDetails(mealId: String)
    fun openRandomMeals()

    fun onSearchClick()

    fun subscribe()
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Destination
@Composable
fun HomeScreen(navigator: HomeNavigator, viewModel: HomeViewModel = koinViewModel()) {
    val context = LocalContext.current
    val analyticsUtil = viewModel.analyticsUtil()
    var hasCamPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else {
            mutableStateOf(true)
        }
    }

    val notificationsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCamPermission = granted
        }
    )

    LaunchedEffect(key1 = true, block = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    })

    val isSubscribed = viewModel.isSubscribed.collectAsState().value

    if (viewModel.shouldShowSubscriptionDialog.value) {
        PremiumCard(
            onDismiss = {
                viewModel.setShowSubscriptionDialogState(false)
            },
            onClickSubscribe = {
                navigator.subscribe()
                viewModel.setShowSubscriptionDialogState(false)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (isSubscribed) {
            is SubscriptionStatusUiState.Success -> {
                Scaffold(
                    topBar = {
                        StandardToolbar(
                            navigate = {},
                            title = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Image(
                                        modifier = Modifier.size(100.dp, 100.dp),
                                        painter = painterResource(
                                            id = R.drawable.ic_meal_time_banner
                                        ),
                                        contentDescription = null
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!isSubscribed.isSubscribed) {
                                            TextButton(
                                                onClick = {
                                                    analyticsUtil.trackUserEvent(
                                                        "upgrade_to_premium_clicked"
                                                    )
                                                    viewModel.setShowSubscriptionDialogState(true)
                                                }
                                            ) {
                                                Text(
                                                    text = "Upgrade to Premium",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Card(
                                            modifier = Modifier
                                                .size(42.dp),
                                            shape = Shapes.large,
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            onClick = {
                                                analyticsUtil.trackUserEvent("search feature clicked")
                                                navigator.onSearchClick()
                                            },
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 4.dp
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    painterResource(id = R.drawable.ic_search),
                                                    contentDescription = "Search",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            showBackArrow = false,
                            navActions = {}
                        )
                    },
                    floatingActionButton = {
                        if (viewModel.isMyMeal.value) {
                            FloatingActionButton(
                                modifier = Modifier
                                    .height(50.dp),
                                shape = MaterialTheme.shapes.large,
                                containerColor = MaterialTheme.colorScheme.primary,
                                content = {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                id = R.drawable.fork_knife_thin
                                            ),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiary
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Add Meal",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onTertiary
                                        )
                                    }
                                },
                                onClick = {
                                    analyticsUtil.trackUserEvent("add my meal clicked")
                                    navigator.openAddMeal()
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    MainContent(
                        navigator = navigator,
                        isSubscribed = isSubscribed.isSubscribed,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = paddingValues),
                        onClick = { page ->
                            if (page == 0) {
                                viewModel.setIsMyMeal(false)
                            } else {
                                viewModel.setIsMyMeal(true)
                            }
                        }
                    )
                }
            }

            else -> {}
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainContent(
    navigator: HomeNavigator,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit,
    isSubscribed: Boolean
) {
    val listOfTabs = listOf(
        TabItem.Incoming(navigator = navigator),
        TabItem.Outgoing(navigator = navigator),
    )
    val pagerState = rememberPagerState()

    Column(modifier = modifier) {
        Tabs(
            tabs = listOfTabs,
            pagerState = pagerState,
            onClick = {
                onClick(it)
            }
        )
        TabContent(
            tabs = listOfTabs,
            pagerState = pagerState,
            isSubscribed = isSubscribed
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabContent(tabs: List<TabItem>, pagerState: PagerState, isSubscribed: Boolean) {
    HorizontalPager(
        count = tabs.size,
        state = pagerState,
        modifier = Modifier,
        userScrollEnabled = true
    ) { page ->
        tabs[page].screen(
            isSubscribed
        )
    }
}

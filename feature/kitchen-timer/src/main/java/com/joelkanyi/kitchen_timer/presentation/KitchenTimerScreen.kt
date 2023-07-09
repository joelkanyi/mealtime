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
package com.joelkanyi.kitchen_timer.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.joelkanyi.kitchen_timer.domain.model.KitchenTimer
import com.kanyideveloper.compose_ui.components.StandardToolbar
import com.kanyideveloper.compose_ui.theme.PrimaryColor
import com.kanyideveloper.compose_ui.theme.Shapes
import com.kanyideveloper.core.util.convertMillisecondsToTimeString
import com.kanyideveloper.core.util.minutesToMilliseconds
import com.kanyideveloper.mealtime.core.R
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination
@Composable
fun KitchenTimerScreen(viewModel: KitchenTimerViewModel = koinViewModel()) {
    val timerValue = viewModel.remainingTimerValue.observeAsState(initial = KitchenTimer()).value
    val isTimerRunning = viewModel.isTimerRunning.observeAsState(initial = false).value
    val context = LocalContext.current
    val analyticsUtil = viewModel.analyticsUtil()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember {
        SnackbarHostState()
    }

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
        analyticsUtil.trackUserEvent("open kitchen timer screen")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    })

    if (viewModel.showHowLongDialog.value) {
        HowLongDialog(
            currentTimerValue = viewModel.currentTimerValue.value,
            setCurrentTimerValue = {
                analyticsUtil.trackUserEvent("set timer value: $it")
                viewModel.setCurrentTimerValue(it)
            },
            onDismiss = {
                analyticsUtil.trackUserEvent("dismissed timer dialog")
                viewModel.setShowHowLongDialog(false)
            },
            onTimerSet = {
                analyticsUtil.trackUserEvent("timer set")
                viewModel.setShowHowLongDialog(false)
                viewModel.startTimer()
            }
        )
    }

    KitchenTimerScreenContent(
        timerValue = timerValue,
        isTimerRunning = isTimerRunning,
        originalTime = minutesToMilliseconds(viewModel.currentTimerValue.value),
        percentage = viewModel.percentage.value ?: 0f,
        snackbarHostState = snackbarHostState,
        onStop = {
            analyticsUtil.trackUserEvent("stop timer")
            viewModel.stopTimer()
        },
        onStart = {
            if (viewModel.currentTimerValue.value == 0) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Please set a timer value",
                        duration = SnackbarDuration.Short
                    )
                }
                return@KitchenTimerScreenContent
            }
            if (viewModel.isTimerRunning.value == true) {
                analyticsUtil.trackUserEvent("pause timer")
                viewModel.pauseTimer()
            } else {
                analyticsUtil.trackUserEvent("start timer")
                viewModel.startTimer()
            }
        },
        showSetTimerDialog = { viewModel.setShowHowLongDialog(true) }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun KitchenTimerScreenContent(
    timerValue: KitchenTimer,
    isTimerRunning: Boolean,
    originalTime: Long,
    percentage: Float,
    onStop: () -> Unit,
    onStart: () -> Unit,
    showSetTimerDialog: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StandardToolbar(
                navigate = {},
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Kitchen Timer",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Card(
                            modifier = Modifier
                                .size(36.dp),
                            shape = Shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            onClick = {
                                showSetTimerDialog()
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
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Timer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                showBackArrow = false,
                navActions = {}
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                TimerProgressIndicator(
                    modifier = Modifier
                        .padding(top = 32.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter),
                    timerValue = timerValue,
                    mainColor = PrimaryColor,
                    radius = 40.dp,
                    strokeWidth = 10.dp,
                    percentage = percentage,
                    originalTime = originalTime
                )

                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 56.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .size(70.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = onStop,
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
                                modifier = Modifier.size(42.dp),
                                painter = painterResource(id = R.drawable.ic_stop),
                                contentDescription = "Stop Timer",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .size(70.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = {
                            onStart()
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
                                modifier = Modifier.size(42.dp),
                                painter = painterResource(
                                    id = if (isTimerRunning) R.drawable.ic_pause else R.drawable.ic_play
                                ),
                                contentDescription = "Add Timer",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerProgressIndicator(
    percentage: Float,
    timerValue: KitchenTimer,
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
    mainColor: Color,
    strokeWidth: Dp = 6.dp,
    originalTime: Long
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .size(radius * 6.4f)
        ) {
            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Square)
            )

            drawArc(
                color = mainColor,
                startAngle = 0f,
                sweepAngle = percentage,
                useCenter = false,
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Square)
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timerValue.hour + ":" + timerValue.minute + ":" + timerValue.second,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Remaining",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Original Time: ${convertMillisecondsToTimeString(originalTime)}",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun HowLongDialog(
    onDismiss: () -> Unit,
    onTimerSet: (Int) -> Unit,
    currentTimerValue: Int,
    setCurrentTimerValue: (Int) -> Unit
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Kitchen Timer",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "How long do you want to set the time (Minutes)?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                InfiniteNumberPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    list = (0..1000).toList(),
                    firstIndex = currentTimerValue,
                    onSelect = {
                        setCurrentTimerValue(it)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onTimerSet(30) }) {
                Text(
                    text = "Set",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun InfiniteNumberPicker(
    modifier: Modifier = Modifier,
    list: List<Int>,
    firstIndex: Int,
    onSelect: (Int) -> Unit
) {
    val listState = rememberLazyListState(firstIndex)
    val coroutineScope = rememberCoroutineScope()

    val currentValue = remember {
        mutableStateOf(0)
    }

    LaunchedEffect(!listState.isScrollInProgress) {
        coroutineScope.launch {
            onSelect(currentValue.value)
            listState.animateScrollToItem(index = listState.firstVisibleItemIndex)
        }
    }

    Box(
        modifier = Modifier
            .height(106.dp)
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            items(count = Int.MAX_VALUE, itemContent = {
                val index = it % list.size
                if (it == remember { derivedStateOf { listState.firstVisibleItemIndex } }.value + 1) {
                    currentValue.value = list[index]
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    modifier = modifier.alpha(
                        if (it == remember { derivedStateOf { listState.firstVisibleItemIndex } }.value + 1) 1f else 0.3f
                    ),
                    text = list[index].toString(),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.02.em,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
            })
        }
        Spacer(
            modifier = modifier
                .height(106.dp)
                .width(120.dp)
        )
    }
}

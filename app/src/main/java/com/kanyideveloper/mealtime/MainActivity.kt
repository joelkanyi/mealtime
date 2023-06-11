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
package com.kanyideveloper.mealtime

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.joelkanyi.kitchen_timer.presentation.destinations.KitchenTimerScreenDestination
import com.kanyideveloper.compose_ui.theme.MealTimeTheme
import com.kanyideveloper.compose_ui.theme.Theme
import com.kanyideveloper.core.state.SubscriptionStatusUiState
import com.kanyideveloper.core.util.Constants.PURCHASE_ID
import com.kanyideveloper.favorites.presentation.favorites.presentation.destinations.FavoritesScreenDestination
import com.kanyideveloper.mealplanner.destinations.MealPlannerScreenDestination
import com.kanyideveloper.mealtime.component.StandardScaffold
import com.kanyideveloper.mealtime.component.navGraph
import com.kanyideveloper.mealtime.navigation.CoreFeatureNavigator
import com.kanyideveloper.mealtime.navigation.NavGraphs
import com.kanyideveloper.mealtime.navigation.scaleInEnterTransition
import com.kanyideveloper.mealtime.navigation.scaleInPopEnterTransition
import com.kanyideveloper.mealtime.navigation.scaleOutExitTransition
import com.kanyideveloper.mealtime.navigation.scaleOutPopExitTransition
import com.kanyideveloper.presentation.destinations.HomeScreenDestination
import com.kanyideveloper.settings.presentation.destinations.SettingsScreenDestination
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QEntitlement
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.NestedNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.scope.DestinationScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject
import timber.log.Timber
class MainActivity : ComponentActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateAvailable = MutableLiveData<Boolean>().apply { value = false }
    private var updateInfo: AppUpdateInfo? = null
    private val updateListener = InstallStateUpdatedListener { state: InstallState ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            appUpdateManager.completeUpdate()
        } else if (state.installStatus() == InstallStatus.INSTALLED) {
            removeInstallStateUpdateListener()
        } else if (state.installStatus() == InstallStatus.FAILED) {
            removeInstallStateUpdateListener()
        } else if (state.installStatus() == InstallStatus.UNKNOWN) {
            removeInstallStateUpdateListener()
        }
    }
    val viewModel: MainViewModel by inject()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition(
                condition = {
                    viewModel.subscriptionStatusUiState
                }
            )
        }

        try {
            appUpdateManager = AppUpdateManagerFactory.create(this)
            appUpdateManager.registerListener(updateListener)
            checkForUpdate()
        } catch (e: Exception) {
            Timber.e("Try check update info exception: ${e.message}")
        }

        setContent {
            val isLogged = viewModel.user.value != null
            val isSubscribed = viewModel.isSubscribed.collectAsState().value

            val themeValue by viewModel.theme.collectAsState(
                initial = Theme.FOLLOW_SYSTEM.themeValue,
                context = Dispatchers.Main.immediate
            )

            if (isLogged) {
                showReviewDialog()
            }

            when (isSubscribed) {
                SubscriptionStatusUiState.Loading -> {}
                is SubscriptionStatusUiState.Success -> {
                    MealTimeTheme(
                        theme = themeValue
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            val navController = rememberAnimatedNavController()
                            val newBackStackEntry by navController.currentBackStackEntryAsState()
                            val route = newBackStackEntry?.destination?.route

                            val bottomBarItems = if (isSubscribed.isSubscribed) {
                                listOf(
                                    BottomNavItem.Home,
                                    BottomNavItem.KitchenTimer,
                                    BottomNavItem.MealPlanner,
                                    BottomNavItem.Favorites,
                                    BottomNavItem.Settings
                                )
                            } else {
                                listOf(
                                    BottomNavItem.Home,
                                    BottomNavItem.MealPlanner,
                                    BottomNavItem.Favorites,
                                    BottomNavItem.Settings
                                )
                            }

                            StandardScaffold(
                                navController = navController,
                                items = bottomBarItems,
                                isLoggedIn = isLogged,
                                showBottomBar = route in listOf(
                                    "home/${HomeScreenDestination.route}",
                                    "kitchen-timer/${KitchenTimerScreenDestination.route}",
                                    "meal_planner/${MealPlannerScreenDestination.route}",
                                    "favorites/${FavoritesScreenDestination.route}",
                                    "settings/${SettingsScreenDestination.route}"
                                )
                            ) { innerPadding ->
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    AppNavigation(
                                        navController = navController,
                                        isLoggedIn = isLogged,
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        subscribe = {
                                            subscribeUser(onSuccess = {
                                                Qonversion.shared.syncPurchases()
                                                viewModel.updateSubscriptionStatus()
                                            })
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun subscribeUser(onSuccess: () -> Unit) {
        Qonversion.shared.purchase(
            this@MainActivity,
            id = PURCHASE_ID,
            object : QonversionEntitlementsCallback {
                override fun onError(error: QonversionError) {
                    Toast
                        .makeText(
                            this@MainActivity,
                            "Purchase failed: ${error.description}, ${error.additionalMessage}",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }

                override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                    onSuccess()
                    Toast
                        .makeText(
                            this@MainActivity,
                            "Purchase successful",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            }
        )
    }

    private fun DestinationScope<*>.currentNavigator(
        isLoggedIn: Boolean,
        subscribe: () -> Unit
    ): CoreFeatureNavigator {
        return CoreFeatureNavigator(
            navGraph = navBackStackEntry.destination.navGraph(isLoggedIn),
            navController = navController,
            subscribe = subscribe
        )
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @ExperimentalAnimationApi
    @Composable
    internal fun AppNavigation(
        subscribe: () -> Unit,
        navController: NavHostController,
        modifier: Modifier = Modifier,
        isLoggedIn: Boolean
    ) {
        val navHostEngine = rememberAnimatedNavHostEngine(
            navHostContentAlignment = Alignment.TopCenter,
            rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING, // default `rootDefaultAnimations` means no animations
            defaultAnimationsForNestedNavGraph = mapOf(
                NavGraphs.home to NestedNavGraphDefaultAnimations(
                    enterTransition = {
                        scaleInEnterTransition()
                    },
                    exitTransition = {
                        scaleOutExitTransition()
                    },
                    popEnterTransition = {
                        scaleInPopEnterTransition()
                    },
                    popExitTransition = {
                        scaleOutPopExitTransition()
                    }
                ),
                NavGraphs.search to NestedNavGraphDefaultAnimations(
                    enterTransition = {
                        scaleInEnterTransition()
                    },
                    exitTransition = {
                        scaleOutExitTransition()
                    },
                    popEnterTransition = {
                        scaleInPopEnterTransition()
                    },
                    popExitTransition = {
                        scaleOutPopExitTransition()
                    }
                ),
                NavGraphs.favorites to NestedNavGraphDefaultAnimations(
                    enterTransition = {
                        scaleInEnterTransition()
                    },
                    exitTransition = {
                        scaleOutExitTransition()
                    },
                    popEnterTransition = {
                        scaleInPopEnterTransition()
                    },
                    popExitTransition = {
                        scaleOutPopExitTransition()
                    }
                ),
                NavGraphs.settings to NestedNavGraphDefaultAnimations(
                    enterTransition = {
                        scaleInEnterTransition()
                    },
                    exitTransition = {
                        scaleOutExitTransition()
                    },
                    popEnterTransition = {
                        scaleInPopEnterTransition()
                    },
                    popExitTransition = {
                        scaleOutPopExitTransition()
                    }
                ),
                NavGraphs.mealPlanner to NestedNavGraphDefaultAnimations(
                    enterTransition = {
                        scaleInEnterTransition()
                    },
                    exitTransition = {
                        scaleOutExitTransition()
                    },
                    popEnterTransition = {
                        scaleInPopEnterTransition()
                    },
                    popExitTransition = {
                        scaleOutPopExitTransition()
                    }
                ),
                NavGraphs.auth to NestedNavGraphDefaultAnimations(
                    enterTransition = {
                        scaleInEnterTransition()
                    },
                    exitTransition = {
                        scaleOutExitTransition()
                    },
                    popEnterTransition = {
                        scaleInPopEnterTransition()
                    },
                    popExitTransition = {
                        scaleOutPopExitTransition()
                    }
                )
            )
        )

        DestinationsNavHost(
            engine = navHostEngine,
            navController = navController,
            navGraph = NavGraphs.root(isLoggedIn = isLoggedIn),
            modifier = modifier,
            dependenciesContainerBuilder = {
                dependency(
                    currentNavigator(
                        subscribe = subscribe,
                        isLoggedIn = isLoggedIn
                    )
                )
            }
        )
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            Timber.e("Update info: ${it.availableVersionCode()}")
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                updateInfo = it
                updateAvailable.value = true
                startForInAppUpdate(updateInfo)
            } else {
                updateAvailable.value = false
            }
        }
    }

    private fun startForInAppUpdate(it: AppUpdateInfo?) {
        appUpdateManager.startUpdateFlowForResult(it!!, AppUpdateType.FLEXIBLE, this, 1101)
    }

    private fun removeInstallStateUpdateListener() {
        appUpdateManager.unregisterListener(updateListener)
    }

    override fun onStop() {
        super.onStop()
        removeInstallStateUpdateListener()
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                } else if (appUpdateInfo.installStatus() == InstallStatus.INSTALLED) {
                    removeInstallStateUpdateListener()
                } else if (appUpdateInfo.installStatus() == InstallStatus.FAILED) {
                    removeInstallStateUpdateListener()
                } else if (appUpdateInfo.installStatus() == InstallStatus.UNKNOWN) {
                    removeInstallStateUpdateListener()
                }
            }
    }

    private fun showReviewDialog() {
        val reviewManager = ReviewManagerFactory.create(applicationContext)
        reviewManager.requestReviewFlow().addOnCompleteListener {
            if (it.isSuccessful) {
                reviewManager.launchReviewFlow(this, it.result)
            }
        }
    }
}

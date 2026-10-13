package com.example.revlens.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.revlens.ui.components.BottomNavItem
import com.example.revlens.ui.components.RevLensBottomBar
import com.example.revlens.ui.onboarding.OnboardingScreen
import com.example.revlens.ui.screens.detailscreens.BusinessSetupScreen
import com.example.revlens.ui.screens.detailscreens.GoalPlannerScreen
import com.example.revlens.ui.screens.detailscreens.SaaSMetricsScreen
import com.example.revlens.ui.screens.detailscreens.ScenarioComparisonScreen
import com.example.revlens.ui.screens.navbarscreens.DashboardScreen
import com.example.revlens.ui.screens.navbarscreens.ForecastScreen
import com.example.revlens.ui.screens.navbarscreens.ScenarioBuilderScreen
import com.example.revlens.ui.screens.navbarscreens.SettingsScreen
import com.example.revlens.ui.screens.navbarscreens.SimulatorScreen

private const val ANIM_DURATION = 350
private val ANIM_EASING = FastOutSlowInEasing

@Composable
fun RevLensNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem(
            "Dashboard", Icons.Filled.Dashboard, DashboardRoute::class.qualifiedName ?: ""
        ),
        BottomNavItem(
            "Simulator", Icons.Filled.Tune, PricingSimulatorRoute::class.qualifiedName ?: ""
        ),
        BottomNavItem(
            "Scenarios", Icons.Filled.Layers, ScenarioBuilderRoute::class.qualifiedName ?: ""
        ),
        BottomNavItem(
            "Forecast", Icons.AutoMirrored.Filled.TrendingUp,
            ForecastRoute::class.qualifiedName ?: ""
        ),
        BottomNavItem("Profile", Icons.Filled.Person, SettingsRoute::class.qualifiedName ?: "")
    )

    val mainTabRoutes = setOf(
        DashboardRoute::class.qualifiedName,
        PricingSimulatorRoute::class.qualifiedName,
        ScenarioBuilderRoute::class.qualifiedName,
        ForecastRoute::class.qualifiedName,
        SettingsRoute::class.qualifiedName
    )

    val showBottomBar = mainTabRoutes.any { prefix ->
        prefix != null && currentDestination?.route?.startsWith(prefix) == true
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                RevLensBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentDestination?.route,
                    onNavigate = { route ->
                        val target: Any = when {
                            route.contains("DashboardRoute") -> DashboardRoute
                            route.contains("PricingSimulatorRoute") -> PricingSimulatorRoute(0)
                            route.contains("ScenarioBuilderRoute") -> ScenarioBuilderRoute
                            route.contains("ForecastRoute") -> ForecastRoute
                            route.contains("SettingsRoute") -> SettingsRoute
                            else -> DashboardRoute
                        }
                        navController.navigate(target) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = OnboardingRoute,
            modifier = modifier.padding(innerPadding),
            enterTransition = {
                val isTargetMainTab = mainTabRoutes.any { it != null && targetState.destination.route?.startsWith(it) == true }
                val isInitialMainTab = mainTabRoutes.any { it != null && initialState.destination.route?.startsWith(it) == true }
                if (isTargetMainTab && isInitialMainTab) {
                    fadeIn(animationSpec = tween(ANIM_DURATION))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                    )
                }
            },
            exitTransition = {
                val isTargetMainTab = mainTabRoutes.any { it != null && targetState.destination.route?.startsWith(it) == true }
                val isInitialMainTab = mainTabRoutes.any { it != null && initialState.destination.route?.startsWith(it) == true }
                if (isTargetMainTab && isInitialMainTab) {
                    fadeOut(animationSpec = tween(ANIM_DURATION))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                    )
                }
            },
            popEnterTransition = {
                val isTargetMainTab = mainTabRoutes.any { it != null && targetState.destination.route?.startsWith(it) == true }
                val isInitialMainTab = mainTabRoutes.any { it != null && initialState.destination.route?.startsWith(it) == true }
                if (isTargetMainTab && isInitialMainTab) {
                    fadeIn(animationSpec = tween(ANIM_DURATION))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                    )
                }
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                )
            }
        ) {
            composable<OnboardingRoute> {
                OnboardingScreen(
                    onFinishOnboarding = {
                        navController.navigate(DashboardRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    }
                )
            }

            composable<DashboardRoute> {
                DashboardScreen(
                    onNavigateToInput = {
                        navController.navigate(InputDataRoute)
                    },
                    onNavigateToPricing = {
                        navController.navigate(PricingSimulatorRoute(initialTab = 0))
                    },
                    onNavigateToWhatIf = {
                        navController.navigate(PricingSimulatorRoute(initialTab = 1))
                    },
                    onNavigateToBreakEven = {
                        navController.navigate(PricingSimulatorRoute(initialTab = 2))
                    },
                    onNavigateToSettings = {
                        navController.navigate(SettingsRoute)
                    },
                    onNavigateToMetrics = {
                        navController.navigate(MetricsRoute)
                    },
                    onNavigateToGoalPlanner = {
                        navController.navigate(GoalPlannerRoute)
                    }
                )
            }

            composable<InputDataRoute> {
                BusinessSetupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<PricingSimulatorRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<PricingSimulatorRoute>()
                SimulatorScreen(
                    initialTab = route.initialTab,
                    onNavigateToMetrics = { navController.navigate(MetricsRoute) }
                )
            }

            composable<ScenarioBuilderRoute> {
                ScenarioBuilderScreen(
                    onNavigateToCreate = { navController.navigate(PricingSimulatorRoute(initialTab = 0)) },
                    onNavigateToCompare = { navController.navigate(ScenarioComparisonRoute) },
                    onNavigateToMetrics = { navController.navigate(MetricsRoute) }
                )
            }

            composable<ScenarioComparisonRoute> {
                ScenarioComparisonScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<ForecastRoute> {
                ForecastScreen(
                    onNavigateToGoalPlanner = { navController.navigate(GoalPlannerRoute) },
                    onNavigateToMetrics = { navController.navigate(MetricsRoute) }
                )
            }

            composable<GoalPlannerRoute> {
                GoalPlannerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<MetricsRoute> {
                SaaSMetricsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

package com.example.revlens.ui.navigation

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
import com.example.revlens.ui.breakeven.BreakEvenScreen
import com.example.revlens.ui.components.BottomNavItem
import com.example.revlens.ui.components.RevLensBottomBar
import com.example.revlens.ui.dashboard.DashboardScreen
import com.example.revlens.ui.forecast.ForecastScreen
import com.example.revlens.ui.goal.GoalPlannerScreen
import com.example.revlens.ui.metrics.SaaSMetricsScreen
import com.example.revlens.ui.scenario.ScenarioBuilderScreen
import com.example.revlens.ui.settings.SettingsScreen
import com.example.revlens.ui.setup.BusinessSetupScreen
import com.example.revlens.ui.whatif.WhatIfScreen

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
        BottomNavItem("Simulator", Icons.Filled.Tune, WhatIfRoute::class.qualifiedName ?: ""),
        BottomNavItem(
            "Scenarios", Icons.Filled.Layers, ScenarioBuilderRoute::class.qualifiedName ?: ""
        ),
        BottomNavItem(
            "Forecast", Icons.AutoMirrored.Filled.TrendingUp,
            ForecastRoute::class.qualifiedName ?: ""
        ),
        BottomNavItem("Profile", Icons.Filled.Person, SettingsRoute::class.qualifiedName ?: "")
    )

    // Only show bottom bar on root destinations
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.route == item.route
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                RevLensBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentDestination?.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = modifier.padding(innerPadding)
        ) {
            composable<DashboardRoute> {
                DashboardScreen(
                    onNavigateToInput = {
                        navController.navigate(InputDataRoute)
                    },
                    onNavigateToWhatIf = {
                        navController.navigate(WhatIfRoute)
                    },
                    onNavigateToBreakEven = {
                        navController.navigate(BreakEvenRoute)
                    },
                    onNavigateToSettings = {
                        navController.navigate(SettingsRoute)
                    },
                    onNavigateToMetrics = {
                        navController.navigate(MetricsRoute)
                    }
                )
            }

            composable<InputDataRoute> {
                BusinessSetupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<WhatIfRoute> {
                WhatIfScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMetrics = { navController.navigate(MetricsRoute) }
                )
            }

            composable<BreakEvenRoute> {
                BreakEvenScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<ScenarioBuilderRoute> {
                ScenarioBuilderScreen(
                    onNavigateToCreate = { navController.navigate(WhatIfRoute) },
                    onNavigateToCompare = { navController.navigate(ScenarioComparisonRoute) },
                    onNavigateToMetrics = { navController.navigate(MetricsRoute) }
                )
            }

            composable<ScenarioComparisonRoute> {
                // Placeholder for Scenario Comparison Screen
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
                SaaSMetricsScreen()
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

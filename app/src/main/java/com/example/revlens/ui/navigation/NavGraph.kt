package com.example.revlens.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.revlens.ui.breakeven.BreakEvenScreen
import com.example.revlens.ui.dashboard.DashboardScreen
import com.example.revlens.ui.forecast.ForecastScreen
import com.example.revlens.ui.goal.GoalPlannerScreen
import com.example.revlens.ui.scenario.ScenarioBuilderScreen
import com.example.revlens.ui.setup.BusinessSetupScreen
import com.example.revlens.ui.whatif.WhatIfScreen
import com.example.revlens.ui.metrics.SaaSMetricsScreen
import com.example.revlens.ui.settings.SettingsScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.revlens.ui.components.BottomNavItem
import com.example.revlens.ui.components.RevLensBottomBar

@Composable
fun RevLensNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, DashboardRoute::class.qualifiedName ?: ""),
        BottomNavItem("Simulate", Icons.Filled.Build, WhatIfRoute::class.qualifiedName ?: ""),
        BottomNavItem("Scenarios", Icons.Filled.Assessment, ScenarioBuilderRoute::class.qualifiedName ?: ""),
        BottomNavItem("Forecast", Icons.Filled.Timeline, ForecastRoute::class.qualifiedName ?: ""),
        BottomNavItem("Metrics", Icons.Filled.ShowChart, MetricsRoute::class.qualifiedName ?: "")
    )

    // Only show bottom bar on root destinations
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.route == item.route
    }

    Scaffold(
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
                    onNavigateBack = { navController.popBackStack() }
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
                    onNavigateToCompare = { navController.navigate(ScenarioComparisonRoute) }
                )
            }

            composable<ScenarioComparisonRoute> {
                // Placeholder for Scenario Comparison Screen
            }

            composable<ForecastRoute> {
                ForecastScreen(
                    onNavigateToGoalPlanner = { navController.navigate(GoalPlannerRoute) }
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

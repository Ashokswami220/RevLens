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
import com.example.revlens.ui.screens.detailscreens.BreakEvenScreen
import com.example.revlens.ui.components.BottomNavItem
import com.example.revlens.ui.components.RevLensBottomBar
import com.example.revlens.ui.screens.navbarscreens.DashboardScreen
import com.example.revlens.ui.screens.navbarscreens.ForecastScreen
import com.example.revlens.ui.screens.detailscreens.GoalPlannerScreen
import com.example.revlens.ui.screens.detailscreens.SaaSMetricsScreen
import com.example.revlens.ui.onboarding.OnboardingScreen
import com.example.revlens.ui.screens.navbarscreens.SimulatorScreen
import com.example.revlens.ui.screens.navbarscreens.ScenarioBuilderScreen
import com.example.revlens.ui.screens.detailscreens.ScenarioComparisonScreen
import com.example.revlens.ui.screens.navbarscreens.SettingsScreen
import com.example.revlens.ui.screens.detailscreens.BusinessSetupScreen

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

    val showBottomBar = currentDestination?.route != OnboardingRoute::class.qualifiedName && 
                        currentDestination?.route != InputDataRoute::class.qualifiedName

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
            startDestination = OnboardingRoute,
            modifier = modifier.padding(innerPadding)
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
                    onNavigateToWhatIf = {
                        navController.navigate(PricingSimulatorRoute)
                    },
                    onNavigateToBreakEven = {
                        navController.navigate(PricingSimulatorRoute)
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

            composable<PricingSimulatorRoute> {
                SimulatorScreen(
                    onNavigateToMetrics = { navController.navigate(MetricsRoute) }
                )
            }

            composable<ScenarioBuilderRoute> {
                ScenarioBuilderScreen(
                    onNavigateToCreate = { navController.navigate(PricingSimulatorRoute) },
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

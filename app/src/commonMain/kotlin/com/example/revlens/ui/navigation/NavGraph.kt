package com.example.revlens.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.BottomNavItem
import com.example.revlens.ui.components.RevLensBottomBar
import com.example.revlens.ui.onboarding.IntroScreen
import com.example.revlens.ui.onboarding.OnboardingDataScreen
import com.example.revlens.ui.screens.detailscreens.BusinessSetupScreen
import com.example.revlens.ui.screens.detailscreens.GoalPlannerScreen
import com.example.revlens.ui.screens.detailscreens.SaaSMetricsScreen
import com.example.revlens.ui.screens.detailscreens.ScenarioComparisonScreen
import com.example.revlens.ui.screens.navbarscreens.DashboardScreen
import com.example.revlens.ui.screens.navbarscreens.ForecastScreen
import com.example.revlens.ui.screens.navbarscreens.ScenarioBuilderScreen
import com.example.revlens.ui.screens.navbarscreens.SettingsScreen
import com.example.revlens.ui.screens.navbarscreens.SimulatorScreen
import com.example.revlens.ui.theme.RevLensTheme
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import com.example.revlens.ui.components.LocalAppNavigator
import com.example.revlens.ui.components.LocalCurrentTab

private const val ANIM_DURATION = 350
private val ANIM_EASING = FastOutSlowInEasing

private fun isMainTab(route: String?): Boolean {
    if (route == null) return false
    return route.contains("Dashboard") ||
            route.contains("PricingSimulator") ||
            route.contains("Simulator") ||
            (route.contains("Scenario") && !route.contains("ScenarioComparison")) ||
            (route.contains("Forecast") && !route.contains("GoalPlanner")) ||
            route.contains("Settings") ||
            route.contains("Profile")
}

@Composable
fun RevLensNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = koinViewModel()
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

    if (isOnboardingCompleted == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(RevLensTheme.colors.background)
        )
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: ""

    val currentTab = when {
        currentDestination?.hasRoute<DashboardRoute>() == true || currentRoute.contains("Dashboard") -> "Home"
        currentDestination?.hasRoute<PricingSimulatorRoute>() == true || currentRoute.contains("PricingSimulator") || currentRoute.contains("Simulator") -> "Simulator"
        currentDestination?.hasRoute<ScenarioBuilderRoute>() == true || (currentRoute.contains("ScenarioBuilder") || (currentRoute.contains("Scenario") && !currentRoute.contains("ScenarioComparison"))) -> "Scenarios"
        currentDestination?.hasRoute<ForecastRoute>() == true || (currentRoute.contains("Forecast") && !currentRoute.contains("GoalPlanner")) -> "Forecast"
        currentDestination?.hasRoute<SettingsRoute>() == true || currentRoute.contains("Settings") || currentRoute.contains("Profile") -> "Profile"
        else -> null
    }

    val showBottomBar = currentTab != null

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Filled.Dashboard, "Home"),
        BottomNavItem("Simulator", Icons.Filled.Tune, "Simulator"),
        BottomNavItem("Scenarios", Icons.Filled.Layers, "Scenarios"),
        BottomNavItem("Forecast", Icons.AutoMirrored.Filled.TrendingUp, "Forecast"),
        BottomNavItem("Profile", Icons.Filled.Person, "Profile")
    )

    val navigateToTab: (String) -> Unit = { tabKey ->
        if (tabKey == "Home" || tabKey == "Dashboard") {
            navController.popBackStack(DashboardRoute, inclusive = false)
            BrowserRouter.updateUrl("home")
        } else {
            val target: Any = when (tabKey) {
                "Simulator" -> PricingSimulatorRoute(0)
                "Scenarios" -> ScenarioBuilderRoute
                "Forecast" -> ForecastRoute
                "Profile", "Settings" -> SettingsRoute
                else -> DashboardRoute
            }
            navController.navigate(target) {
                launchSingleTop = true
            }
            val slug = when (tabKey) {
                "Simulator" -> "simulator"
                "Scenarios" -> "scenarios"
                "Forecast" -> "forecast"
                "Profile", "Settings" -> "settings"
                else -> "home"
            }
            BrowserRouter.updateUrl(slug)
        }
    }

    LaunchedEffect(currentRoute) {
        val slug = when {
            currentDestination?.hasRoute<IntroRoute>() == true -> "intro"
            currentDestination?.hasRoute<OnboardingRoute>() == true -> "onboarding"
            currentDestination?.hasRoute<DashboardRoute>() == true || currentRoute.contains("Dashboard") -> "home"
            currentDestination?.hasRoute<PricingSimulatorRoute>() == true || currentRoute.contains("Simulator") -> "simulator"
            currentDestination?.hasRoute<ScenarioBuilderRoute>() == true || currentRoute.contains("ScenarioBuilder") -> "scenarios"
            currentDestination?.hasRoute<ForecastRoute>() == true || currentRoute.contains("Forecast") -> "forecast"
            currentDestination?.hasRoute<SettingsRoute>() == true || currentRoute.contains("Settings") -> "settings"
            currentDestination?.hasRoute<MetricsRoute>() == true || currentRoute.contains("Metrics") -> "metrics"
            currentDestination?.hasRoute<GoalPlannerRoute>() == true || currentRoute.contains("GoalPlanner") -> "goals"
            currentDestination?.hasRoute<InputDataRoute>() == true || currentRoute.contains("InputData") -> "input"
            currentDestination?.hasRoute<ScenarioComparisonRoute>() == true || currentRoute.contains("Comparison") -> "compare"
            else -> null
        }
        if (slug != null) {
            BrowserRouter.updateUrl(slug)
        }
    }

    LaunchedEffect(Unit) {
        val initial = BrowserRouter.getInitialRoute()
        if (initial != null && isOnboardingCompleted == true) {
            when (initial.lowercase()) {
                "simulator" -> navController.navigate(PricingSimulatorRoute(0)) { launchSingleTop = true }
                "scenarios" -> navController.navigate(ScenarioBuilderRoute) { launchSingleTop = true }
                "forecast" -> navController.navigate(ForecastRoute) { launchSingleTop = true }
                "settings", "profile" -> navController.navigate(SettingsRoute) { launchSingleTop = true }
                "metrics" -> navController.navigate(MetricsRoute) { launchSingleTop = true }
                "goals", "goalplanner" -> navController.navigate(GoalPlannerRoute) { launchSingleTop = true }
                "input" -> navController.navigate(InputDataRoute) { launchSingleTop = true }
                "compare" -> navController.navigate(ScenarioComparisonRoute) { launchSingleTop = true }
                "home", "dashboard" -> navController.popBackStack(DashboardRoute, inclusive = false)
            }
        }
        BrowserRouter.onUrlChange { newRoute ->
            if (isOnboardingCompleted == true) {
                when (newRoute.lowercase()) {
                    "simulator" -> navController.navigate(PricingSimulatorRoute(0)) { launchSingleTop = true }
                    "scenarios" -> navController.navigate(ScenarioBuilderRoute) { launchSingleTop = true }
                    "forecast" -> navController.navigate(ForecastRoute) { launchSingleTop = true }
                    "settings", "profile" -> navController.navigate(SettingsRoute) { launchSingleTop = true }
                    "metrics" -> navController.navigate(MetricsRoute) { launchSingleTop = true }
                    "goals", "goalplanner" -> navController.navigate(GoalPlannerRoute) { launchSingleTop = true }
                    "input" -> navController.navigate(InputDataRoute) { launchSingleTop = true }
                    "compare" -> navController.navigate(ScenarioComparisonRoute) { launchSingleTop = true }
                    "home", "dashboard" -> navController.popBackStack(DashboardRoute, inclusive = false)
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalAppNavigator provides navigateToTab,
        LocalCurrentTab provides (currentTab ?: "Home")
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    RevLensBottomBar(
                        items = bottomNavItems,
                        currentRoute = currentTab,
                        onNavigate = navigateToTab
                    )
                }
            }
        ) { innerPadding ->
            val isNonDashboardTab = currentTab != null && currentTab != "Home" && currentTab != "Dashboard"

            PlatformBackHandler(enabled = isNonDashboardTab) {
                navController.navigate(DashboardRoute) {
                    popUpTo(DashboardRoute) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
            NavHost(
                navController = navController,
                startDestination = if (isOnboardingCompleted == true) DashboardRoute else IntroRoute,
                modifier = modifier.padding(innerPadding),
            enterTransition = {
                val isTargetMainTab = isMainTab(targetState.destination.route)
                val isInitialMainTab = isMainTab(initialState.destination.route)
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
                val isTargetMainTab = isMainTab(targetState.destination.route)
                val isInitialMainTab = isMainTab(initialState.destination.route)
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
                val isTargetMainTab = isMainTab(targetState.destination.route)
                val isInitialMainTab = isMainTab(initialState.destination.route)
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
                val isTargetMainTab = isMainTab(targetState.destination.route)
                val isInitialMainTab = isMainTab(initialState.destination.route)
                if (isTargetMainTab && isInitialMainTab) {
                    fadeOut(animationSpec = tween(ANIM_DURATION))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                    )
                }
            }
        ) {
            composable<IntroRoute> {
                IntroScreen(
                    onGetStarted = {
                        navController.navigate(OnboardingRoute)
                    }
                )
            }

            composable<OnboardingRoute> {
                OnboardingDataScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onFinishOnboarding = { newProfile ->
                        viewModel.completeOnboarding(newProfile)
                        navController.navigate(DashboardRoute) {
                            popUpTo(IntroRoute) { inclusive = true }
                        }
                        BrowserRouter.updateUrl("home")
                    }
                )
            }

            composable<DashboardRoute> {
                DashboardScreen(
                    onNavigateToInput = {
                        navController.navigate(InputDataRoute)
                    },
                    onNavigateToPricing = {
                        navController.navigate(PricingSimulatorRoute(initialTab = 0)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToWhatIf = {
                        navController.navigate(PricingSimulatorRoute(initialTab = 1)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToBreakEven = {
                        navController.navigate(PricingSimulatorRoute(initialTab = 2)) {
                            launchSingleTop = true
                        }
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
                    onNavigateToCreate = {
                        navController.navigate(PricingSimulatorRoute(initialTab = 0)) {
                            launchSingleTop = true
                        }
                    },
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
                SettingsScreen()
            }
        }
    }
}
}



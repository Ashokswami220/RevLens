package com.example.revlens.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.BottomNavItem
import com.example.revlens.ui.components.LocalAppNavigator
import com.example.revlens.ui.components.LocalCurrentTab
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
import com.example.revlens.ui.screens.workspace.CreateWorkspaceScreen
import com.example.revlens.ui.screens.workspace.WorkspaceManagementScreen
import com.example.revlens.ui.theme.RevLensTheme
import org.koin.compose.viewmodel.koinViewModel

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
        currentDestination?.hasRoute<DashboardRoute>() == true || currentRoute.contains(
            "Dashboard"
        ) -> "Home"

        currentDestination?.hasRoute<PricingSimulatorRoute>() == true || currentRoute.contains(
            "PricingSimulator"
        ) || currentRoute.contains("Simulator") -> "Simulator"

        currentDestination?.hasRoute<ScenarioBuilderRoute>() == true || (currentRoute.contains(
            "ScenarioBuilder"
        ) || (currentRoute.contains("Scenario") && !currentRoute.contains(
            "ScenarioComparison"
        ))) -> "Scenarios"

        currentDestination?.hasRoute<ForecastRoute>() == true || (currentRoute.contains(
            "Forecast"
        ) && !currentRoute.contains("GoalPlanner")) -> "Forecast"

        currentDestination?.hasRoute<SettingsRoute>() == true || currentRoute.contains(
            "Settings"
        ) || currentRoute.contains("Profile") -> "Profile"

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
            currentDestination?.hasRoute<DashboardRoute>() == true || currentRoute.contains(
                "Dashboard"
            ) -> "home"

            currentDestination?.hasRoute<PricingSimulatorRoute>() == true || currentRoute.contains(
                "Simulator"
            ) -> "simulator"

            currentDestination?.hasRoute<ScenarioBuilderRoute>() == true || currentRoute.contains(
                "ScenarioBuilder"
            ) -> "scenarios"

            currentDestination?.hasRoute<ForecastRoute>() == true || currentRoute.contains(
                "Forecast"
            ) -> "forecast"

            currentDestination?.hasRoute<SettingsRoute>() == true || currentRoute.contains(
                "Settings"
            ) -> "settings"

            currentDestination?.hasRoute<MetricsRoute>() == true || currentRoute.contains(
                "Metrics"
            ) -> "metrics"

            currentDestination?.hasRoute<GoalPlannerRoute>() == true || currentRoute.contains(
                "GoalPlanner"
            ) -> "goals"

            currentDestination?.hasRoute<InputDataRoute>() == true || currentRoute.contains(
                "InputData"
            ) -> "input"

            currentDestination?.hasRoute<ScenarioComparisonRoute>() == true || currentRoute.contains(
                "Comparison"
            ) -> "compare"

            currentDestination?.hasRoute<ReportsRoute>() == true || currentRoute.contains(
                "Reports"
            ) -> "reports"

            currentDestination?.hasRoute<SecurityRoute>() == true || currentRoute.contains(
                "Security"
            ) -> "security"

            currentDestination?.hasRoute<NotificationsRoute>() == true || currentRoute.contains(
                "Notifications"
            ) -> "notifications"

            currentDestination?.hasRoute<ProfileRoute>() == true || currentRoute.contains(
                "Profile"
            ) -> "profile"

            currentDestination?.hasRoute<TeamRoute>() == true || currentRoute.contains(
                "Team"
            ) -> "team"

            currentDestination?.hasRoute<BillingRoute>() == true || currentRoute.contains(
                "Billing"
            ) -> "billing"

            currentDestination?.hasRoute<IntegrationsRoute>() == true || currentRoute.contains(
                "Integrations"
            ) -> "integrations"

            currentDestination?.hasRoute<ApiWebhooksRoute>() == true || currentRoute.contains(
                "ApiWebhooks"
            ) -> "developer"

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
                "simulator" -> navController.navigate(PricingSimulatorRoute(0)) {
                    launchSingleTop = true
                }

                "scenarios" -> navController.navigate(ScenarioBuilderRoute) {
                    launchSingleTop = true
                }

                "forecast" -> navController.navigate(ForecastRoute) { launchSingleTop = true }
                "settings" -> navController.navigate(SettingsRoute) { launchSingleTop = true }
                "profile" -> navController.navigate(ProfileRoute) { launchSingleTop = true }

                "metrics" -> navController.navigate(MetricsRoute) { launchSingleTop = true }
                "goals", "goalplanner" -> navController.navigate(
                    GoalPlannerRoute
                ) { launchSingleTop = true }

                "input" -> navController.navigate(InputDataRoute) { launchSingleTop = true }
                "reports" -> navController.navigate(ReportsRoute) { launchSingleTop = true }
                "security" -> navController.navigate(SecurityRoute) { launchSingleTop = true }
                "notifications" -> navController.navigate(NotificationsRoute) { launchSingleTop = true }
                "login" -> navController.navigate(LoginRoute) { launchSingleTop = true }
                "team" -> navController.navigate(TeamRoute) { launchSingleTop = true }
                "billing" -> navController.navigate(BillingRoute) { launchSingleTop = true }
                "integrations" -> navController.navigate(IntegrationsRoute) { launchSingleTop = true }
                "developer", "apiwebhooks", "webhooks" -> navController.navigate(ApiWebhooksRoute) { launchSingleTop = true }
                "compare" -> navController.navigate(ScenarioComparisonRoute) {
                    launchSingleTop = true
                }

                "home", "dashboard" -> navController.popBackStack(DashboardRoute, inclusive = false)
            }
        }
        BrowserRouter.onUrlChange { newRoute ->
            if (isOnboardingCompleted == true) {
                when (newRoute.lowercase()) {
                    "simulator" -> navController.navigate(
                        PricingSimulatorRoute(0)
                    ) { launchSingleTop = true }

                    "scenarios" -> navController.navigate(ScenarioBuilderRoute) {
                        launchSingleTop = true
                    }

                    "forecast" -> navController.navigate(ForecastRoute) { launchSingleTop = true }
                    "settings" -> navController.navigate(SettingsRoute) { launchSingleTop = true }
                    "profile" -> navController.navigate(ProfileRoute) { launchSingleTop = true }

                    "metrics" -> navController.navigate(MetricsRoute) { launchSingleTop = true }
                    "goals", "goalplanner" -> navController.navigate(
                        GoalPlannerRoute
                    ) { launchSingleTop = true }

                    "input" -> navController.navigate(InputDataRoute) { launchSingleTop = true }
                    "reports" -> navController.navigate(ReportsRoute) { launchSingleTop = true }
                    "security" -> navController.navigate(SecurityRoute) { launchSingleTop = true }
                    "notifications" -> navController.navigate(NotificationsRoute) { launchSingleTop = true }
                    "login" -> navController.navigate(LoginRoute) { launchSingleTop = true }
                    "team" -> navController.navigate(TeamRoute) { launchSingleTop = true }
                    "billing" -> navController.navigate(BillingRoute) { launchSingleTop = true }
                    "integrations" -> navController.navigate(IntegrationsRoute) { launchSingleTop = true }
                    "developer", "apiwebhooks", "webhooks" -> navController.navigate(ApiWebhooksRoute) { launchSingleTop = true }
                    "compare" -> navController.navigate(ScenarioComparisonRoute) {
                        launchSingleTop = true
                    }

                    "home", "dashboard" -> navController.popBackStack(
                        DashboardRoute, inclusive = false
                    )
                }
            }
        }
    }

    val appLockRepository: com.example.revlens.domain.repository.AppLockRepository = org.koin.compose.koinInject()
    val biometricAuthenticator: com.example.revlens.core.platform.BiometricAuthenticator = org.koin.compose.koinInject()

    AppLockGate(
        appLockRepository = appLockRepository,
        biometricAuthenticator = biometricAuthenticator
    ) {
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
            val isNonDashboardTab =
                currentTab != null && currentTab != "Home" && currentTab != "Dashboard"

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
                            navController.navigate(CreateWorkspaceRoute)
                        },
                        onLoginClick = {
                            navController.navigate(LoginRoute)
                        }
                    )
                }

                composable<CreateWorkspaceRoute> {
                    CreateWorkspaceScreen(
                        onNavigateToOnboarding = {
                            navController.navigate(OnboardingRoute)
                        },
                        onBackClick = {
                            navController.popBackStack()
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
                        onNavigateToGrowth = {
                            navController.navigate(PricingSimulatorRoute(initialTab = 3)) {
                                launchSingleTop = true
                            }
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
                        onNavigateToMetrics = { navController.navigate(MetricsRoute) },
                        onNavigateToExperiments = { navController.navigate(ExperimentsRoute) }
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
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRevenueBreakdown = { navController.navigate(RevenueBreakdownRoute) },
                        onNavigateToLtvAnalysis = { navController.navigate(LtvAnalysisRoute) },
                        onNavigateToCacAnalytics = { navController.navigate(CacAnalyticsRoute) },
                        onNavigateToCustomerAnalytics = { navController.navigate(CustomerAnalyticsRoute) },
                        onNavigateToCohortAnalysis = { navController.navigate(CohortAnalysisRoute) }
                    )
                }

                composable<SettingsRoute> {
                    SettingsScreen(
                        onNavigateToWorkspace = { navController.navigate(WorkspaceManagementRoute) },
                        onNavigateToTeam = { navController.navigate(TeamRoute) },
                        onNavigateToBaseline = { navController.navigate(InputDataRoute) },
                        onNavigateToDataImport = { navController.navigate(DataImportRoute) },
                        onNavigateToSignUp = { navController.navigate(LoginRoute) },
                        onNavigateToProfile = { navController.navigate(ProfileRoute) },
                        onNavigateToReports = { navController.navigate(ReportsRoute) },
                        onNavigateToSecurity = { navController.navigate(SecurityRoute) },
                        onNavigateToNotifications = { navController.navigate(NotificationsRoute) },
                        onNavigateToBilling = { navController.navigate(BillingRoute) },
                        onNavigateToIntegrations = { navController.navigate(IntegrationsRoute) },
                        onNavigateToApiWebhooks = { navController.navigate(ApiWebhooksRoute) }
                    )
                }

                composable<WorkspaceManagementRoute> {
                    WorkspaceManagementScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToBaseline = { navController.navigate(InputDataRoute) },
                        onNavigateToCreateWorkspace = { navController.navigate(CreateWorkspaceRoute) }
                    )
                }

                composable<DataImportRoute> {
                    val importViewModel: com.example.revlens.ui.screens.data.DataImportViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.data.DataImportScreen(
                        viewModel = importViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAnalytics = {
                            navController.navigate(RevenueBreakdownRoute) {
                                popUpTo(DashboardRoute) { inclusive = false }
                            }
                        }
                    )
                }

                composable<RevenueBreakdownRoute> {
                    val breakdownViewModel: com.example.revlens.ui.screens.analytics.RevenueBreakdownViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.analytics.RevenueBreakdownScreen(
                        viewModel = breakdownViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDataImport = { navController.navigate(DataImportRoute) },
                        onNavigateToSimulator = { navController.navigate(PricingSimulatorRoute(0)) }
                    )
                }

                composable<LtvAnalysisRoute> {
                    val ltvViewModel: com.example.revlens.ui.screens.analytics.LtvAnalysisViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.analytics.LtvAnalysisScreen(
                        viewModel = ltvViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToWhatIf = {
                            navController.navigate(PricingSimulatorRoute(1))
                        },
                        onNavigateToBaseline = { navController.navigate(InputDataRoute) }
                    )
                }

                composable<CacAnalyticsRoute> {
                    val cacViewModel: com.example.revlens.ui.screens.analytics.CacAnalyticsViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.analytics.CacAnalyticsScreen(
                        viewModel = cacViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToGrowthSimulator = { navController.navigate(PricingSimulatorRoute(3)) }
                    )
                }

                composable<CustomerAnalyticsRoute> {
                    val customerViewModel: com.example.revlens.ui.screens.analytics.CustomerAnalyticsViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.analytics.CustomerAnalyticsScreen(
                        viewModel = customerViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDataImport = { navController.navigate(DataImportRoute) }
                    )
                }

                composable<CohortAnalysisRoute> {
                    val cohortViewModel: com.example.revlens.ui.screens.analytics.CohortAnalysisViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.analytics.CohortAnalysisScreen(
                        viewModel = cohortViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDataImport = { navController.navigate(DataImportRoute) }
                    )
                }

                composable<ExperimentsRoute> {
                    val expViewModel: com.example.revlens.ui.screens.experiments.ExperimentsViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.experiments.ExperimentsScreen(
                        viewModel = expViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDetails = { id -> navController.navigate(ExperimentDetailsRoute(id)) }
                    )
                }

                composable<ExperimentDetailsRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<ExperimentDetailsRoute>()
                    val detailsViewModel: com.example.revlens.ui.screens.experiments.ExperimentDetailsViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.experiments.ExperimentDetailsScreen(
                        experimentId = route.id,
                        viewModel = detailsViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ReportsRoute> {
                    val reportsViewModel: com.example.revlens.ui.screens.reports.ReportsViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.reports.ReportsScreen(
                        viewModel = reportsViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<SecurityRoute> {
                    val securityViewModel: com.example.revlens.ui.screens.security.SecuritySettingsViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.security.SecuritySettingsScreen(
                        viewModel = securityViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSignUp = { navController.navigate(LoginRoute) }
                    )
                }

                composable<NotificationsRoute> {
                    val notifViewModel: com.example.revlens.ui.screens.notifications.NotificationSettingsViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.notifications.NotificationSettingsScreen(
                        viewModel = notifViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<LoginRoute> {
                    val loginViewModel: com.example.revlens.ui.screens.auth.LoginViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.auth.LoginScreen(
                        viewModel = loginViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ProfileRoute> {
                    val profileViewModel: com.example.revlens.ui.screens.profile.ProfileViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.profile.ProfileScreen(
                        viewModel = profileViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLogin = { navController.navigate(LoginRoute) }
                    )
                }

                composable<TeamRoute> {
                    val teamViewModel: com.example.revlens.ui.screens.team.TeamViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.team.TeamScreen(
                        viewModel = teamViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<BillingRoute> {
                    val billingViewModel: com.example.revlens.ui.screens.billing.BillingViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.billing.BillingScreen(
                        viewModel = billingViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<IntegrationsRoute> {
                    val integrationsViewModel: com.example.revlens.ui.screens.integrations.IntegrationsViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.integrations.IntegrationsScreen(
                        viewModel = integrationsViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ApiWebhooksRoute> {
                    val developerViewModel: com.example.revlens.ui.screens.developer.ApiWebhooksViewModel = org.koin.compose.viewmodel.koinViewModel()
                    com.example.revlens.ui.screens.developer.ApiWebhooksScreen(
                        viewModel = developerViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
}




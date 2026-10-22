package com.revlens.app.ui.navigation

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.revlens.app.domain.model.SessionState
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.BottomNavItem
import com.revlens.app.ui.components.LocalAppNavigator
import com.revlens.app.ui.components.LocalCurrentTab
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.RevLensBottomBar
import com.revlens.app.ui.onboarding.IntroScreen
import com.revlens.app.ui.onboarding.OnboardingDataScreen
import com.revlens.app.ui.screens.analytics.CacAnalyticsScreen
import com.revlens.app.ui.screens.analytics.CacAnalyticsViewModel
import com.revlens.app.ui.screens.analytics.CohortAnalysisScreen
import com.revlens.app.ui.screens.analytics.CohortAnalysisViewModel
import com.revlens.app.ui.screens.analytics.CustomerAnalyticsScreen
import com.revlens.app.ui.screens.analytics.CustomerAnalyticsViewModel
import com.revlens.app.ui.screens.analytics.LtvAnalysisScreen
import com.revlens.app.ui.screens.analytics.LtvAnalysisViewModel
import com.revlens.app.ui.screens.analytics.RevenueBreakdownScreen
import com.revlens.app.ui.screens.analytics.RevenueBreakdownViewModel
import com.revlens.app.ui.screens.auth.InviteAcceptanceScreen
import com.revlens.app.ui.screens.auth.InviteAcceptanceViewModel
import com.revlens.app.ui.screens.auth.LoginScreen
import com.revlens.app.ui.screens.auth.LoginViewModel
import com.revlens.app.ui.screens.billing.BillingScreen
import com.revlens.app.ui.screens.billing.BillingViewModel
import com.revlens.app.ui.screens.data.DataImportScreen
import com.revlens.app.ui.screens.data.DataImportViewModel
import com.revlens.app.ui.screens.detailscreens.BusinessSetupScreen
import com.revlens.app.ui.screens.detailscreens.GoalPlannerScreen
import com.revlens.app.ui.screens.detailscreens.SaaSMetricsScreen
import com.revlens.app.ui.screens.detailscreens.ScenarioComparisonScreen
import com.revlens.app.ui.screens.developer.ApiWebhooksScreen
import com.revlens.app.ui.screens.developer.ApiWebhooksViewModel
import com.revlens.app.ui.screens.experiments.ExperimentDetailsScreen
import com.revlens.app.ui.screens.experiments.ExperimentDetailsViewModel
import com.revlens.app.ui.screens.experiments.ExperimentsScreen
import com.revlens.app.ui.screens.experiments.ExperimentsViewModel
import com.revlens.app.ui.screens.integrations.IntegrationsScreen
import com.revlens.app.ui.screens.integrations.IntegrationsViewModel
import com.revlens.app.ui.screens.marketing.LandingScreen
import com.revlens.app.ui.screens.marketing.LandingViewModel
import com.revlens.app.ui.screens.marketing.PublicPricingScreen
import com.revlens.app.ui.screens.marketing.PublicPricingViewModel
import com.revlens.app.ui.screens.navbarscreens.DashboardScreen
import com.revlens.app.ui.screens.navbarscreens.ForecastScreen
import com.revlens.app.ui.screens.navbarscreens.ScenarioBuilderScreen
import com.revlens.app.ui.screens.navbarscreens.SettingsScreen
import com.revlens.app.ui.screens.navbarscreens.SimulatorScreen
import com.revlens.app.ui.screens.notifications.NotificationSettingsScreen
import com.revlens.app.ui.screens.notifications.NotificationSettingsViewModel
import com.revlens.app.ui.screens.profile.AccountActionsScreen
import com.revlens.app.ui.screens.profile.ProfileScreen
import com.revlens.app.ui.screens.profile.ProfileViewModel
import com.revlens.app.ui.screens.reports.ReportsScreen
import com.revlens.app.ui.screens.reports.ReportsViewModel
import com.revlens.app.ui.screens.team.TeamScreen
import com.revlens.app.ui.screens.team.TeamViewModel
import com.revlens.app.ui.screens.workspace.CreateWorkspaceScreen
import com.revlens.app.ui.screens.workspace.WorkspaceManagementScreen
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
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
    val hasSeenIntro by viewModel.hasSeenIntro.collectAsState()
    val session by viewModel.session.collectAsState()
    val accessRevokedMessage by viewModel.accessRevokedMessage.collectAsState()
    val isReady by viewModel.isReady.collectAsState()
    val activeWorkspace by viewModel.activeWorkspace.collectAsState()

    if (accessRevokedMessage != null) {
        AlertDialog(
            onDismissRequest = { /* Require explicit OK click */ },
            containerColor = RevLensTheme.colors.surface,
            title = {
                Text(
                    text = "Workspace Access Removed",
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = accessRevokedMessage ?: "",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "OK",
                    onClick = {
                        viewModel.acknowledgeAccessRevoked {
                            navController.navigate(CreateWorkspaceRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        )
    }

    if (!isReady || hasSeenIntro == null || (hasSeenIntro == true && session is SessionState.Authenticated && isOnboardingCompleted == null)) {
        Box(
            modifier = modifier.fillMaxSize()
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
        ) -> "Dashboard"

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
        BottomNavItem("Dashboard", Icons.Filled.Dashboard, "Dashboard"),
        BottomNavItem("Simulator", Icons.Filled.Tune, "Simulator"),
        BottomNavItem("Scenarios", Icons.Filled.Layers, "Scenarios"),
        BottomNavItem("Forecast", Icons.AutoMirrored.Filled.TrendingUp, "Forecast"),
        BottomNavItem("Profile", Icons.Filled.Person, "Profile")
    )

    val navigateToTab: (String) -> Unit = { tabKey ->
        if (tabKey == "Dashboard" || tabKey == "Home") {
            navController.popBackStack(DashboardRoute, inclusive = false)
            BrowserRouter.updateUrl("dashboard")
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
                else -> "dashboard"
            }
            BrowserRouter.updateUrl(slug)
        }
    }

    // URL Update Effect
    LaunchedEffect(currentRoute) {
        val slug = when {
            currentDestination?.hasRoute<IntroRoute>() == true -> "intro"
            currentDestination?.hasRoute<OnboardingRoute>() == true -> "onboarding"
            currentDestination?.hasRoute<DashboardRoute>() == true || currentRoute.contains(
                "Dashboard"
            ) -> "dashboard"

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

            currentDestination?.hasRoute<LandingRoute>() == true || currentRoute.contains(
                "Landing"
            ) -> "landing"

            currentDestination?.hasRoute<PublicPricingRoute>() == true || currentRoute.contains(
                "PublicPricing"
            ) -> "pricing"

            currentDestination?.hasRoute<InviteAcceptanceRoute>() == true || currentRoute.contains(
                "InviteAcceptance"
            ) -> "invitation"

            else -> null
        }
        if (slug != null) {
            BrowserRouter.updateUrl(slug)
        }
    }

    // Deep Links Initialization
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
                "notifications" -> navController.navigate(NotificationsRoute) {
                    launchSingleTop = true
                }

                "login" -> navController.navigate(LoginRoute) { launchSingleTop = true }
                "team" -> navController.navigate(TeamRoute) { launchSingleTop = true }
                "billing" -> navController.navigate(BillingRoute) { launchSingleTop = true }
                "integrations" -> navController.navigate(IntegrationsRoute) {
                    launchSingleTop = true
                }

                "developer", "apiwebhooks", "webhooks" -> navController.navigate(
                    ApiWebhooksRoute
                ) { launchSingleTop = true }

                "pricing", "plans" -> navController.navigate(PublicPricingRoute) {
                    launchSingleTop = true
                }

                "landing" -> navController.navigate(LandingRoute) { launchSingleTop = true }
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
                    "notifications" -> navController.navigate(
                        NotificationsRoute
                    ) { launchSingleTop = true }

                    "login" -> navController.navigate(LoginRoute) { launchSingleTop = true }
                    "team" -> navController.navigate(TeamRoute) { launchSingleTop = true }
                    "billing" -> navController.navigate(BillingRoute) { launchSingleTop = true }
                    "integrations" -> navController.navigate(IntegrationsRoute) {
                        launchSingleTop = true
                    }

                    "developer", "apiwebhooks", "webhooks" -> navController.navigate(
                        ApiWebhooksRoute
                    ) { launchSingleTop = true }

                    "pricing", "plans" -> navController.navigate(
                        PublicPricingRoute
                    ) { launchSingleTop = true }

                    "landing" -> navController.navigate(LandingRoute) { launchSingleTop = true }
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

    CompositionLocalProvider(
        LocalAppNavigator provides navigateToTab,
        LocalCurrentTab provides (currentTab ?: "Dashboard")
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
                currentTab != null && currentTab != "Dashboard" && currentTab != "Home"

            PlatformBackHandler(enabled = isNonDashboardTab) {
                navController.navigate(DashboardRoute) {
                    popUpTo(DashboardRoute) { inclusive = false }
                    launchSingleTop = true
                }
            }

            // Strictly acts as a fallback Auth Guard, leaves forward routing to the ViewModels
            LaunchedEffect(session, hasSeenIntro) {
                // If user hasn't seen intro yet, let them view the intro screen uninterrupted
                if (hasSeenIntro == false) return@LaunchedEffect

                val currentRouteStr = navController.currentDestination?.route ?: ""
                if (currentRouteStr.isEmpty()) return@LaunchedEffect

                val isAuthScreen =
                    currentRouteStr.contains("IntroRoute") || currentRouteStr.contains(
                        "LoginRoute"
                    )

                when (session) {
                    is SessionState.SignedOut -> {
                        // Once intro is seen, SignedOut ALWAYS routes to LoginRoute (never Intro!)
                        if (!currentRouteStr.contains("LoginRoute")) {
                            navController.navigate(LoginRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    is SessionState.Authenticated -> {
                        // If they restart the app or authenticate and are on Auth screens,
                        // push them to Dashboard or CreateWorkspace.
                        if (isAuthScreen) {
                            val target: Any =
                                if (activeWorkspace != null && isOnboardingCompleted == true) DashboardRoute else CreateWorkspaceRoute
                            navController.navigate(target) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    else -> {}
                }
            }

            val initialStartDestination = remember {
                when {
                    hasSeenIntro == false -> IntroRoute
                    session !is SessionState.Authenticated -> LoginRoute
                    activeWorkspace != null && isOnboardingCompleted == true -> DashboardRoute
                    else -> CreateWorkspaceRoute
                }
            }

            NavHost(
                navController = navController,
                startDestination = initialStartDestination,
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
                            viewModel.setIntroSeen()
                            navController.navigate(LoginRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            BrowserRouter.updateUrl("login")
                        }
                    )
                }

                composable<LoginRoute> {
                    val loginViewModel: LoginViewModel = koinViewModel()
                    LoginScreen(
                        viewModel = loginViewModel,
                        onNavigateToDashboard = {
                            navController.navigate(DashboardRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            BrowserRouter.updateUrl("dashboard")
                        },
                        onNavigateToCreateWorkspace = {
                            navController.navigate(CreateWorkspaceRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToInviteAcceptance = {
                            navController.navigate(InviteAcceptanceRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateBack = null
                    )
                }

                composable<InviteAcceptanceRoute> {
                    val inviteViewModel: InviteAcceptanceViewModel = koinViewModel()
                    InviteAcceptanceScreen(
                        viewModel = inviteViewModel,
                        onNavigateToDashboard = {
                            navController.navigate(DashboardRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            BrowserRouter.updateUrl("dashboard")
                        },
                        onNavigateToCreateWorkspace = {
                            navController.navigate(CreateWorkspaceRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<CreateWorkspaceRoute> {
                    CreateWorkspaceScreen(
                        onNavigateToOnboarding = {
                            navController.navigate(OnboardingRoute) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToDashboard = {
                            navController.navigate(DashboardRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            BrowserRouter.updateUrl("dashboard")
                        },
                        onBackClick = null,
                        onLogout = {
                            viewModel.logout {
                                navController.navigate(LoginRoute) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                                BrowserRouter.updateUrl("login")
                            }
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
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            BrowserRouter.updateUrl("dashboard")
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
                        onNavigateToCompare = {
                            navController.navigate(ScenarioComparisonRoute)
                        },
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
                        onNavigateToRevenueBreakdown = {
                            navController.navigate(
                                RevenueBreakdownRoute
                            )
                        },
                        onNavigateToLtvAnalysis = { navController.navigate(LtvAnalysisRoute) },
                        onNavigateToCacAnalytics = {
                            navController.navigate(
                                CacAnalyticsRoute
                            )
                        },
                        onNavigateToCustomerAnalytics = {
                            navController.navigate(
                                CustomerAnalyticsRoute
                            )
                        },
                        onNavigateToCohortAnalysis = {
                            navController.navigate(
                                CohortAnalysisRoute
                            )
                        }
                    )
                }

                composable<SettingsRoute> {
                    SettingsScreen(
                        onNavigateToWorkspace = {
                            navController.navigate(
                                WorkspaceManagementRoute
                            )
                        },
                        onNavigateToTeam = { navController.navigate(TeamRoute) },
                        onNavigateToBaseline = { navController.navigate(InputDataRoute) },
                        onNavigateToDataImport = { navController.navigate(DataImportRoute) },
                        onNavigateToSignUp = { navController.navigate(LoginRoute) },
                        onNavigateToProfile = { navController.navigate(ProfileRoute) },
                        onNavigateToReports = { navController.navigate(ReportsRoute) },
                        onNavigateToNotifications = {
                            navController.navigate(
                                NotificationsRoute
                            )
                        },
                        onNavigateToMore = { navController.navigate(AccountActionsRoute) },
                        onNavigateToBilling = { navController.navigate(BillingRoute) },
                        onNavigateToIntegrations = {
                            navController.navigate(
                                IntegrationsRoute
                            )
                        },
                        onNavigateToApiWebhooks = { navController.navigate(ApiWebhooksRoute) }
                    )
                }

                composable<AccountActionsRoute> {
                    AccountActionsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate(LoginRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            BrowserRouter.updateUrl("login")
                        },
                        onNavigateToCreateWorkspace = {
                            navController.navigate(CreateWorkspaceRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<WorkspaceManagementRoute> {
                    WorkspaceManagementScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToBaseline = { navController.navigate(InputDataRoute) },
                        onNavigateToCreateWorkspace = {
                            navController.navigate(CreateWorkspaceRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<DataImportRoute> {
                    val importViewModel: DataImportViewModel = koinViewModel()
                    DataImportScreen(
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
                    val breakdownViewModel: RevenueBreakdownViewModel = koinViewModel()
                    RevenueBreakdownScreen(
                        viewModel = breakdownViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDataImport = { navController.navigate(DataImportRoute) },
                        onNavigateToSimulator = {
                            navController.navigate(
                                PricingSimulatorRoute(0)
                            )
                        }
                    )
                }

                composable<LtvAnalysisRoute> {
                    val ltvViewModel: LtvAnalysisViewModel = koinViewModel()
                    LtvAnalysisScreen(
                        viewModel = ltvViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToWhatIf = {
                            navController.navigate(
                                PricingSimulatorRoute(1)
                            )
                        },
                        onNavigateToBaseline = { navController.navigate(InputDataRoute) }
                    )
                }

                composable<CacAnalyticsRoute> {
                    val cacViewModel: CacAnalyticsViewModel = koinViewModel()
                    CacAnalyticsScreen(
                        viewModel = cacViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToGrowthSimulator = {
                            navController.navigate(
                                PricingSimulatorRoute(3)
                            )
                        }
                    )
                }

                composable<CustomerAnalyticsRoute> {
                    val customerViewModel: CustomerAnalyticsViewModel = koinViewModel()
                    CustomerAnalyticsScreen(
                        viewModel = customerViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDataImport = { navController.navigate(DataImportRoute) }
                    )
                }

                composable<CohortAnalysisRoute> {
                    val cohortViewModel: CohortAnalysisViewModel = koinViewModel()
                    CohortAnalysisScreen(
                        viewModel = cohortViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDataImport = { navController.navigate(DataImportRoute) }
                    )
                }

                composable<ExperimentsRoute> {
                    val expViewModel: ExperimentsViewModel = koinViewModel()
                    ExperimentsScreen(
                        viewModel = expViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDetails = { id ->
                            navController.navigate(ExperimentDetailsRoute(id))
                        }
                    )
                }

                composable<ExperimentDetailsRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<ExperimentDetailsRoute>()
                    val detailsViewModel: ExperimentDetailsViewModel = koinViewModel()
                    ExperimentDetailsScreen(
                        experimentId = route.id,
                        viewModel = detailsViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ReportsRoute> {
                    val reportsViewModel: ReportsViewModel = koinViewModel()
                    ReportsScreen(
                        viewModel = reportsViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<NotificationsRoute> {
                    val notifViewModel: NotificationSettingsViewModel = koinViewModel()
                    NotificationSettingsScreen(
                        viewModel = notifViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ProfileRoute> {
                    val profileViewModel: ProfileViewModel = koinViewModel()
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLogin = {
                            viewModel.logout {
                                navController.navigate(LoginRoute) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                                BrowserRouter.updateUrl("login")
                            }
                        }
                    )
                }

                composable<TeamRoute> {
                    val teamViewModel: TeamViewModel = koinViewModel()
                    TeamScreen(
                        viewModel = teamViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<BillingRoute> {
                    val billingViewModel: BillingViewModel = koinViewModel()
                    BillingScreen(
                        viewModel = billingViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<IntegrationsRoute> {
                    val integrationsViewModel: IntegrationsViewModel = koinViewModel()
                    IntegrationsScreen(
                        viewModel = integrationsViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ApiWebhooksRoute> {
                    val developerViewModel: ApiWebhooksViewModel = koinViewModel()
                    ApiWebhooksScreen(
                        viewModel = developerViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<LandingRoute> {
                    val landingViewModel: LandingViewModel = koinViewModel()
                    LandingScreen(
                        viewModel = landingViewModel,
                        onNavigateToAuth = { navController.navigate(LoginRoute) },
                        onNavigateToDemo = { navController.navigate(DashboardRoute) },
                        onNavigateToPricing = { navController.navigate(PublicPricingRoute) }
                    )
                }

                composable<PublicPricingRoute> {
                    val pricingViewModel: PublicPricingViewModel = koinViewModel()
                    PublicPricingScreen(
                        viewModel = pricingViewModel,
                        onNavigateToAuth = { navController.navigate(LoginRoute) },
                        onNavigateToHome = { navController.navigate(LandingRoute) }
                    )
                }
            }
        }
    }
}

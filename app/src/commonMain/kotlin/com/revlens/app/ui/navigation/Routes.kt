package com.revlens.app.ui.navigation

import kotlinx.serialization.Serializable

// Existing Core Routes
@Serializable
object IntroRoute

@Serializable
object OnboardingRoute

@Serializable
object DashboardRoute

@Serializable
object InputDataRoute

@Serializable
data class PricingSimulatorRoute(
    val initialTab: Int = 0,
    val scenarioId: String? = null
)

@Serializable
object ForecastRoute

@Serializable
object GoalPlannerRoute

@Serializable
object ScenarioBuilderRoute

@Serializable
object ScenarioComparisonRoute

@Serializable
object MetricsRoute

@Serializable
object SettingsRoute

// Group A: Public & Auth Routes
@Serializable
object LandingRoute

@Serializable
object PublicPricingRoute

@Serializable
object LoginRoute


// Group B: Onboarding Routes
@Serializable
object InviteAcceptanceRoute

@Serializable
object CreateWorkspaceRoute

// Group D: Analytics Routes
@Serializable
object CustomerAnalyticsRoute

@Serializable
object CohortAnalysisRoute

@Serializable
object RevenueBreakdownRoute

@Serializable
object LtvAnalysisRoute

@Serializable
object CacAnalyticsRoute

// Group E: Data & Experiments Routes
@Serializable
object DataImportRoute

@Serializable
object ExperimentsRoute

@Serializable
data class ExperimentDetailsRoute(val id: String)

@Serializable
object ReportsRoute

// Group F: Integrations & Developer Routes
@Serializable
object IntegrationsRoute

@Serializable
object ApiWebhooksRoute

// Group G: Workspace & Account Routes
@Serializable
object WorkspaceManagementRoute

@Serializable
object TeamRoute

@Serializable
object BillingRoute

@Serializable
object ProfileRoute

@Serializable
object NotificationsRoute

@Serializable
object AccountActionsRoute

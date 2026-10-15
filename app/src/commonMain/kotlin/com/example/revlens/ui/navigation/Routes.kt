package com.example.revlens.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object IntroRoute

@Serializable
object OnboardingRoute

@Serializable
object DashboardRoute

@Serializable
object InputDataRoute

@Serializable
data class PricingSimulatorRoute(val initialTab: Int = 0)

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

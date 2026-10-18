package com.revlens.app.di

import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Plan
import com.revlens.app.model.Scenario
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.screens.detailscreens.BusinessSetupViewModel
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class WasmProfileRepository : ProfileRepository {
    private val json = Json { ignoreUnknownKeys = true }

    private val _flow = MutableStateFlow(loadFromStorage())
    override val profileFlow: Flow<BusinessProfile?> = _flow.asStateFlow()

    private val _onboardingFlow = MutableStateFlow(loadOnboardingCompleted())
    override val isOnboardingCompletedFlow: Flow<Boolean> = _onboardingFlow.asStateFlow()

    private fun loadOnboardingCompleted(): Boolean {
        return try {
            val item = window.localStorage.getItem("revlens_onboarding_completed")
            val hasProfile = window.localStorage.getItem("revlens_profile") != null
            item == "true" && hasProfile
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _onboardingFlow.value = completed
        try {
            window.localStorage.setItem("revlens_onboarding_completed", completed.toString())
        } catch (_: Exception) {
        }
    }

    private fun loadFromStorage(): BusinessProfile? {
        return try {
            val raw = window.localStorage.getItem("revlens_profile")
            if (raw != null) {
                val profile = json.decodeFromString<BusinessProfile>(raw)
                // If stored profile contains old Enterprise tier from previous default, sanitize to Starter & Pro
                if (profile.plans.any { it.name.equals("Enterprise", ignoreCase = true) }) {
                    val sanitized = profile.copy(
                        plans = listOf(
                            Plan(
                                id = "1", name = "Starter", price = BigDecimal("29.00"),
                                customerCount = 120
                            ),
                            Plan(
                                id = "2", name = "Pro", price = BigDecimal("79.00"),
                                customerCount = 65
                            )
                        )
                    )
                    window.localStorage.setItem("revlens_profile", json.encodeToString(sanitized))
                    sanitized
                } else {
                    profile
                }
            } else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun saveProfile(profile: BusinessProfile) {
        _flow.value = profile
        try {
            window.localStorage.setItem("revlens_profile", json.encodeToString(profile))
        } catch (_: Exception) {
        }
    }
}

class WasmScenarioRepository : ScenarioRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val _flow = MutableStateFlow(loadFromStorage() ?: emptyList())
    override val scenariosFlow: Flow<List<Scenario>> = _flow.asStateFlow()

    private fun loadFromStorage(): List<Scenario>? {
        return try {
            val raw = window.localStorage.getItem("revlens_scenarios")
            if (raw != null) json.decodeFromString(raw) else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun saveScenario(scenario: Scenario) {
        val list = _flow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == scenario.id }
        if (idx != -1) list[idx] = scenario else list.add(scenario)
        _flow.value = list
        try {
            window.localStorage.setItem("revlens_scenarios", json.encodeToString(list))
        } catch (_: Exception) {
        }
    }

    override suspend fun deleteScenario(scenarioId: String) {
        val list = _flow.value.filter { it.id != scenarioId }
        _flow.value = list
        try {
            window.localStorage.setItem("revlens_scenarios", json.encodeToString(list))
        } catch (_: Exception) {
        }
    }
}

val wasmModule = module {
    single<ProfileRepository> {
        com.revlens.app.data.WebFirestoreProfileRepository(
            WasmProfileRepository()
        )
    }
    single<ScenarioRepository> {
        com.revlens.app.data.WebFirestoreScenarioRepository(
            WasmScenarioRepository()
        )
    }
    single<WorkspaceRepository> {
        com.revlens.app.data.WebFirestoreWorkspaceRepository(
            com.revlens.app.data.DefaultWorkspaceRepository()
        )
    }
    single<com.revlens.app.domain.context.WorkspaceContext> { com.revlens.app.domain.context.DefaultWorkspaceContext() }
    single<com.revlens.app.domain.session.SessionManager> { com.revlens.app.domain.session.DefaultSessionManager() }
    single { com.revlens.app.domain.usecase.ResolveGateUseCase() }
    single<com.revlens.app.core.platform.FilePicker> { com.revlens.app.core.platform.DefaultFilePicker() }
    single<com.revlens.app.core.platform.FileSharer> { com.revlens.app.core.platform.DefaultFileSharer() }
    single<com.revlens.app.core.platform.SecureStorage> { com.revlens.app.core.platform.InMemorySecureStorage() }
    single<com.revlens.app.core.platform.BiometricAuthenticator> { com.revlens.app.core.platform.DefaultBiometricAuthenticator() }
    single<com.revlens.app.core.platform.LocalNotifier> { com.revlens.app.core.platform.DefaultLocalNotifier() }
    single<com.revlens.app.domain.repository.AnalyticsRepository> { com.revlens.app.data.DefaultAnalyticsRepository() }
    single<com.revlens.app.domain.repository.ExperimentRepository> { com.revlens.app.data.DefaultExperimentRepository() }
    single<com.revlens.app.domain.repository.ReportRepository> { com.revlens.app.data.DefaultReportRepository() }
    single<com.revlens.app.domain.repository.AppLockRepository> { com.revlens.app.data.DefaultAppLockRepository() }
    single<com.revlens.app.domain.repository.NotificationPreferencesRepository> { com.revlens.app.data.DefaultNotificationPreferencesRepository() }
    single<com.revlens.app.domain.repository.AuthRepository> {
        com.revlens.app.data.WebFirebaseAuthRepository(
            get()
        )
    }
    single<com.revlens.app.domain.repository.TeamRepository> { com.revlens.app.data.fake.FakeTeamRepository() }
    single<com.revlens.app.domain.repository.BillingRepository> { com.revlens.app.data.fake.FakeBillingRepository() }
    single<com.revlens.app.domain.repository.IntegrationRepository> { com.revlens.app.data.fake.FakeIntegrationRepository() }
    single<com.revlens.app.domain.repository.DeveloperRepository> { com.revlens.app.data.fake.FakeDeveloperRepository() }
    single { com.revlens.app.domain.usecase.MigrateGuestDataUseCase(get(), get(), get(), get()) }
    single { com.revlens.app.domain.usecase.LoginUseCase(get(), get(), get()) }
    single { com.revlens.app.domain.usecase.SignUpUseCase(get(), get(), get()) }
    single { com.revlens.app.domain.usecase.RequestPasswordResetUseCase(get()) }
    single { com.revlens.app.domain.usecase.ValidateResetTokenUseCase(get()) }
    single { com.revlens.app.domain.usecase.ResetPasswordUseCase(get()) }
    single { com.revlens.app.domain.usecase.CreateWorkspaceUseCase(get(), get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { BusinessSetupViewModel() }
    viewModel {
        com.revlens.app.ui.screens.workspace.WorkspaceManagementViewModel(
            get(), get(), get(), get()
        )
    }
    viewModel { com.revlens.app.ui.screens.workspace.CreateWorkspaceViewModel(get()) }
    viewModel { com.revlens.app.ui.screens.data.DataImportViewModel(get(), get(), get()) }
    viewModel {
        com.revlens.app.ui.screens.analytics.RevenueBreakdownViewModel(
            get(), get(), get()
        )
    }
    viewModel { com.revlens.app.ui.screens.analytics.LtvAnalysisViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.analytics.CacAnalyticsViewModel(get(), get(), get()) }
    viewModel { com.revlens.app.ui.screens.simulator.GrowthSimulatorViewModel(get(), get()) }
    viewModel {
        com.revlens.app.ui.screens.analytics.CustomerAnalyticsViewModel(
            get(), get(), get()
        )
    }
    viewModel { com.revlens.app.ui.screens.analytics.CohortAnalysisViewModel(get(), get(), get()) }
    viewModel { com.revlens.app.ui.screens.experiments.ExperimentsViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.experiments.ExperimentDetailsViewModel(get()) }
    viewModel {
        com.revlens.app.ui.screens.reports.ReportsViewModel(
            get(), get(), get(), get(), get()
        )
    }
    viewModel { com.revlens.app.ui.screens.security.SecuritySettingsViewModel(get(), get(), get()) }
    viewModel {
        com.revlens.app.ui.screens.notifications.NotificationSettingsViewModel(
            get(), get()
        )
    }
    viewModel { com.revlens.app.ui.screens.auth.LoginViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.profile.ProfileViewModel(get(), get(), get(), get()) }
    viewModel { com.revlens.app.ui.screens.team.TeamViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.billing.BillingViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.integrations.IntegrationsViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.developer.ApiWebhooksViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.marketing.PublicPricingViewModel() }
    viewModel { com.revlens.app.ui.screens.marketing.LandingViewModel() }
}





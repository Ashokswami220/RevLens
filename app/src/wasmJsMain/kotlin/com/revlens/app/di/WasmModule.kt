package com.revlens.app.di

import com.revlens.app.domain.model.SessionState
import com.revlens.app.data.DefaultTeamRepository
import com.revlens.app.data.DefaultIntegrationRepository
import com.revlens.app.data.DefaultDeveloperRepository
import com.revlens.app.data.DefaultBillingRepository
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Plan
import com.revlens.app.model.Scenario
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.screens.analytics.CacAnalyticsViewModel
import com.revlens.app.ui.screens.analytics.CohortAnalysisViewModel
import com.revlens.app.ui.screens.analytics.CustomerAnalyticsViewModel
import com.revlens.app.ui.screens.analytics.LtvAnalysisViewModel
import com.revlens.app.ui.screens.analytics.RevenueBreakdownViewModel
import com.revlens.app.ui.screens.auth.InviteAcceptanceViewModel
import com.revlens.app.ui.screens.auth.LoginViewModel
import com.revlens.app.ui.screens.billing.BillingViewModel
import com.revlens.app.ui.screens.data.DataImportViewModel
import com.revlens.app.ui.screens.detailscreens.BusinessSetupViewModel
import com.revlens.app.ui.screens.developer.ApiWebhooksViewModel
import com.revlens.app.ui.screens.experiments.ExperimentDetailsViewModel
import com.revlens.app.ui.screens.experiments.ExperimentsViewModel
import com.revlens.app.ui.screens.integrations.IntegrationsViewModel
import com.revlens.app.core.platform.BiometricAuthenticator
import com.revlens.app.core.platform.DefaultBiometricAuthenticator
import com.revlens.app.core.platform.DefaultFilePicker
import com.revlens.app.core.platform.DefaultFileSharer
import com.revlens.app.core.platform.DefaultLocalNotifier
import com.revlens.app.core.platform.FilePicker
import com.revlens.app.core.platform.FileSharer
import com.revlens.app.core.platform.InMemorySecureStorage
import com.revlens.app.core.platform.LocalNotifier
import com.revlens.app.core.platform.SecureStorage
import com.revlens.app.core.util.DefaultIdGenerator
import com.revlens.app.core.util.IdGenerator
import com.revlens.app.data.DefaultAnalyticsRepository
import com.revlens.app.data.DefaultAppLockRepository
import com.revlens.app.data.DefaultExperimentRepository
import com.revlens.app.data.DefaultNotificationPreferencesRepository
import com.revlens.app.data.DefaultReportRepository
import com.revlens.app.data.DefaultWorkspaceRepository
import com.revlens.app.data.WebFirebaseAuthRepository
import com.revlens.app.data.WebFirestoreProfileRepository
import com.revlens.app.data.WebFirestoreScenarioRepository
import com.revlens.app.data.WebFirestoreWorkspaceRepository
import com.revlens.app.domain.context.DefaultWorkspaceContext
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.repository.AnalyticsRepository
import com.revlens.app.domain.repository.AppLockRepository
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.BillingRepository
import com.revlens.app.domain.repository.DeveloperRepository
import com.revlens.app.domain.repository.ExperimentRepository
import com.revlens.app.domain.repository.IntegrationRepository
import com.revlens.app.domain.repository.NotificationPreferencesRepository
import com.revlens.app.domain.repository.ReportRepository
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.domain.session.DefaultSessionManager
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.domain.usecase.CreateWorkspaceUseCase
import com.revlens.app.domain.usecase.LoginUseCase
import com.revlens.app.domain.usecase.MigrateGuestDataUseCase
import com.revlens.app.domain.usecase.RequestPasswordResetUseCase
import com.revlens.app.domain.usecase.ResetPasswordUseCase
import com.revlens.app.domain.usecase.ResolveGateUseCase
import com.revlens.app.domain.usecase.SignUpUseCase
import com.revlens.app.domain.usecase.ValidateResetTokenUseCase
import com.revlens.app.ui.screens.marketing.LandingViewModel
import com.revlens.app.ui.screens.marketing.PublicPricingViewModel
import com.revlens.app.ui.screens.notifications.NotificationSettingsViewModel
import com.revlens.app.ui.screens.profile.ProfileViewModel
import com.revlens.app.ui.screens.reports.ReportsViewModel
import com.revlens.app.ui.screens.security.SecuritySettingsViewModel
import com.revlens.app.ui.screens.simulator.GrowthSimulatorViewModel
import com.revlens.app.ui.screens.team.TeamViewModel
import com.revlens.app.ui.screens.workspace.CreateWorkspaceViewModel
import com.revlens.app.ui.screens.workspace.WorkspaceManagementViewModel
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
            item == "true"
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _onboardingFlow.value = completed
        try {
            if (completed) {
                window.localStorage.setItem("revlens_onboarding_completed", "true")
            } else {
                window.localStorage.removeItem("revlens_onboarding_completed")
                window.localStorage.removeItem("revlens_profile")
                _flow.value = null
            }
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
            } else if (loadOnboardingCompleted()) {
                val defaultProf = com.revlens.app.model.createDefaultGuestProfile()
                window.localStorage.setItem("revlens_profile", json.encodeToString(defaultProf))
                defaultProf
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

    private val _introFlow = MutableStateFlow(loadIntroSeen())
    override val hasSeenIntroFlow: Flow<Boolean> = _introFlow.asStateFlow()

    private fun loadIntroSeen(): Boolean {
        return try {
            val item = window.localStorage.getItem("revlens_intro_seen")
            item == "true"
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun setHasSeenIntro(seen: Boolean) {
        _introFlow.value = seen
        try {
            if (seen) {
                window.localStorage.setItem("revlens_intro_seen", "true")
            } else {
                window.localStorage.removeItem("revlens_intro_seen")
            }
        } catch (_: Exception) {
        }
    }

    override suspend fun clearLocalData() {
        try {
            window.localStorage.removeItem("revlens_onboarding_completed")
            window.localStorage.removeItem("revlens_profile")
            _flow.value = null
            _onboardingFlow.value = false
            // Note: revlens_intro_seen is intentionally preserved
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
        WebFirestoreProfileRepository(
            WasmProfileRepository()
        )
    }
    single<ScenarioRepository> {
        WebFirestoreScenarioRepository(
            WasmScenarioRepository()
        )
    }
    single<WorkspaceRepository> {
        WebFirestoreWorkspaceRepository(
            DefaultWorkspaceRepository()
        )
    }
    single<WorkspaceContext> { DefaultWorkspaceContext() }
    single<SessionManager> { DefaultSessionManager(SessionState.Unknown) }
    single { ResolveGateUseCase() }
    single<IdGenerator> { DefaultIdGenerator() }
    single<FilePicker> { DefaultFilePicker() }
    single<FileSharer> { DefaultFileSharer() }
    single<SecureStorage> { InMemorySecureStorage() }
    single<BiometricAuthenticator> { DefaultBiometricAuthenticator() }
    single<LocalNotifier> { DefaultLocalNotifier() }
    single<AnalyticsRepository> { DefaultAnalyticsRepository() }
    single<ExperimentRepository> { DefaultExperimentRepository() }
    single<ReportRepository> { DefaultReportRepository() }
    single<AppLockRepository> { DefaultAppLockRepository() }
    single<NotificationPreferencesRepository> { DefaultNotificationPreferencesRepository() }
    single<AuthRepository> {
        WebFirebaseAuthRepository(
            get()
        )
    }
    single<TeamRepository> {
        DefaultTeamRepository()
    }
    single<BillingRepository> { DefaultBillingRepository() }
    single<IntegrationRepository> { DefaultIntegrationRepository() }
    single<DeveloperRepository> { DefaultDeveloperRepository() }
    single { MigrateGuestDataUseCase(get(), get(), get(), get()) }
    single { LoginUseCase(get(), get(), get()) }
    single { SignUpUseCase(get(), get(), get()) }
    single { RequestPasswordResetUseCase(get()) }
    single { ValidateResetTokenUseCase(get()) }
    single { ResetPasswordUseCase(get()) }
    single { CreateWorkspaceUseCase(get(), get(), get()) }
    viewModel {
        MainViewModel(
            repository = get(),
            scenarioRepository = get(),
            workspaceRepository = get(),
            workspaceContext = get(),
            sessionManager = get(),
            authRepository = get(),
            teamRepository = get()
        )
    }
    viewModel { BusinessSetupViewModel() }
    viewModel {
        WorkspaceManagementViewModel(
            workspaceRepository = get(),
            workspaceContext = get(),
            profileRepository = get(),
            teamRepository = get(),
            authRepository = get(),
            sessionManager = get()
        )
    }
    viewModel {
        CreateWorkspaceViewModel(
            createWorkspaceUseCase = get(),
            teamRepository = get(),
            workspaceRepository = get(),
            workspaceContext = get(),
            profileRepository = get(),
            sessionManager = get(),
            authRepository = get()
        )
    }
    viewModel {
        DataImportViewModel(
            analyticsRepository = get(),
            workspaceContext = get(),
            filePicker = get()
        )
    }
    viewModel {
        RevenueBreakdownViewModel(
            profileRepository = get(),
            analyticsRepository = get(),
            workspaceContext = get()
        )
    }
    viewModel {
        LtvAnalysisViewModel(
            profileRepository = get(),
            analyticsRepository = get()
        )
    }
    viewModel {
        CacAnalyticsViewModel(
            profileRepository = get(),
            analyticsRepository = get(),
            workspaceContext = get()
        )
    }
    viewModel {
        GrowthSimulatorViewModel(
            profileRepository = get(),
            scenarioRepository = get()
        )
    }
    viewModel {
        CustomerAnalyticsViewModel(
            profileRepository = get(),
            analyticsRepository = get()
        )
    }
    viewModel {
        CohortAnalysisViewModel(
            profileRepository = get(),
            analyticsRepository = get()
        )
    }
    viewModel {
        ExperimentsViewModel(
            experimentRepository = get(),
            workspaceContext = get()
        )
    }
    viewModel {
        ExperimentDetailsViewModel(
            experimentRepository = get()
        )
    }
    viewModel {
        ReportsViewModel(
            reportRepository = get(),
            workspaceContext = get(),
            profileRepository = get(),
            scenarioRepository = get(),
            fileSharer = get()
        )
    }
    viewModel {
        SecuritySettingsViewModel(
            appLockRepository = get(),
            biometricAuthenticator = get(),
            sessionManager = get()
        )
    }
    viewModel {
        NotificationSettingsViewModel(
            preferencesRepository = get(),
            localNotifier = get()
        )
    }
    viewModel {
        LoginViewModel(
            loginUseCase = get(),
            profileRepository = get(),
            teamRepository = get()
        )
    }
    viewModel {
        InviteAcceptanceViewModel(
            teamRepository = get(),
            workspaceContext = get(),
            profileRepository = get(),
            sessionManager = get(),
            authRepository = get(),
            workspaceRepository = get()
        )
    }
    viewModel {
        ProfileViewModel(
            authRepository = get(),
            sessionManager = get(),
            workspaceRepository = get(),
            workspaceContext = get()
        )
    }
    viewModel {
        TeamViewModel(
            teamRepository = get(),
            workspaceContext = get(),
            sessionManager = get(),
            authRepository = get()
        )
    }
    viewModel {
        BillingViewModel(
            billingRepository = get(),
            workspaceContext = get()
        )
    }
    viewModel {
        IntegrationsViewModel(
            integrationRepository = get(),
            workspaceContext = get()
        )
    }
    viewModel {
        ApiWebhooksViewModel(
            developerRepository = get(),
            workspaceContext = get()
        )
    }
    viewModel { PublicPricingViewModel() }
    viewModel { LandingViewModel() }
}

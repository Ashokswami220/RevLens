package com.example.revlens.di

import com.example.revlens.data.ProfileRepository
import com.example.revlens.data.ScenarioRepository
import com.example.revlens.domain.repository.WorkspaceRepository
import com.example.revlens.model.BigDecimal
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
import com.example.revlens.model.Scenario
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.screens.detailscreens.BusinessSetupViewModel
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
    single<ProfileRepository> { WasmProfileRepository() }
    single<ScenarioRepository> { WasmScenarioRepository() }
    single<WorkspaceRepository> { com.example.revlens.data.DefaultWorkspaceRepository() }
    single<com.example.revlens.domain.context.WorkspaceContext> { com.example.revlens.domain.context.DefaultWorkspaceContext() }
    single<com.example.revlens.domain.session.SessionManager> { com.example.revlens.domain.session.DefaultSessionManager() }
    single { com.example.revlens.domain.usecase.ResolveGateUseCase() }
    single<com.example.revlens.core.platform.FilePicker> { com.example.revlens.core.platform.DefaultFilePicker() }
    single<com.example.revlens.core.platform.FileSharer> { com.example.revlens.core.platform.DefaultFileSharer() }
    single<com.example.revlens.core.platform.SecureStorage> { com.example.revlens.core.platform.InMemorySecureStorage() }
    single<com.example.revlens.core.platform.BiometricAuthenticator> { com.example.revlens.core.platform.DefaultBiometricAuthenticator() }
    single<com.example.revlens.core.platform.LocalNotifier> { com.example.revlens.core.platform.DefaultLocalNotifier() }
    single<com.example.revlens.domain.repository.AnalyticsRepository> { com.example.revlens.data.DefaultAnalyticsRepository() }
    single { com.example.revlens.domain.usecase.CreateWorkspaceUseCase(get(), get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { BusinessSetupViewModel() }
    viewModel { com.example.revlens.ui.screens.workspace.WorkspaceManagementViewModel(get(), get(), get(), get()) }
    viewModel { com.example.revlens.ui.screens.workspace.CreateWorkspaceViewModel(get()) }
    viewModel { com.example.revlens.ui.screens.data.DataImportViewModel(get(), get(), get()) }
    viewModel { com.example.revlens.ui.screens.analytics.RevenueBreakdownViewModel(get(), get(), get()) }
    viewModel { com.example.revlens.ui.screens.analytics.LtvAnalysisViewModel(get(), get()) }
    viewModel { com.example.revlens.ui.screens.analytics.CacAnalyticsViewModel(get(), get(), get()) }
    viewModel { com.example.revlens.ui.screens.simulator.GrowthSimulatorViewModel(get(), get()) }
    viewModel { com.example.revlens.ui.screens.analytics.CustomerAnalyticsViewModel(get(), get(), get()) }
}

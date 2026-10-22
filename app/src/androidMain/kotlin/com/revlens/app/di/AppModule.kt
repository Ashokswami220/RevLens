package com.revlens.app.di

import com.revlens.app.domain.model.SessionState
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.revlens.app.core.platform.InMemorySecureStorage
import com.revlens.app.core.platform.SecureStorage
import com.revlens.app.core.util.DefaultIdGenerator
import com.revlens.app.core.util.IdGenerator
import com.revlens.app.data.AndroidFirebaseAuthRepository
import com.revlens.app.data.AndroidFirestoreProfileRepository
import com.revlens.app.data.AndroidFirestoreScenarioRepository
import com.revlens.app.data.AndroidFirestoreWorkspaceRepository
import com.revlens.app.data.DataStoreProfileRepository
import com.revlens.app.data.DataStoreScenarioRepository
import com.revlens.app.data.DefaultExperimentRepository
import com.revlens.app.data.DefaultWorkspaceRepository
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.context.DefaultWorkspaceContext
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.ExperimentRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.domain.session.DefaultSessionManager
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.domain.usecase.CreateWorkspaceUseCase
import com.revlens.app.domain.usecase.LoginUseCase
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
import com.revlens.app.ui.screens.marketing.LandingViewModel
import com.revlens.app.ui.screens.marketing.PublicPricingViewModel
import com.revlens.app.ui.screens.notifications.NotificationSettingsViewModel
import com.revlens.app.ui.screens.profile.ProfileViewModel
import com.revlens.app.ui.screens.reports.ReportsViewModel
import com.revlens.app.ui.screens.simulator.GrowthSimulatorViewModel
import com.revlens.app.ui.screens.team.TeamViewModel
import com.revlens.app.ui.screens.workspace.CreateWorkspaceViewModel
import com.revlens.app.ui.screens.workspace.WorkspaceManagementViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "revlens_settings")

val appModule = module {
    single<DataStore<Preferences>> { androidContext().dataStore }
    single<ProfileRepository> {
        AndroidFirestoreProfileRepository(
            localFallback = DataStoreProfileRepository(get()),
            workspaceContext = get(),
            authRepository = get()
        )
    }
    single<ScenarioRepository> {
        AndroidFirestoreScenarioRepository(
            localFallback = DataStoreScenarioRepository(get()),
            workspaceContext = get()
        )
    }
    single<WorkspaceRepository> {
        AndroidFirestoreWorkspaceRepository(
            localFallback = DefaultWorkspaceRepository(emptyList()),
            authRepository = get()
        )
    }
    single<WorkspaceContext> { DefaultWorkspaceContext() }
    single<SessionManager> { DefaultSessionManager(SessionState.Unknown) }
    single<IdGenerator> { DefaultIdGenerator() }
    single<SecureStorage> { InMemorySecureStorage() }
    single<ExperimentRepository> { DefaultExperimentRepository() }
    single<AuthRepository> {
        AndroidFirebaseAuthRepository(
            context = androidContext(),
            sessionManager = get()
        )
    }
    single { LoginUseCase(get(), get()) }
    single { CreateWorkspaceUseCase(get(), get(), get()) }
    viewModel {
        MainViewModel(
            repository = get(),
            scenarioRepository = get(),
            workspaceRepository = get(),
            workspaceContext = get(),
            sessionManager = get(),
            authRepository = get()
        )
    }
    viewModel { BusinessSetupViewModel() }
    viewModel {
        WorkspaceManagementViewModel(
            workspaceRepository = get(),
            workspaceContext = get(),
            profileRepository = get()
        )
    }
    viewModel {
        CreateWorkspaceViewModel(
            createWorkspaceUseCase = get(),
            workspaceRepository = get(),
            workspaceContext = get(),
            profileRepository = get(),
            sessionManager = get(),
            authRepository = get()
        )
    }
    viewModel { DataImportViewModel() }
    viewModel { RevenueBreakdownViewModel(profileRepository = get()) }
    viewModel { LtvAnalysisViewModel(profileRepository = get()) }
    viewModel { CacAnalyticsViewModel(profileRepository = get()) }
    viewModel {
        GrowthSimulatorViewModel(
            profileRepository = get(),
            scenarioRepository = get()
        )
    }
    viewModel { CustomerAnalyticsViewModel(profileRepository = get()) }
    viewModel { CohortAnalysisViewModel(profileRepository = get()) }
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
    viewModel { ReportsViewModel() }
    viewModel { NotificationSettingsViewModel() }
    viewModel {
        LoginViewModel(
            loginUseCase = get(),
            profileRepository = get()
        )
    }
    viewModel {
        InviteAcceptanceViewModel()
    }
    viewModel {
        ProfileViewModel(
            authRepository = get(),
            sessionManager = get(),
            workspaceRepository = get(),
            workspaceContext = get()
        )
    }
    viewModel { TeamViewModel() }
    viewModel { BillingViewModel() }
    viewModel { IntegrationsViewModel() }
    viewModel { ApiWebhooksViewModel() }
    viewModel { PublicPricingViewModel() }
    viewModel { LandingViewModel() }
}

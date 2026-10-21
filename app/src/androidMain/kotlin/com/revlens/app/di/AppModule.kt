package com.revlens.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.revlens.app.data.DataStoreProfileRepository
import com.revlens.app.data.DataStoreScenarioRepository
import com.revlens.app.data.DefaultWorkspaceRepository
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.context.DefaultWorkspaceContext
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.domain.session.DefaultSessionManager
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.domain.usecase.ResolveGateUseCase
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.screens.detailscreens.BusinessSetupViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "revlens_settings")

val appModule = module {
    single<DataStore<Preferences>> { androidContext().dataStore }
    single<ProfileRepository> {
        com.revlens.app.data.AndroidFirestoreProfileRepository(
            localFallback = DataStoreProfileRepository(get()),
            workspaceContext = getOrNull()
        )
    }
    single<ScenarioRepository> {
        com.revlens.app.data.AndroidFirestoreScenarioRepository(
            DataStoreScenarioRepository(get())
        )
    }
    single<WorkspaceRepository> {
        com.revlens.app.data.AndroidFirestoreWorkspaceRepository(
            DefaultWorkspaceRepository()
        )
    }
    single<WorkspaceContext> { DefaultWorkspaceContext() }
    single<SessionManager> { DefaultSessionManager() }
    single { ResolveGateUseCase() }
    single<com.revlens.app.core.util.IdGenerator> { com.revlens.app.core.util.DefaultIdGenerator() }
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
        com.revlens.app.data.AndroidFirebaseAuthRepository(
            get(), getOrNull()
        )
    }
    single<com.revlens.app.domain.repository.TeamRepository> {
        com.revlens.app.data.AndroidFirestoreTeamRepository(
            localFallback = com.revlens.app.data.fake.FakeTeamRepository(getOrNull(), getOrNull()),
            workspaceContext = getOrNull(),
            profileRepository = getOrNull()
        )
    }
    single<com.revlens.app.domain.repository.BillingRepository> { com.revlens.app.data.fake.FakeBillingRepository() }
    single<com.revlens.app.domain.repository.IntegrationRepository> { com.revlens.app.data.fake.FakeIntegrationRepository() }
    single<com.revlens.app.domain.repository.DeveloperRepository> { com.revlens.app.data.fake.FakeDeveloperRepository() }
    single { com.revlens.app.domain.usecase.MigrateGuestDataUseCase(get(), get(), get(), get()) }
    single { com.revlens.app.domain.usecase.LoginUseCase(get(), get(), getOrNull()) }
    single { com.revlens.app.domain.usecase.SignUpUseCase(get(), get(), getOrNull()) }
    single { com.revlens.app.domain.usecase.RequestPasswordResetUseCase(get()) }
    single { com.revlens.app.domain.usecase.ValidateResetTokenUseCase(get()) }
    single { com.revlens.app.domain.usecase.ResetPasswordUseCase(get()) }
    single { com.revlens.app.domain.usecase.CreateWorkspaceUseCase(get(), get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), getOrNull(), getOrNull()) }
    viewModel { BusinessSetupViewModel() }
    viewModel {
        com.revlens.app.ui.screens.workspace.WorkspaceManagementViewModel(
            get(), get(), get(), get(), getOrNull(), getOrNull(), getOrNull()
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
    viewModel { com.revlens.app.ui.screens.auth.LoginViewModel(get(), get(), getOrNull(), getOrNull()) }
    viewModel { com.revlens.app.ui.screens.auth.InviteAcceptanceViewModel(get(), get(), get(), get(), getOrNull()) }
    viewModel { com.revlens.app.ui.screens.profile.ProfileViewModel(get(), get(), get(), get()) }
    viewModel { com.revlens.app.ui.screens.team.TeamViewModel(get(), get(), getOrNull(), getOrNull()) }
    viewModel { com.revlens.app.ui.screens.billing.BillingViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.integrations.IntegrationsViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.developer.ApiWebhooksViewModel(get(), get()) }
    viewModel { com.revlens.app.ui.screens.marketing.PublicPricingViewModel() }
    viewModel { com.revlens.app.ui.screens.marketing.LandingViewModel() }
}





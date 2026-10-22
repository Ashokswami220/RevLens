package com.revlens.app.di

import com.revlens.app.data.DefaultTeamRepository
import com.revlens.app.domain.model.SessionState
import com.revlens.app.data.DefaultIntegrationRepository
import com.revlens.app.data.DefaultDeveloperRepository
import com.revlens.app.data.DefaultBillingRepository
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
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
import com.revlens.app.data.AndroidFirebaseAuthRepository
import com.revlens.app.data.AndroidFirestoreProfileRepository
import com.revlens.app.data.AndroidFirestoreScenarioRepository
import com.revlens.app.data.AndroidFirestoreTeamRepository
import com.revlens.app.data.AndroidFirestoreWorkspaceRepository
import com.revlens.app.data.DataStoreProfileRepository
import com.revlens.app.data.DataStoreScenarioRepository
import com.revlens.app.data.DefaultAnalyticsRepository
import com.revlens.app.data.DefaultAppLockRepository
import com.revlens.app.data.DefaultExperimentRepository
import com.revlens.app.data.DefaultNotificationPreferencesRepository
import com.revlens.app.data.DefaultReportRepository
import com.revlens.app.data.DefaultWorkspaceRepository
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
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
import com.revlens.app.domain.repository.WorkspaceRepository
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
import com.revlens.app.ui.screens.security.SecuritySettingsViewModel
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
        AndroidFirebaseAuthRepository(
            context = androidContext(),
            sessionManager = get()
        )
    }
    single<TeamRepository> {
        AndroidFirestoreTeamRepository(
            workspaceContext = get(),
            profileRepository = get(),
            authRepository = get()
        )
    }
    single<BillingRepository> { DefaultBillingRepository() }
    single<IntegrationRepository> { DefaultIntegrationRepository() }
    single<DeveloperRepository> { DefaultDeveloperRepository() }
    single { MigrateGuestDataUseCase(get(), get(), get(), get()) }
    single { LoginUseCase(get(), get(), getOrNull()) }
    single { SignUpUseCase(get(), get(), getOrNull()) }
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

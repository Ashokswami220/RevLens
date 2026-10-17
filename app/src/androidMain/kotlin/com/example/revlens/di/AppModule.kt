package com.example.revlens.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.revlens.data.DataStoreProfileRepository
import com.example.revlens.data.DataStoreScenarioRepository
import com.example.revlens.data.DefaultWorkspaceRepository
import com.example.revlens.data.ProfileRepository
import com.example.revlens.data.ScenarioRepository
import com.example.revlens.domain.context.DefaultWorkspaceContext
import com.example.revlens.domain.context.WorkspaceContext
import com.example.revlens.domain.repository.WorkspaceRepository
import com.example.revlens.domain.session.DefaultSessionManager
import com.example.revlens.domain.session.SessionManager
import com.example.revlens.domain.usecase.ResolveGateUseCase
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.screens.detailscreens.BusinessSetupViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "revlens_settings")

val appModule = module {
    single<DataStore<Preferences>> { androidContext().dataStore }
    single<ProfileRepository> { DataStoreProfileRepository(get()) }
    single<ScenarioRepository> { DataStoreScenarioRepository(get()) }
    single<WorkspaceRepository> { DefaultWorkspaceRepository() }
    single<WorkspaceContext> { DefaultWorkspaceContext() }
    single<SessionManager> { DefaultSessionManager() }
    single { ResolveGateUseCase() }
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

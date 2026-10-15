package com.example.revlens.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.revlens.data.ProfileRepository
import com.example.revlens.data.ScenarioRepository
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.screens.detailscreens.BusinessSetupViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "revlens_settings")

val appModule = module {
    single<DataStore<Preferences>> { androidContext().dataStore }
    single { ProfileRepository(get()) }
    single { ScenarioRepository(get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { BusinessSetupViewModel() }
}

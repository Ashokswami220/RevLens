package com.revlens.app

import android.app.Application
import com.revlens.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RevLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (_: Exception) {}
        startKoin {
            androidContext(this@RevLensApplication)
            modules(appModule)
        }
    }
}

package com.example.revlens

import android.app.Application
import com.example.revlens.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RevLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RevLensApplication)
            modules(appModule)
        }
    }
}

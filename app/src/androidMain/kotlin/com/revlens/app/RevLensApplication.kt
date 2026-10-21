package com.revlens.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.revlens.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RevLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .build()
                )
                .build()
            FirebaseFirestore.getInstance().firestoreSettings =
                settings
        } catch (_: Throwable) {
        }
        startKoin {
            androidContext(this@RevLensApplication)
            modules(appModule)
        }
    }
}

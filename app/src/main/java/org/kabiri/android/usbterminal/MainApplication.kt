package org.kabiri.android.usbterminal

import android.app.Application
import org.kabiri.android.usbterminal.koin.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Created by Ali Kabiri on 13.04.20.
 */
class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // start Koin context.
        startKoin{
            androidContext(this@MainApplication)
            androidLogger(Level.DEBUG)
            modules(appModule)
        }
    }
}
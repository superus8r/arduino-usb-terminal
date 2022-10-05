package org.kabiri.android.usbterminal

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Created by Ali Kabiri on 13.04.20.
 */
@HiltAndroidApp
class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
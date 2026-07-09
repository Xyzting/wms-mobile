package com.utb.wms

import android.app.Application
import com.utb.wms.di.AppContainer

class WmsApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

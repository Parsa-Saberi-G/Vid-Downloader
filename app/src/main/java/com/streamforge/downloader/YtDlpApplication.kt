package com.streamforge.downloader

import android.app.Application
import com.streamforge.downloader.repository.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class YtDlpApplication : Application() {
    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            container.toolManager.initialize()
        }
    }
}

package com.streamforge.downloader

import android.app.Application
import com.streamforge.downloader.repository.AppContainer

class YtDlpApplication : Application() {
    val container by lazy { AppContainer(this) }
}

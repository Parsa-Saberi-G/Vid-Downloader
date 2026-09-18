package com.parsa.ytdlp

import android.app.Application
import com.parsa.ytdlp.repository.AppContainer

class YtDlpApplication : Application() {
    val container by lazy { AppContainer(this) }
}

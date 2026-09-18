package com.parsa.ytdlp.repository

import android.content.Context
import com.parsa.ytdlp.downloader.ProcessYtDlpEngine
import com.parsa.ytdlp.downloader.YtDlpEngine
import com.parsa.ytdlp.model.DownloadItem
import com.parsa.ytdlp.model.DownloadOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.UUID

class DownloadRepository(private val engine: YtDlpEngine) {
    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()
    suspend fun analyze(url: String) = engine.extractInfo(url)
    fun start(scope: CoroutineScope, url: String, options: DownloadOptions = DownloadOptions()): String {
        val id = UUID.randomUUID().toString()
        _downloads.update { listOf(DownloadItem(id, url, status = com.parsa.ytdlp.model.DownloadStatus.ANALYZING)) + it }
        scope.launch(Dispatchers.IO) {
            engine.download(id, url, options).collect { event -> _downloads.update { items -> items.map { item -> if (item.id != id) item else when (event) {
                is com.parsa.ytdlp.downloader.YtDlpEvent.Progress -> item.copy(status = com.parsa.ytdlp.model.DownloadStatus.DOWNLOADING, progress = event.percent, fileName = event.fileName, speed = event.speed, eta = event.eta)
                is com.parsa.ytdlp.downloader.YtDlpEvent.Completed -> item.copy(status = com.parsa.ytdlp.model.DownloadStatus.COMPLETED, progress = 1f, fileName = event.fileName, completedAt = System.currentTimeMillis())
                is com.parsa.ytdlp.downloader.YtDlpEvent.Failed -> item.copy(status = com.parsa.ytdlp.model.DownloadStatus.FAILED, errorMessage = event.message)
            } } } }
        }
        return id
    }
    fun cancel(id: String) { engine.cancel(id); _downloads.update { items -> items.map { if (it.id == id) it.copy(status = com.parsa.ytdlp.model.DownloadStatus.CANCELLED) else it } } }
}

class AppContainer(context: Context) {
    val downloadRepository = DownloadRepository(ProcessYtDlpEngine(context.applicationContext))
}

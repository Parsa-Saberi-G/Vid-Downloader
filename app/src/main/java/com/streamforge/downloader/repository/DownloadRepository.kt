package com.streamforge.downloader.repository

import android.content.Context
import com.streamforge.downloader.downloader.ProcessYtDlpEngine
import com.streamforge.downloader.downloader.YtDlpEngine
import com.streamforge.downloader.manager.ToolManager
import com.streamforge.downloader.model.DownloadItem
import com.streamforge.downloader.model.DownloadOptions
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
        _downloads.update { listOf(DownloadItem(id, url, status = com.streamforge.downloader.model.DownloadStatus.ANALYZING)) + it }
        scope.launch(Dispatchers.IO) {
            try {
                engine.download(id, url, options).collect { event -> _downloads.update { items -> items.map { item -> if (item.id != id) item else when (event) {
                    is com.streamforge.downloader.downloader.YtDlpEvent.Progress -> item.copy(status = com.streamforge.downloader.model.DownloadStatus.DOWNLOADING, progress = event.percent, fileName = event.fileName, speed = event.speed, eta = event.eta)
                    is com.streamforge.downloader.downloader.YtDlpEvent.Completed -> item.copy(status = com.streamforge.downloader.model.DownloadStatus.COMPLETED, progress = 1f, fileName = event.fileName, completedAt = System.currentTimeMillis())
                    is com.streamforge.downloader.downloader.YtDlpEvent.Failed -> item.copy(status = com.streamforge.downloader.model.DownloadStatus.FAILED, errorMessage = event.message)
                } } } }
            } catch (error: Exception) {
                _downloads.update { items -> items.map { item ->
                    if (item.id == id) item.copy(status = com.streamforge.downloader.model.DownloadStatus.FAILED, errorMessage = error.message ?: "Download failed")
                    else item
                } }
            }
        }
        return id
    }
    fun cancel(id: String) { engine.cancel(id); _downloads.update { items -> items.map { if (it.id == id) it.copy(status = com.streamforge.downloader.model.DownloadStatus.CANCELLED) else it } } }
}

class AppContainer(context: Context) {
    val toolManager = ToolManager(context)
    val dependencyManager = com.streamforge.downloader.manager.DependencyManager(toolManager)
    val settingsRepository = SettingsRepository(context)
    val errorHandler = com.streamforge.downloader.util.ErrorHandler()
    val downloadRepository = DownloadRepository(
        ProcessYtDlpEngine(context.applicationContext, toolManager)
    )
}

package com.streamforge.downloader.downloader

import com.streamforge.downloader.model.DownloadOptions
import com.streamforge.downloader.model.MediaInfo
import kotlinx.coroutines.flow.Flow

interface YtDlpEngine {
    suspend fun isAvailable(): Boolean
    suspend fun getVersion(): String?
    suspend fun extractInfo(url: String): Result<MediaInfo>
    fun download(id: String, url: String, options: DownloadOptions): Flow<YtDlpEvent>
    fun cancel(id: String)
}

sealed interface YtDlpEvent {
    data class Progress(val percent: Float, val fileName: String, val speed: String, val eta: String) : YtDlpEvent
    data class Completed(val fileName: String) : YtDlpEvent
    data class Failed(val message: String) : YtDlpEvent
}

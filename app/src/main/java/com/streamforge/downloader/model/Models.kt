package com.streamforge.downloader.model

enum class DownloadStatus { QUEUED, ANALYZING, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED }

data class DownloadItem(
    val id: String,
    val url: String,
    val fileName: String = "Preparing…",
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0,
    val totalBytes: Long? = null,
    val speed: String = "—",
    val eta: String = "—",
    val errorMessage: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

data class MediaInfo(val title: String, val uploader: String? = null, val duration: String? = null, val formats: List<MediaFormat> = emptyList())
data class MediaFormat(val id: String, val label: String, val extension: String, val hasVideo: Boolean, val hasAudio: Boolean)
data class DownloadOptions(
    val formatId: String? = null,
    val outputDirectory: String? = null,
    val audioOnly: Boolean = false,
    val customArguments: List<String> = emptyList(),
    val cookiesFile: String? = null,
    val proxy: String? = null,
    val userAgent: String? = null,
    val ffmpegPath: String? = null,
    val postProcessingEnabled: Boolean = true
)

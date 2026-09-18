package com.streamforge.downloader.model

enum class DependencyId(val displayName: String) {
    YT_DLP("yt-dlp"),
    FFMPEG("ffmpeg")
}

enum class DependencyState { INSTALLED, MISSING, UPDATE_AVAILABLE, ERROR, INSTALLING }

data class DependencyStatus(
    val id: DependencyId,
    val state: DependencyState,
    val path: String? = null,
    val version: String? = null,
    val message: String? = null
)

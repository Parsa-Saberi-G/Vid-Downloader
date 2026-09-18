package com.streamforge.downloader.downloader

import com.streamforge.downloader.model.MediaFormat
import com.streamforge.downloader.model.MediaInfo

object YtDlpParser {
    fun parseProgress(line: String): YtDlpEvent.Progress? {
        val match = Regex("(?<percent>\\d+(?:\\.\\d+)?)%.*?(?:at\\s+)?(?<speed>\\S+/s)?.*?(?:ETA\\s+(?<eta>\\S+))?", RegexOption.IGNORE_CASE).find(line) ?: return null
        return YtDlpEvent.Progress(match.groups["percent"]!!.value.toFloat() / 100f, "Downloading", match.groups["speed"]?.value ?: "—", match.groups["eta"]?.value ?: "—")
    }
    fun parseInfo(title: String, uploader: String? = null, duration: String? = null) = MediaInfo(title, uploader, duration, listOf(MediaFormat("best", "Best available", "mp4", true, true)))
}

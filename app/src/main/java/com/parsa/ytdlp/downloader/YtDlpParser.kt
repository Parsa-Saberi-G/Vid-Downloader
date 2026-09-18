package com.parsa.ytdlp.downloader

import com.parsa.ytdlp.model.MediaFormat
import com.parsa.ytdlp.model.MediaInfo

object YtDlpParser {
    fun parseProgress(line: String): YtDlpEvent.Progress? {
        val match = Regex("(?<percent>\\d+(?:\\.\\d+)?)%.*?(?:at\\s+)?(?<speed>\\S+/s)?.*?(?:ETA\\s+(?<eta>\\S+))?", RegexOption.IGNORE_CASE).find(line) ?: return null
        return YtDlpEvent.Progress(match.groups["percent"]!!.value.toFloat() / 100f, "Downloading", match.groups["speed"]?.value ?: "—", match.groups["eta"]?.value ?: "—")
    }
    fun parseInfo(title: String, uploader: String? = null, duration: String? = null) = MediaInfo(title, uploader, duration, listOf(MediaFormat("best", "Best available", "mp4", true, true)))
}

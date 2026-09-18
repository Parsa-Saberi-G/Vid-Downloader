package com.streamforge.downloader.util

object FilenameSanitizer {
    fun sanitize(value: String, fallback: String = "download"): String {
        val clean = value.replace(Regex("[\\u0000-\\u001F\\\\/:*?\"<>|]"), "_").trim().trim('.')
        return clean.ifBlank { fallback }.take(180)
    }
}

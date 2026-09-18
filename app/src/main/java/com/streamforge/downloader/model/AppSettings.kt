package com.streamforge.downloader.model

data class AppSettings(
    val downloadFolder: String,
    val defaultQuality: String,
    val defaultFormat: String,
    val autoUpdateYtDlp: Boolean,
    val autoCheckDependencies: Boolean,
    val customYtDlpArguments: String,
    val cookiesFile: String,
    val proxy: String,
    val userAgent: String,
    val ffmpegPath: String,
    val postProcessingEnabled: Boolean,
    val theme: String,
    val dynamicColors: Boolean,
    val compactUi: Boolean,
    val debugLogs: Boolean
) {
    companion object {
        fun defaults(downloadFolder: String) = AppSettings(
            downloadFolder = downloadFolder,
            defaultQuality = "Best",
            defaultFormat = "mp4",
            autoUpdateYtDlp = true,
            autoCheckDependencies = true,
            customYtDlpArguments = "",
            cookiesFile = "",
            proxy = "",
            userAgent = "",
            ffmpegPath = "",
            postProcessingEnabled = true,
            theme = "System",
            dynamicColors = true,
            compactUi = false,
            debugLogs = false
        )
    }
}

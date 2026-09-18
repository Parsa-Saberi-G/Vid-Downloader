package com.streamforge.downloader.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.streamforge.downloader.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

private val Context.settingsDataStore by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val downloadFolder = stringPreferencesKey("download_folder")
        val defaultQuality = stringPreferencesKey("default_quality")
        val defaultFormat = stringPreferencesKey("default_format")
        val autoUpdate = booleanPreferencesKey("auto_update")
        val autoCheck = booleanPreferencesKey("auto_check")
        val arguments = stringPreferencesKey("yt_dlp_arguments")
        val cookies = stringPreferencesKey("cookies_file")
        val proxy = stringPreferencesKey("proxy")
        val userAgent = stringPreferencesKey("user_agent")
        val ffmpeg = stringPreferencesKey("ffmpeg_path")
        val postProcessing = booleanPreferencesKey("post_processing")
        val theme = stringPreferencesKey("theme")
        val dynamicColors = booleanPreferencesKey("dynamic_colors")
        val compactUi = booleanPreferencesKey("compact_ui")
        val debugLogs = booleanPreferencesKey("debug_logs")
    }

    private val defaultFolder = File(
        context.getExternalFilesDir("Download") ?: context.filesDir,
        "Forge Stream"
    ).absolutePath
    private val defaultFfmpeg = File(
        context.filesDir,
        "forge-stream/bin/ffmpeg"
    ).absolutePath

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        preferences.toSettings()
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { preferences ->
            val updated = transform(preferences.toSettings())
            preferences[Keys.downloadFolder] = updated.downloadFolder
            preferences[Keys.defaultQuality] = updated.defaultQuality
            preferences[Keys.defaultFormat] = updated.defaultFormat
            preferences[Keys.autoUpdate] = updated.autoUpdateYtDlp
            preferences[Keys.autoCheck] = updated.autoCheckDependencies
            preferences[Keys.arguments] = updated.customYtDlpArguments
            preferences[Keys.cookies] = updated.cookiesFile
            preferences[Keys.proxy] = updated.proxy
            preferences[Keys.userAgent] = updated.userAgent
            preferences[Keys.ffmpeg] = updated.ffmpegPath
            preferences[Keys.postProcessing] = updated.postProcessingEnabled
            preferences[Keys.theme] = updated.theme
            preferences[Keys.dynamicColors] = updated.dynamicColors
            preferences[Keys.compactUi] = updated.compactUi
            preferences[Keys.debugLogs] = updated.debugLogs
        }
    }

    suspend fun reset() {
        context.settingsDataStore.edit { it.clear() }
    }

    private fun Preferences.toSettings() = AppSettings(
        downloadFolder = this[Keys.downloadFolder] ?: defaultFolder,
        defaultQuality = this[Keys.defaultQuality] ?: "Best",
        defaultFormat = this[Keys.defaultFormat] ?: "mp4",
        autoUpdateYtDlp = this[Keys.autoUpdate] ?: true,
        autoCheckDependencies = this[Keys.autoCheck] ?: true,
        customYtDlpArguments = this[Keys.arguments] ?: "",
        cookiesFile = this[Keys.cookies] ?: "",
        proxy = this[Keys.proxy] ?: "",
        userAgent = this[Keys.userAgent] ?: "",
        ffmpegPath = this[Keys.ffmpeg] ?: defaultFfmpeg,
        postProcessingEnabled = this[Keys.postProcessing] ?: true,
        theme = this[Keys.theme] ?: "System",
        dynamicColors = this[Keys.dynamicColors] ?: true,
        compactUi = this[Keys.compactUi] ?: false,
        debugLogs = this[Keys.debugLogs] ?: false
    )
}

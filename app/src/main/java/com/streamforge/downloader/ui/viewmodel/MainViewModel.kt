package com.streamforge.downloader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamforge.downloader.manager.DependencyManager
import com.streamforge.downloader.model.AppSettings
import com.streamforge.downloader.model.DependencyId
import com.streamforge.downloader.model.DependencyStatus
import com.streamforge.downloader.model.DownloadOptions
import com.streamforge.downloader.model.MediaInfo
import com.streamforge.downloader.repository.DownloadRepository
import com.streamforge.downloader.repository.SettingsRepository
import com.streamforge.downloader.util.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: DownloadRepository,
    private val settingsRepository: SettingsRepository,
    private val dependencyManager: DependencyManager,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    val downloads = repository.downloads
    val settings = settingsRepository.settings
    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()
    private val _info = MutableStateFlow<MediaInfo?>(null)
    val info: StateFlow<MediaInfo?> = _info.asStateFlow()
    private val _dependencies = MutableStateFlow(
        DependencyId.entries.map { DependencyStatus(it, com.streamforge.downloader.model.DependencyState.MISSING) }
    )
    val dependencies: StateFlow<List<DependencyStatus>> = _dependencies.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            settings.collect { value ->
                if (value.autoCheckDependencies) refreshDependencies(value)
            }
        }
        viewModelScope.launch { errorHandler.messages.collect { _message.value = it } }
    }

    fun setUrl(value: String) { _url.value = value; _message.value = null }

    fun analyze() = viewModelScope.launch {
        if (_url.value.isBlank()) {
            _message.value = "Enter a URL first."
            return@launch
        }
        repository.analyze(_url.value).onSuccess { _info.value = it }
            .onFailure { errorHandler.report("analyze", it, "Install yt-dlp and check the URL.") }
    }

    fun download(audioOnly: Boolean = false) {
        val currentUrl = _url.value
        if (currentUrl.isBlank()) {
            _message.value = "Enter a URL first."
            return
        }
        viewModelScope.launch {
            val value = settings.first()
            val format = when (value.defaultQuality) {
                "1080p" -> "bestvideo[height<=1080]+bestaudio/best[height<=1080]"
                "720p" -> "bestvideo[height<=720]+bestaudio/best[height<=720]"
                "480p" -> "bestvideo[height<=480]+bestaudio/best[height<=480]"
                "Audio only" -> "bestaudio"
                else -> "bestvideo+bestaudio/best"
            }
            repository.start(viewModelScope, currentUrl, DownloadOptions(
                formatId = format,
                outputDirectory = value.downloadFolder,
                audioOnly = audioOnly,
                customArguments = value.customYtDlpArguments.split(" ").filter(String::isNotBlank),
                cookiesFile = value.cookiesFile,
                proxy = value.proxy,
                userAgent = value.userAgent,
                ffmpegPath = value.ffmpegPath,
                postProcessingEnabled = value.postProcessingEnabled
            ))
        }
    }

    fun cancel(id: String) = repository.cancel(id)
    fun refreshDependencies(settings: AppSettings? = null) = viewModelScope.launch {
        _dependencies.value = dependencyManager.checkAll(settings?.ffmpegPath ?: "")
    }
    fun install(id: DependencyId) = viewModelScope.launch {
        _message.value = "Installing ${id.displayName}..."
        dependencyManager.install(id).onSuccess { refreshDependencies() }
            .onFailure { errorHandler.report("install-${id.name}", it, "Check your network connection or configure a compatible binary path.") }
    }
    fun remove(id: DependencyId) {
        dependencyManager.remove(id)
        refreshDependencies()
    }
    fun updateSettings(transform: (AppSettings) -> AppSettings) = viewModelScope.launch {
        settingsRepository.update(transform)
    }
    fun resetSettings() = viewModelScope.launch { settingsRepository.reset() }
}

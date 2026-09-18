package com.streamforge.downloader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamforge.downloader.model.DownloadOptions
import com.streamforge.downloader.model.MediaInfo
import com.streamforge.downloader.repository.DownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: DownloadRepository) : ViewModel() {
    val downloads = repository.downloads
    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()
    private val _info = MutableStateFlow<MediaInfo?>(null)
    val info: StateFlow<MediaInfo?> = _info.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    fun setUrl(value: String) { _url.value = value; _message.value = null }
    fun analyze() = viewModelScope.launch { if (_url.value.isBlank()) { _message.value = "Enter a URL first"; return@launch }; repository.analyze(_url.value).onSuccess { _info.value = it }.onFailure { _message.value = it.message } }
    fun download() { if (_url.value.isNotBlank()) repository.start(viewModelScope, _url.value, DownloadOptions(_info.value?.formats?.firstOrNull()?.id)) }
    fun cancel(id: String) = repository.cancel(id)
}

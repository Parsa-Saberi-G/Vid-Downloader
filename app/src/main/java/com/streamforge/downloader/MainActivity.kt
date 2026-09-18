package com.streamforge.downloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.streamforge.downloader.ui.App
import com.streamforge.downloader.ui.viewmodel.MainViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as YtDlpApplication).container
        setContent {
            App(viewModel(factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        MainViewModel(container.downloadRepository, container.settingsRepository, container.dependencyManager, container.errorHandler) as T
                }))
        }
    }
}

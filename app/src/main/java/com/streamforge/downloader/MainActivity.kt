package com.streamforge.downloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.streamforge.downloader.ui.App
import com.streamforge.downloader.ui.theme.YtDlpTheme
import com.streamforge.downloader.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as YtDlpApplication).container
        setContent {
            YtDlpTheme { App(viewModel { MainViewModel(container.downloadRepository) }) }
        }
    }
}

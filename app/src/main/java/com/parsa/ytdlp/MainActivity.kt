package com.parsa.ytdlp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parsa.ytdlp.ui.App
import com.parsa.ytdlp.ui.theme.YtDlpTheme
import com.parsa.ytdlp.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as YtDlpApplication).container
        setContent {
            YtDlpTheme { App(viewModel { MainViewModel(container.downloadRepository) }) }
        }
    }
}

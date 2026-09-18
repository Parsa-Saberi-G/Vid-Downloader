package com.parsa.ytdlp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parsa.ytdlp.model.DownloadItem
import com.parsa.ytdlp.ui.viewmodel.MainViewModel

@Composable fun App(vm: MainViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(topBar = { TopAppBar(title = { Text("YT-DLP Downloader") }) }, bottomBar = { NavigationBar { NavigationBarItem(tab == 0, { tab = 0 }, label = { Text("Home") }, icon = { Text("⌂") }); NavigationBarItem(tab == 1, { tab = 1 }, label = { Text("Downloads") }, icon = { Text("↓") }); NavigationBarItem(tab == 2, { tab = 2 }, label = { Text("Settings") }, icon = { Text("⚙") }) } }) { padding -> when (tab) { 0 -> HomeScreen(vm, Modifier.padding(padding)); 1 -> DownloadsScreen(vm, Modifier.padding(padding)); else -> SettingsScreen(Modifier.padding(padding)) } }
}

@Composable private fun HomeScreen(vm: MainViewModel, modifier: Modifier) {
    val url by vm.url.collectAsStateWithLifecycle(); val info by vm.info.collectAsStateWithLifecycle(); val message by vm.message.collectAsStateWithLifecycle(); val downloads by vm.downloads.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(url, vm::setUrl, Modifier.fillMaxWidth(), label = { Text("Media URL") }, singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = vm::analyze) { Text("Analyze") }; OutlinedButton(onClick = { /* clipboard integration belongs here */ }) { Text("Paste") } }
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        info?.let { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(it.title, style = MaterialTheme.typography.titleMedium); it.uploader?.let { Text(it) }; it.duration?.let { Text("Duration: $it") }; Spacer(Modifier.height(8.dp)); Button(onClick = vm::download) { Text("Download") } } } }
        Text("Recent downloads", style = MaterialTheme.typography.titleLarge)
        downloads.take(3).forEach { DownloadRow(it, vm::cancel) }
    }
}

@Composable private fun DownloadsScreen(vm: MainViewModel, modifier: Modifier) { val downloads by vm.downloads.collectAsStateWithLifecycle(); LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(downloads, key = { it.id }) { DownloadRow(it, vm::cancel) } } }
@Composable private fun DownloadRow(item: DownloadItem, cancel: (String) -> Unit) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(item.fileName, style = MaterialTheme.typography.titleMedium); Text(item.status.name); LinearProgressIndicator({ item.progress }, Modifier.fillMaxWidth()); if (item.status.name == "DOWNLOADING") { Text("${(item.progress * 100).toInt()}%  ${item.speed}  ETA ${item.eta}"); TextButton({ cancel(item.id) }) { Text("Cancel") } }; item.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) } } } }
@Composable private fun SettingsScreen(modifier: Modifier) { Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Settings", style = MaterialTheme.typography.headlineMedium); Text("Download directory: Android Storage Access Framework"); Text("Default quality: Best available"); Text("Maximum concurrent downloads: 1"); Text("yt-dlp: Not configured"); Text("FFmpeg: Not configured"); Text("About: A safe, open yt-dlp frontend") } }

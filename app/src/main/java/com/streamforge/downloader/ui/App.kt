@file:OptIn(ExperimentalMaterial3Api::class)

package com.streamforge.downloader.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streamforge.downloader.model.AppSettings
import com.streamforge.downloader.model.DependencyState
import com.streamforge.downloader.model.DownloadItem
import com.streamforge.downloader.ui.theme.YtDlpTheme
import com.streamforge.downloader.ui.viewmodel.MainViewModel

@Composable
fun App(vm: MainViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle(initialValue = AppSettings.defaults(""))
    YtDlpTheme(settings) { AppContent(vm) }
}

@Composable
private fun AppContent(vm: MainViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Forge Stream") }) },
        bottomBar = {
            NavigationBar {
                listOf("Home", "Downloads", "Settings").forEachIndexed { index, label ->
                    NavigationBarItem(tab == index, { tab = index }, label = { Text(label) }, icon = { Text(label.take(1)) })
                }
            }
        }
    ) { padding ->
        when (tab) {
            0 -> HomeScreen(vm, Modifier.padding(padding)) { tab = 2 }
            1 -> DownloadsScreen(vm, Modifier.padding(padding))
            else -> SettingsScreen(vm, Modifier.padding(padding))
        }
    }
}

@Composable
private fun HomeScreen(vm: MainViewModel, modifier: Modifier, openSettings: () -> Unit) {
    val url by vm.url.collectAsStateWithLifecycle()
    val info by vm.info.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val downloads by vm.downloads.collectAsStateWithLifecycle()
    val dependencies by vm.dependencies.collectAsStateWithLifecycle()
    val toolState by vm.toolState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (toolState.isInitializing) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(toolState.progress)
        }
        val missing = dependencies.filter { it.state != DependencyState.INSTALLED }
        if (missing.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Setup check", style = MaterialTheme.typography.titleMedium)
                    Text("Missing: ${missing.joinToString { it.id.displayName }}")
                    Text("The included download components are unavailable. Reinstall the app or open Settings for details.")
                    Button(onClick = openSettings) { Text("View details") }
                }
            }
        }
        OutlinedTextField(url, vm::setUrl, Modifier.fillMaxWidth(), label = { Text("Media URL") }, singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::analyze) { Text("Analyze") }
            OutlinedButton(onClick = { }) { Text("Paste") }
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        info?.let {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(it.title, style = MaterialTheme.typography.titleMedium)
                    it.uploader?.let { uploader -> Text(uploader) }
                    it.duration?.let { duration -> Text("Duration: $duration") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { vm.download(false) }) { Text("Download video") }
                        OutlinedButton(onClick = { vm.download(true) }) { Text("Audio") }
                    }
                }
            }
        }
        Text("Recent downloads", style = MaterialTheme.typography.titleLarge)
        downloads.take(3).forEach { DownloadRow(it, vm::cancel) }
    }
}

@Composable
private fun DownloadsScreen(vm: MainViewModel, modifier: Modifier) {
    val downloads by vm.downloads.collectAsStateWithLifecycle()
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Downloads", style = MaterialTheme.typography.headlineMedium) }
        items(downloads, key = { it.id }) { DownloadRow(it, vm::cancel) }
    }
}

@Composable
private fun DownloadRow(item: DownloadItem, cancel: (String) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.fileName, style = MaterialTheme.typography.titleMedium)
            Text(item.status.name)
            LinearProgressIndicator(progress = { item.progress }, modifier = Modifier.fillMaxWidth())
            if (item.status.name == "DOWNLOADING") {
                Text("${(item.progress * 100).toInt()}%  ${item.speed}  ETA ${item.eta}")
                TextButton({ cancel(item.id) }) { Text("Cancel") }
            }
            item.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun SettingsScreen(vm: MainViewModel, modifier: Modifier) {
    val settings by vm.settings.collectAsStateWithLifecycle(initialValue = AppSettings.defaults(""))
    val dependencies by vm.dependencies.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium) }
        item { Text("Built-in components", style = MaterialTheme.typography.titleLarge) }
        item {
            dependencies.forEach { dependency ->
                val installed = dependency.state == DependencyState.INSTALLED
                ListItem(
                    headlineContent = { Text(dependency.id.displayName) },
                    supportingContent = {
                        Text(
                            if (installed) {
                                "${dependency.version ?: "Ready"}\n${dependency.path}\nIncluded and configured automatically"
                            } else {
                                "${dependency.state}: ${dependency.path}\n${dependency.message ?: "Included component unavailable"}"
                            }
                        )
                    }
                )
            }
            OutlinedButton(onClick = vm::refreshDependencies) { Text("Check again") }
            OutlinedButton(onClick = vm::reinstallTools) { Text("Reinstall built-in tools") }
        }
        item { Text("General", style = MaterialTheme.typography.titleLarge) }
        item { SettingTextField("Download folder", settings.downloadFolder) { text -> vm.updateSettings { value -> value.copy(downloadFolder = text) } } }
        item {
            SettingDropdown("Default quality", settings.defaultQuality, listOf("Best", "1080p", "720p", "480p", "Audio only")) {
                vm.updateSettings { value -> value.copy(defaultQuality = it) }
            }
        }
        item {
            SettingDropdown("Default format", settings.defaultFormat, listOf("mp4", "mkv", "webm", "mp3", "m4a")) {
                vm.updateSettings { value -> value.copy(defaultFormat = it) }
            }
        }
        item { SettingSwitch("Auto-update yt-dlp", settings.autoUpdateYtDlp) { vm.updateSettings { it.copy(autoUpdateYtDlp = !it.autoUpdateYtDlp) } } }
        item { SettingSwitch("Auto-check dependencies", settings.autoCheckDependencies) { vm.updateSettings { it.copy(autoCheckDependencies = !it.autoCheckDependencies) } } }
        item { Text("Download options", style = MaterialTheme.typography.titleLarge) }
        item { SettingTextField("Custom arguments", settings.customYtDlpArguments) { vm.updateSettings { value -> value.copy(customYtDlpArguments = it) } } }
        item { SettingTextField("Cookies file", settings.cookiesFile) { vm.updateSettings { value -> value.copy(cookiesFile = it) } } }
        item { SettingTextField("Proxy", settings.proxy) { vm.updateSettings { value -> value.copy(proxy = it) } } }
        item { SettingTextField("User agent", settings.userAgent) { vm.updateSettings { value -> value.copy(userAgent = it) } } }
        item { Text("Media processing is included and configured automatically.", style = MaterialTheme.typography.bodyMedium) }
        item { SettingSwitch("Enable post-processing", settings.postProcessingEnabled) { vm.updateSettings { it.copy(postProcessingEnabled = !it.postProcessingEnabled) } } }
        item { Text("Appearance & advanced", style = MaterialTheme.typography.titleLarge) }
        item {
            SettingDropdown("Theme", settings.theme, listOf("System", "Light", "Dark")) {
                vm.updateSettings { value -> value.copy(theme = it) }
            }
        }
        item { SettingSwitch("Dynamic colors", settings.dynamicColors) { vm.updateSettings { it.copy(dynamicColors = !it.dynamicColors) } } }
        item { SettingSwitch("Compact UI", settings.compactUi) { vm.updateSettings { it.copy(compactUi = !it.compactUi) } } }
        item { SettingSwitch("Debug logs", settings.debugLogs) { vm.updateSettings { it.copy(debugLogs = !it.debugLogs) } } }
        item {
            OutlinedButton(onClick = {
                clipboard.setText(AnnotatedString(settings.toExportText()))
            }) { Text("Export settings") }
        }
        item { OutlinedButton(onClick = vm::resetSettings) { Text("Reset settings") } }
    }

}

private fun AppSettings.toExportText() = buildString {
    appendLine("downloadFolder=$downloadFolder")
    appendLine("defaultQuality=$defaultQuality")
    appendLine("defaultFormat=$defaultFormat")
    appendLine("autoUpdateYtDlp=$autoUpdateYtDlp")
    appendLine("autoCheckDependencies=$autoCheckDependencies")
    appendLine("customYtDlpArguments=$customYtDlpArguments")
    appendLine("cookiesFile=$cookiesFile")
    appendLine("proxy=$proxy")
    appendLine("userAgent=$userAgent")
    appendLine("ffmpegPath=$ffmpegPath")
    appendLine("postProcessingEnabled=$postProcessingEnabled")
    appendLine("theme=$theme")
    appendLine("dynamicColors=$dynamicColors")
    appendLine("compactUi=$compactUi")
    appendLine("debugLogs=$debugLogs")
}

@Composable
private fun SettingTextField(label: String, value: String, onSave: (String) -> Unit) {
    var draft by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = draft,
        onValueChange = { draft = it; onSave(it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun SettingDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("$label: $selected", modifier = Modifier.fillMaxWidth())
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onToggle: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() }
            )
        }
    )
}

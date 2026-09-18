package com.streamforge.downloader.manager

import android.content.Context
import android.os.Build
import android.util.Log
import com.streamforge.downloader.model.DependencyId
import com.streamforge.downloader.model.DependencyState
import com.streamforge.downloader.model.DependencyStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

data class ToolInitializationState(
    val isReady: Boolean = false,
    val isInitializing: Boolean = false,
    val progress: String = "Preparing built-in tools",
    val error: String? = null
)

class ToolManager(context: Context) {
    private val appContext = context.applicationContext
    private val binDirectory = File(appContext.filesDir, "forge-stream/bin")
    private val mutex = Mutex()
    private val _state = MutableStateFlow(ToolInitializationState())
    val state: StateFlow<ToolInitializationState> = _state.asStateFlow()

    val ytDlpPath: File get() = File(binDirectory, "yt-dlp")
    val ffmpegPath: File get() = File(binDirectory, "ffmpeg")

    suspend fun initialize(force: Boolean = false): Result<List<DependencyStatus>> =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                runCatching {
                    _state.value = ToolInitializationState(isInitializing = true, progress = "Detecting device ABI")
                    val abi = supportedAbi()
                    check(abi != null) { "This device ABI is not supported. Use arm64-v8a or x86_64." }
                    check(binDirectory.exists() || binDirectory.mkdirs()) {
                        "Unable to create app tool directory"
                    }
                    val statuses = listOf(
                        provision(DependencyId.YT_DLP, abi, force),
                        provision(DependencyId.FFMPEG, abi, force)
                    )
                    check(statuses.all { it.state == DependencyState.INSTALLED }) {
                        statuses.firstOrNull { it.state != DependencyState.INSTALLED }?.message
                            ?: "Built-in tools could not be started"
                    }
                    _state.value = ToolInitializationState(isReady = true, progress = "Built-in tools ready")
                    statuses
                }.onFailure { error ->
                    Log.e(TAG, "Tool initialization failed", error)
                    _state.value = ToolInitializationState(
                        progress = "Tool setup failed",
                        error = error.message ?: "Unable to prepare built-in tools"
                    )
                }
            }
        }

    suspend fun statuses(): List<DependencyStatus> {
        val result = initialize()
        return result.getOrElse {
            listOf(
                statusFor(DependencyId.YT_DLP, it.message),
                statusFor(DependencyId.FFMPEG, it.message)
            )
        }
    }

    private fun provision(id: DependencyId, abi: String, force: Boolean): DependencyStatus {
        _state.value = _state.value.copy(
            progress = "Preparing ${id.displayName.lowercase()}"
        )
        val target = path(id)
        return runCatching {
            if (force || !target.isFile || target.length() == 0L) {
                val temporary = File(binDirectory, "${target.name}.tmp")
                val assetPath = "forge-stream/$abi/${target.name}"
                appContext.assets.open(assetPath).use { input ->
                    temporary.outputStream().use { output -> input.copyTo(output) }
                }
                check(temporary.length() > 0) { "Bundled ${id.displayName} is empty" }
                if (target.exists()) check(target.delete()) { "Unable to replace ${target.name}" }
                check(temporary.renameTo(target)) { "Unable to prepare ${target.name}" }
            }
            check(target.setExecutable(true, false) || target.canExecute()) {
                "Unable to mark ${id.displayName} executable"
            }
            val version = readVersion(target)
            DependencyStatus(
                id = id,
                state = DependencyState.INSTALLED,
                path = target.absolutePath,
                version = version
            )
        }.getOrElse { error ->
            statusFor(id, error.message ?: "Unable to prepare ${id.displayName}")
        }
    }

    private fun readVersion(file: File): String {
        val process = ProcessBuilder(file.absolutePath, "--version")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText().trim() }
        check(process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            "Version check timed out for ${file.name}"
        }
        check(process.exitValue() == 0) {
            output.ifBlank { "${file.name} failed its version check" }
        }
        return output.lineSequence().firstOrNull().orEmpty()
    }

    private fun statusFor(id: DependencyId, message: String?) = DependencyStatus(
        id = id,
        state = if (path(id).exists()) DependencyState.ERROR else DependencyState.MISSING,
        path = path(id).absolutePath,
        message = message
    )

    private fun path(id: DependencyId) = when (id) {
        DependencyId.YT_DLP -> ytDlpPath
        DependencyId.FFMPEG -> ffmpegPath
    }

    private fun supportedAbi(): String? = Build.SUPPORTED_ABIS.firstOrNull {
        it == "arm64-v8a" || it == "x86_64"
    }

    companion object {
        private const val TAG = "ToolManager"
    }
}

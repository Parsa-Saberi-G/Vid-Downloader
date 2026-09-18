package com.streamforge.downloader.downloader

import android.content.Context
import android.util.Log
import com.streamforge.downloader.manager.ToolManager
import com.streamforge.downloader.model.DownloadOptions
import com.streamforge.downloader.model.MediaInfo
import com.streamforge.downloader.model.MediaFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ProcessYtDlpEngine(
    private val context: Context,
    private val toolManager: ToolManager
) : YtDlpEngine {
    private val processes = ConcurrentHashMap<String, Process>()

    override suspend fun isAvailable() = toolManager.initialize().isSuccess
    override suspend fun getVersion(): String? {
        if (toolManager.initialize().isFailure) return null
        return runCommand(listOf("--version")).getOrNull()?.trim()
    }

    override suspend fun extractInfo(url: String): Result<MediaInfo> = runCatching {
        require(isAvailable()) { "The bundled download engine is unavailable. Reinstall the app or contact support." }
        val output = runCommand(listOf("--dump-single-json", "--no-warnings", "--skip-download", url)).getOrThrow()
        val json = JSONObject(output.substring(output.indexOf('{').takeIf { it >= 0 } ?: error("yt-dlp returned no metadata")))
        val formats = json.optJSONArray("formats")?.let { array ->
            (0 until array.length()).mapNotNull { index ->
                array.optJSONObject(index)?.let {
                    MediaFormat(
                        id = it.optString("format_id"),
                        label = it.optString("format_note", it.optString("format", "Available")),
                        extension = it.optString("ext", "unknown"),
                        hasVideo = it.optString("vcodec", "none") != "none",
                        hasAudio = it.optString("acodec", "none") != "none"
                    )
                }
            }
        }.orEmpty()
        MediaInfo(json.optString("title", "Untitled"), json.optString("uploader").ifBlank { null },
            json.optString("duration_string").ifBlank { null }, formats)
    }

    override fun download(id: String, url: String, options: DownloadOptions): Flow<YtDlpEvent> = flow {
        toolManager.initialize().getOrElse {
            emit(YtDlpEvent.Failed(it.message ?: "Built-in tools are unavailable"))
            return@flow
        }
        val executable = toolManager.ytDlpPath
        if (!executable.canExecute()) {
            emit(YtDlpEvent.Failed("The bundled download engine is unavailable. Reinstall the app or contact support."))
            return@flow
        }
        val defaultDirectory = context.getExternalFilesDir("downloads") ?: File(context.filesDir, "downloads")
        val outputDirectory = File(options.outputDirectory ?: defaultDirectory.absolutePath)
            .apply { mkdirs() }
        val outputTemplate = File(outputDirectory, "%(title)s.%(ext)s").absolutePath
        val args = mutableListOf(executable.absolutePath, "--newline", "--progress", "-o", outputTemplate)
        if (options.audioOnly) args += listOf("-x", "--audio-format", "mp3")
        if (options.postProcessingEnabled) {
            args += listOf("--ffmpeg-location", toolManager.ffmpegPath.parentFile!!.absolutePath)
        }
        options.formatId?.let { args += listOf("-f", it) }
        options.cookiesFile?.takeIf { it.isNotBlank() }?.let { args += listOf("--cookies", it) }
        options.proxy?.takeIf { it.isNotBlank() }?.let { args += listOf("--proxy", it) }
        options.userAgent?.takeIf { it.isNotBlank() }?.let { args += listOf("--user-agent", it) }
        args += options.customArguments
        args += url
        try {
            val process = ProcessBuilder(args).redirectErrorStream(true).start()
            processes[id] = process
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    Log.d("YtDlp", line)
                    YtDlpParser.parseProgress(line)?.let { emit(it) }
                    if (line.contains("ERROR", ignoreCase = true)) emit(YtDlpEvent.Failed(YtDlpParser.cleanError(line)))
                }
            }
            val exitCode = process.waitFor()
            if (exitCode == 0) emit(YtDlpEvent.Completed(outputTemplate))
            else if (processes[id] != null) emit(YtDlpEvent.Failed("yt-dlp exited with code $exitCode"))
        } catch (error: Exception) {
            Log.e("YtDlp", "Download failed", error)
            emit(YtDlpEvent.Failed(error.message ?: "Download failed. Check the URL and dependency status."))
        } finally {
            processes.remove(id)
        }
    }

    override fun cancel(id: String) { processes.remove(id)?.destroy() }

    private suspend fun runCommand(arguments: List<String>): Result<String> = runCatching {
        val process = ProcessBuilder(listOf(toolManager.ytDlpPath.absolutePath) + arguments)
            .redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        check(process.waitFor() == 0) { output.ifBlank { "yt-dlp command failed" } }
        output
    }
}

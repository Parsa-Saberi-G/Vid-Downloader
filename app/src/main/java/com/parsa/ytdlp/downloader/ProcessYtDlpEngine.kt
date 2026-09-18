package com.parsa.ytdlp.downloader

import android.content.Context
import com.parsa.ytdlp.model.DownloadOptions
import com.parsa.ytdlp.model.MediaInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/** Backend seam for a bundled/user-provided yt-dlp executable. No binary is downloaded implicitly. */
class ProcessYtDlpEngine(private val context: Context) : YtDlpEngine {
    private val processes = ConcurrentHashMap<String, Process>()
    private val executable = File(context.filesDir, "yt-dlp")

    override suspend fun isAvailable() = executable.canExecute()
    override suspend fun getVersion(): String? = null // Implement after provisioning a trusted executable.
    override suspend fun extractInfo(url: String): Result<MediaInfo> =
        Result.failure(IllegalStateException("yt-dlp is not installed. Add a trusted executable to app storage."))

    override fun download(id: String, url: String, options: DownloadOptions): Flow<YtDlpEvent> = flow {
        emit(YtDlpEvent.Failed("yt-dlp is not installed. Configure a trusted executable first."))
    }

    override fun cancel(id: String) { processes.remove(id)?.destroy() }
}

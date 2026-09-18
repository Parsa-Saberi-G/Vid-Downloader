package com.streamforge.downloader.manager

import android.content.Context
import android.os.Build
import android.util.Log
import com.streamforge.downloader.model.DependencyId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

class BinaryInstaller(private val context: Context) {
    // codeCacheDir is executable app-private storage on Android; filesDir may be mounted noexec.
    private val directory = File(context.codeCacheDir, "forge-stream-binaries").apply { mkdirs() }

    fun target(id: DependencyId): File = File(directory, id.name.lowercase())

    suspend fun install(id: DependencyId): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val target = target(id)
            val temporary = File(directory, "${target.name}.download")
            val url = downloadUrl(id) ?: error("No compatible ${id.displayName} build is available for ${Build.SUPPORTED_ABIS.firstOrNull()}")
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 120_000
                instanceFollowRedirects = true
                requestMethod = "GET"
            }
            try {
                check(connection.responseCode in 200..299) { "Download failed with HTTP ${connection.responseCode}" }
                connection.inputStream.use { input ->
                    temporary.outputStream().use { output -> input.copyTo(output) }
                }
            } finally {
                connection.disconnect()
            }
            check(temporary.length() > 0) { "Downloaded file was empty" }
            check(isElf(temporary)) {
                "The downloaded ${id.displayName} package is not Android-native. Choose an Android-compatible build."
            }
            if (target.exists()) check(target.delete()) { "Unable to replace existing binary" }
            check(temporary.renameTo(target)) {
                "Unable to move downloaded binary into app storage"
            }

            check(target.setExecutable(true, false)) { "Unable to mark binary executable" }
            target
        }.onFailure { Log.e("BinaryInstaller", "Unable to install ${id.displayName}", it) }
    }

    fun remove(id: DependencyId): Boolean = target(id).delete()

    private fun isElf(file: File): Boolean = RandomAccessFile(file, "r").use {
        it.readUnsignedByte() == 0x7f &&
            it.readUnsignedByte() == 0x45 &&
            it.readUnsignedByte() == 0x4c &&
            it.readUnsignedByte() == 0x46
    }

    private fun downloadUrl(id: DependencyId): String? {
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: return null
        return when (id) {
            DependencyId.YT_DLP -> when (abi) {
                "arm64-v8a" -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux_aarch64"
                "x86_64" -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux"
                else -> null
            }
            // This URL is intentionally centralized so it can be replaced by a vetted Android build.
            DependencyId.FFMPEG -> null
        }
    }
}

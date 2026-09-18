package com.streamforge.downloader.manager

import android.content.Context
import com.streamforge.downloader.model.DependencyId
import com.streamforge.downloader.model.DependencyState
import com.streamforge.downloader.model.DependencyStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DependencyManager(context: Context) {
    private val appContext = context.applicationContext
    private val checker = BinaryChecker()
    private val installer = BinaryInstaller(appContext)

    suspend fun checkAll(customFfmpegPath: String = ""): List<DependencyStatus> = withContext(Dispatchers.IO) {
        listOf(check(DependencyId.YT_DLP), check(DependencyId.FFMPEG, customFfmpegPath))
    }

    suspend fun check(id: DependencyId, customPath: String = ""): DependencyStatus = withContext(Dispatchers.IO) {
        val file = if (id == DependencyId.FFMPEG && customPath.isNotBlank()) File(customPath) else installer.target(id)
        val result = checker.check(file)
        if (result.available) DependencyStatus(id, DependencyState.INSTALLED, file.absolutePath, result.version)
        else DependencyStatus(id, if (file.exists()) DependencyState.ERROR else DependencyState.MISSING, file.absolutePath, message = result.error)
    }

    suspend fun install(id: DependencyId): Result<DependencyStatus> {
        val file = installer.install(id).getOrElse { return Result.failure(it) }
        val checked = check(id)
        return if (checked.state == DependencyState.INSTALLED) Result.success(checked)
        else Result.failure(IllegalStateException(checked.message ?: "Installed binary failed validation"))
    }

    fun remove(id: DependencyId): Boolean = installer.remove(id)
    fun managedPath(id: DependencyId): File = installer.target(id)
}

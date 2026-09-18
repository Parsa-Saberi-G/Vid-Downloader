package com.streamforge.downloader.manager

import com.streamforge.downloader.model.DependencyId
import com.streamforge.downloader.model.DependencyStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DependencyManager(
    private val toolManager: ToolManager
) {
    val toolState = toolManager.state

    suspend fun checkAll(customFfmpegPath: String = ""): List<DependencyStatus> = withContext(Dispatchers.IO) {
        toolManager.statuses()
    }

    suspend fun check(id: DependencyId, customPath: String = ""): DependencyStatus = withContext(Dispatchers.IO) {
        toolManager.statuses().first { it.id == id }
    }

    suspend fun reinstall(): Result<List<DependencyStatus>> = toolManager.initialize(force = true)
}

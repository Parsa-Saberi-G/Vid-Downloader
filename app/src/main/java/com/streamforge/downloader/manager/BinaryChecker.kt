package com.streamforge.downloader.manager

import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

data class BinaryCheckResult(val available: Boolean, val version: String? = null, val error: String? = null)

class BinaryChecker {
    fun check(file: File, versionArgument: String = "--version"): BinaryCheckResult {
        if (!file.isFile) {
            return BinaryCheckResult(false, error = "Binary is missing")
        }
        if (!file.canExecute() && !file.setExecutable(true, false)) {
            return BinaryCheckResult(false, error = "Android could not grant execute permission")
        }
        if (!isNativeExecutable(file)) {
            return BinaryCheckResult(false, error = "This file is not an Android-native executable")
        }
        return try {
            val process = ProcessBuilder(file.absolutePath, versionArgument)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText().trim() }
            val finished = process.waitFor(8, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                BinaryCheckResult(false, error = "Version check timed out")
            } else if (process.exitValue() == 0) {
                BinaryCheckResult(true, version = output.lineSequence().firstOrNull())
            } else {
                BinaryCheckResult(false, error = output.ifBlank { "Version check failed" })
            }
        } catch (error: Exception) {
            Log.e("BinaryChecker", "Unable to check ${file.absolutePath}", error)
            BinaryCheckResult(false, error = error.message ?: "Unable to execute binary")
        }

    }

    private fun isNativeExecutable(file: File): Boolean = runCatching {
        RandomAccessFile(file, "r").use {
            it.readUnsignedByte() == 0x7f &&
                it.readUnsignedByte() == 0x45 &&
                it.readUnsignedByte() == 0x4c &&
                it.readUnsignedByte() == 0x46
        }
    }.getOrDefault(false)
}

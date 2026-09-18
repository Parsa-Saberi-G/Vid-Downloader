package com.streamforge.downloader.util

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ErrorHandler {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    fun report(context: String, error: Throwable, suggestion: String? = null) {
        Log.e("YtDlp:$context", error.message, error)
        val message = buildString {
            append(error.message ?: "Unexpected error")
            suggestion?.let { append("\nTry: ").append(it) }
        }
        _messages.tryEmit(message)
    }
}

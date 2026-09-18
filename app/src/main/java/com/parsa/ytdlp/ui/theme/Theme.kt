package com.parsa.ytdlp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(primary = Color(0xFFB5C8FF), secondary = Color(0xFFBEC6DC), surface = Color(0xFF111318), background = Color(0xFF111318))
@Composable fun YtDlpTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = DarkColors, content = content) }

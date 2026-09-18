package com.streamforge.downloader.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import com.streamforge.downloader.model.AppSettings

private val DarkColors = darkColorScheme(primary = Color(0xFFB5C8FF), secondary = Color(0xFFBEC6DC), surface = Color(0xFF111318), background = Color(0xFF111318))
@Composable
fun YtDlpTheme(settings: AppSettings = AppSettings.defaults(""), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val colors = when (settings.theme.lowercase()) {
        "system" -> if (settings.dynamicColors && Build.VERSION.SDK_INT >= 31) {
            if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else if (systemDark) DarkColors else lightColorScheme()
        "light" -> lightColorScheme()
        "dark" -> DarkColors
        else -> DarkColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

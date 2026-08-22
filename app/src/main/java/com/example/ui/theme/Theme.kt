package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark by default for music player experience
    isOled: Boolean = false,
    accentIndex: Int = 0,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val (primary, primaryContainer) = when (accentIndex) {
        1 -> Pair(AccentOrange, AccentOrangeContainer)
        2 -> Pair(AccentEmerald, AccentEmeraldContainer)
        3 -> Pair(AccentPink, AccentPinkContainer)
        4 -> Pair(AccentBlue, AccentBlueContainer)
        else -> Pair(AccentPurple, AccentPurpleContainer)
    }

    val background = if (isOled) OledBackground else DarkBackground
    val surface = if (isOled) OledSurface else DarkSurface
    val surfaceVariant = if (isOled) OledSurfaceVariant else DarkSurfaceVariant

    val colorScheme = darkColorScheme(
        primary = primary,
        onPrimary = Color.Black,
        primaryContainer = primaryContainer,
        onPrimaryContainer = TextPrimary,
        secondary = primary.copy(alpha = 0.8f),
        onSecondary = Color.Black,
        secondaryContainer = surfaceVariant,
        onSecondaryContainer = TextPrimary,
        tertiary = AccentPinkLight,
        background = background,
        onBackground = TextPrimary,
        surface = surface,
        onSurface = TextPrimary,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = TextSecondary,
        surfaceTint = primary
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

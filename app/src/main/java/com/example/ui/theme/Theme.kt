package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark by default for modern music player experience
    isOled: Boolean = false,
    accentIndex: Int = 0,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val (primary, primaryContainer, onPrimaryContainer) = when (accentIndex) {
        1 -> Triple(AccentOrange, AccentOrangeContainer, OnAccentOrangeContainer)
        2 -> Triple(AccentEmerald, AccentEmeraldContainer, OnAccentEmeraldContainer)
        3 -> Triple(AccentPink, AccentPinkContainer, OnAccentPinkContainer)
        4 -> Triple(AccentBlue, AccentBlueContainer, OnAccentBlueContainer)
        else -> Triple(AccentPurple, AccentPurpleContainer, OnAccentPurpleContainer)
    }

    val background = if (isOled) OledBackground else DarkBackground
    val surface = if (isOled) OledSurface else DarkSurface
    val surfaceVariant = if (isOled) OledSurfaceVariant else DarkSurfaceVariant
    val surfaceContainer = if (isOled) OledSurfaceContainer else DarkSurfaceContainer
    val surfaceContainerLowest = if (isOled) OledBackground else DarkSurfaceContainerLowest
    val surfaceContainerLow = if (isOled) OledSurface else DarkSurfaceContainerLow
    val surfaceContainerHigh = if (isOled) OledSurfaceVariant else DarkSurfaceContainerHigh
    val surfaceContainerHighest = if (isOled) DarkSurfaceVariant else DarkSurfaceContainerHighest

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val baseDynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isOled && darkTheme) {
                baseDynamic.copy(
                    background = OledBackground,
                    surface = OledSurface,
                    surfaceVariant = OledSurfaceVariant,
                    surfaceContainerLowest = OledBackground,
                    surfaceContainerLow = OledSurface,
                    surfaceContainer = OledSurfaceContainer,
                    surfaceContainerHigh = OledSurfaceVariant,
                    surfaceContainerHighest = DarkSurfaceVariant
                )
            } else {
                baseDynamic
            }
        }
        darkTheme -> {
            darkColorScheme(
                primary = primary,
                onPrimary = Color(0xFF1D005D),
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                secondary = primary.copy(alpha = 0.85f),
                onSecondary = Color(0xFF1D005D),
                secondaryContainer = surfaceContainerHigh,
                onSecondaryContainer = TextPrimary,
                tertiary = AccentPinkLight,
                onTertiary = Color(0xFF490022),
                background = background,
                onBackground = TextPrimary,
                surface = surface,
                onSurface = TextPrimary,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = TextSecondary,
                surfaceContainerLowest = surfaceContainerLowest,
                surfaceContainer = surfaceContainer,
                surfaceContainerLow = surfaceContainerLow,
                surfaceContainerHigh = surfaceContainerHigh,
                surfaceContainerHighest = surfaceContainerHighest,
                outline = TextOutline,
                outlineVariant = TextOutline.copy(alpha = 0.4f),
                error = ErrorRed,
                errorContainer = ErrorContainer,
                surfaceTint = primary
            )
        }
        else -> {
            lightColorScheme(
                primary = primary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                background = Color(0xFFFBF8FD),
                surface = Color(0xFFFBF8FD),
                surfaceContainerLowest = Color(0xFFFFFFFF),
                surfaceContainerLow = Color(0xFFF8F2FA),
                surfaceContainer = Color(0xFFF3EDF7),
                surfaceContainerHigh = Color(0xFFECE6F0),
                surfaceContainerHighest = Color(0xFFE6E0E9)
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


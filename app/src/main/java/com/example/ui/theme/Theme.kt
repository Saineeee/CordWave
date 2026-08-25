package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

@Composable
fun PixelPlayerStatusBarStyle(
    color: Color = MaterialTheme.colorScheme.background
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val isLight = ColorUtils.calculateLuminance(color.toArgb()) > 0.55
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.isNavigationBarContrastEnforced = false
            }
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = isLight
            insetsController.isAppearanceLightNavigationBars = isLight
        }
    }
}

private val PixelPlayerDarkColorScheme = darkColorScheme(
    primary = PixelPlayerPurplePrimary,
    onPrimary = PixelPlayerWhite,
    primaryContainer = PixelPlayerPurplePrimary.copy(alpha = 0.3f),
    onPrimaryContainer = PixelPlayerLightPurple,
    secondary = PixelPlayerPink,
    onSecondary = PixelPlayerWhite,
    secondaryContainer = PixelPlayerPink.copy(alpha = 0.15f),
    onSecondaryContainer = PixelPlayerPink.copy(alpha = 0.85f),
    tertiary = PixelPlayerOrange,
    onTertiary = PixelPlayerBlack,
    background = PixelPlayerPurpleDark,
    onBackground = TextPrimary,
    surface = PixelPlayerSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF282631),
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = Color(0xFF0E0E11),
    surfaceContainerLow = Color(0xFF1B1B1F),
    surfaceContainer = Color(0xFF1F1F24),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),
    outline = Color(0xFF49454E),
    error = Color(0xFFFF5252),
    onError = PixelPlayerWhite,
    surfaceTint = PixelPlayerPurplePrimary
)

private val PixelPlayerLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

fun getAccentColorScheme(index: Int, isDark: Boolean, isOled: Boolean): ColorScheme {
    val primaryColor = when (index) {
        1 -> PixelPlayerOrange
        2 -> Color(0xFF4CAF50)
        3 -> PixelPlayerPink
        4 -> Color(0xFF42A5F5)
        else -> PixelPlayerPurplePrimary
    }

    return if (isDark) {
        val bg = if (isOled) Color(0xFF000000) else PixelPlayerPurpleDark
        val surf = if (isOled) Color(0xFF0C0C0E) else PixelPlayerSurface
        val surfLow = if (isOled) Color(0xFF121216) else Color(0xFF1B1B1F)
        val surfCont = if (isOled) Color(0xFF18181D) else Color(0xFF1F1F24)
        val surfHigh = if (isOled) Color(0xFF222228) else Color(0xFF2B2930)

        PixelPlayerDarkColorScheme.copy(
            primary = primaryColor,
            primaryContainer = primaryColor.copy(alpha = 0.25f),
            onPrimaryContainer = primaryColor,
            surfaceTint = primaryColor,
            background = bg,
            surface = surf,
            surfaceContainerLowest = if (isOled) Color.Black else Color(0xFF0E0E11),
            surfaceContainerLow = surfLow,
            surfaceContainer = surfCont,
            surfaceContainerHigh = surfHigh
        )
    } else {
        PixelPlayerLightColorScheme.copy(
            primary = primaryColor,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = primaryColor
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    isOled: Boolean = false,
    accentIndex: Int = 0,
    colorSchemePairOverride: Pair<ColorScheme, ColorScheme>? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val baseScheme = when {
        colorSchemePairOverride != null -> {
            if (darkTheme) colorSchemePairOverride.first else colorSchemePairOverride.second
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getAccentColorScheme(accentIndex, darkTheme, isOled)
    }

    val finalScheme = if (darkTheme && isOled) {
        baseScheme.copy(
            background = Color(0xFF000000),
            surface = Color(0xFF0A0A0C),
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color(0xFF121215),
            surfaceContainer = Color(0xFF17171C),
            surfaceContainerHigh = Color(0xFF202026),
            surfaceContainerHighest = Color(0xFF2B2B32)
        )
    } else {
        baseScheme
    }

    PixelPlayerStatusBarStyle(finalScheme.background)

    MaterialTheme(
        colorScheme = finalScheme,
        typography = Typography,
        content = content
    )
}

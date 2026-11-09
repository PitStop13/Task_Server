package com.taskserver.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// State provider mapped by the whole app
val LocalThemeIsDark = compositionLocalOf { true }

private val ModernShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TaskServerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
        val colorScheme = if (darkTheme) {
            darkColorScheme(
                primary              = CyanPrimary,
                onPrimary            = OnCyan,
                primaryContainer     = Color(0xFF0F2B48),
                onPrimaryContainer   = CyanBright,
                secondary            = PurpleSecondary,
                onSecondary          = OnPurple,
                secondaryContainer   = Color(0xFF331D4A),
                onSecondaryContainer = PurpleBright,
                tertiary             = TerminalGreen,
                onTertiary           = Color.Black,
                background           = Background,
                onBackground         = OnBackground,
                surface              = Surface,
                onSurface            = OnSurface,
                surfaceVariant       = SurfaceVariant,
                onSurfaceVariant     = OnSurfaceDim,
                surfaceContainerHigh = SurfaceHigh,
                surfaceContainer     = SurfaceContainer,
                outline              = BorderDefault,
                outlineVariant       = BorderSubtle,
                error                = ColorError,
                onError              = Color.White,
                errorContainer       = Color(0xFF3B1219),
                onErrorContainer     = ColorError,
                scrim                = Void.copy(alpha = 0.90f),
                inverseSurface       = Color(0xFFF1F5F9),
                inverseOnSurface     = Color(0xFF090B12),
                inversePrimary       = CyanDim
            )
        } else {
            lightColorScheme(
                primary              = CyanPrimary,
                onPrimary            = OnCyan,
                primaryContainer     = Color(0xFFE0F2FE),
                onPrimaryContainer   = Color(0xFF0369A1),
                secondary            = PurpleSecondary,
                onSecondary          = OnPurple,
                secondaryContainer   = Color(0xFFF3E8FF),
                onSecondaryContainer = Color(0xFF6B21A8),
                tertiary             = TerminalGreen,
                onTertiary           = Color.White,
                background           = Background,
                onBackground         = OnBackground,
                surface              = Surface,
                onSurface            = OnSurface,
                surfaceVariant       = SurfaceVariant,
                onSurfaceVariant     = OnSurfaceDim,
                surfaceContainerHigh = SurfaceHigh,
                surfaceContainer     = SurfaceContainer,
                outline              = BorderDefault,
                outlineVariant       = BorderSubtle,
                error                = ColorError,
                onError              = Color.White,
                errorContainer       = Color(0xFFFFE4E6),
                onErrorContainer     = ColorError,
                scrim                = Color.Black.copy(alpha = 0.40f),
                inverseSurface       = Color(0xFF1E293B),
                inverseOnSurface     = Color.White,
                inversePrimary       = CyanBright
            )
        }

        val view = LocalView.current
        
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography  = TaskServerTypography,
            shapes      = ModernShapes,
            content     = content
        )
    }
}

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

private val CyberShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
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
                primaryContainer     = CyanDim,
                onPrimaryContainer   = CyanBright,
                secondary            = PurpleSecondary,
                onSecondary          = OnPurple,
                secondaryContainer   = PurpleDim,
                onSecondaryContainer = PurpleBright,
                tertiary             = TerminalGreen,
                onTertiary           = Void,
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
                onError              = TextPrimary,
                errorContainer       = Color(0xFF3B1219),
                onErrorContainer     = ColorError,
                scrim                = Void.copy(alpha = 0.90f),
                inverseSurface       = TextPrimary,
                inverseOnSurface     = Void,
                inversePrimary       = CyanDim
            )
        } else {
            lightColorScheme(
                primary              = CyanPrimary,
                onPrimary            = OnCyan,
                primaryContainer     = CyanDim,
                onPrimaryContainer   = CyanBright,
                secondary            = PurpleSecondary,
                onSecondary          = OnPurple,
                secondaryContainer   = PurpleDim,
                onSecondaryContainer = PurpleBright,
                tertiary             = TerminalGreen,
                onTertiary           = Void,
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
                onError              = TextPrimary,
                errorContainer       = Color(0xFFFFDCDA),
                onErrorContainer     = ColorError,
                scrim                = Void.copy(alpha = 0.50f),
                inverseSurface       = TextPrimary,
                inverseOnSurface     = Void,
                inversePrimary       = CyanDim
            )
        }

        val view = LocalView.current
        
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography  = TaskServerTypography,
            shapes      = CyberShapes,
            content     = content
        )
    }
}

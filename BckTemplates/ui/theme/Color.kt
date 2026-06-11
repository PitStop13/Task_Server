package com.taskserver.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════
//  PALETTE CYBER-MINIMAL 2026 — DUAL THEME SUPPORT
// ═══════════════════════════════════════════════════════

// ── Base surfaces ──
private val DarkVoid             = Color(0xFF030408)
private val LightVoid            = Color(0xFFFFFFFF)

private val DarkBackground       = Color(0xFF060812)
private val LightBackground      = Color(0xFFF4F6F8)

private val DarkSurface          = Color(0xFF0C0F1A)
private val LightSurface         = Color(0xFFFFFFFF)

private val DarkSurfaceVariant   = Color(0xFF111525)
private val LightSurfaceVariant  = Color(0xFFE2E8F0)

private val DarkSurfaceContainer = Color(0xFF161B2E)
private val LightSurfaceContainer= Color(0xFFCBD5E1)

private val DarkSurfaceHigh      = Color(0xFF1A2038)
private val LightSurfaceHigh     = Color(0xFFF8FAFC)

private val DarkSurfaceBright    = Color(0xFF1F2645)
private val LightSurfaceBright   = Color(0xFFF1F5F9)

val Void: Color @Composable get() = if (LocalThemeIsDark.current) DarkVoid else LightVoid
val Background: Color @Composable get() = if (LocalThemeIsDark.current) DarkBackground else LightBackground
val Surface: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurface else LightSurface
val SurfaceVariant: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurfaceVariant else LightSurfaceVariant
val SurfaceContainer: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurfaceContainer else LightSurfaceContainer
val SurfaceHigh: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurfaceHigh else LightSurfaceHigh
val SurfaceBright: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurfaceBright else LightSurfaceBright

// ── Primary — Electric Cyan ──
private val DarkCyanPrimary      = Color(0xFF00E5FF)
private val LightCyanPrimary     = Color(0xFF007A8A)

private val DarkCyanBright       = Color(0xFF67FFFF)
private val LightCyanBright      = Color(0xFF00A2B5)

private val DarkCyanDim          = Color(0xFF007C8A)
private val LightCyanDim         = Color(0xFF005863)

private val DarkCyanMuted        = Color(0xFF004D5A)
private val LightCyanMuted       = Color(0xFF003840)

private val DarkOnCyan           = Color(0xFF001F28)
private val LightOnCyan          = Color(0xFFFFFFFF)

val CyanPrimary: Color @Composable get() = if (LocalThemeIsDark.current) DarkCyanPrimary else LightCyanPrimary
val CyanBright: Color @Composable get() = if (LocalThemeIsDark.current) DarkCyanBright else LightCyanBright
val CyanDim: Color @Composable get() = if (LocalThemeIsDark.current) DarkCyanDim else LightCyanDim
val CyanMuted: Color @Composable get() = if (LocalThemeIsDark.current) DarkCyanMuted else LightCyanMuted
val OnCyan: Color @Composable get() = if (LocalThemeIsDark.current) DarkOnCyan else LightOnCyan

val CyanGlow: Color @Composable get() = if (LocalThemeIsDark.current) Color(0x4000E5FF) else Color(0x40007A8A)

// ── Secondary — Violet/Purple ──
private val DarkPurpleSecondary  = Color(0xFF7B61FF)
private val LightPurpleSecondary = Color(0xFF5E42DA)

private val DarkPurpleBright     = Color(0xFFA78BFF)
private val LightPurpleBright    = Color(0xFF7C64E8)

private val DarkPurpleDim        = Color(0xFF4A3A99)
private val LightPurpleDim       = Color(0xFF38268F)

private val DarkPurpleMuted      = Color(0xFF2D1F66)
private val LightPurpleMuted     = Color(0xFF1E1352)

private val DarkOnPurple         = Color(0xFF0E0828)
private val LightOnPurple        = Color(0xFFFFFFFF)

val PurpleSecondary: Color @Composable get() = if (LocalThemeIsDark.current) DarkPurpleSecondary else LightPurpleSecondary
val PurpleBright: Color @Composable get() = if (LocalThemeIsDark.current) DarkPurpleBright else LightPurpleBright
val PurpleDim: Color @Composable get() = if (LocalThemeIsDark.current) DarkPurpleDim else LightPurpleDim
val PurpleMuted: Color @Composable get() = if (LocalThemeIsDark.current) DarkPurpleMuted else LightPurpleMuted
val OnPurple: Color @Composable get() = if (LocalThemeIsDark.current) DarkOnPurple else LightOnPurple

val PurpleGlow: Color @Composable get() = if (LocalThemeIsDark.current) Color(0x407B61FF) else Color(0x405E42DA)

// ── Tertiary — Terminal Green ──
private val DarkTerminalGreen    = Color(0xFF00FF41)
private val LightTerminalGreen   = Color(0xFF00A329)

private val DarkTerminalGreenDim = Color(0xFF00C030)
private val LightTerminalGreenDim= Color(0xFF007A1E)

private val DarkTerminalAmber    = Color(0xFFFFAA00)
private val LightTerminalAmber   = Color(0xFFD48100)

private val DarkTerminalRed      = Color(0xFFFF453A)
private val LightTerminalRed     = Color(0xFFDC2626)

val TerminalGreen: Color @Composable get() = if (LocalThemeIsDark.current) DarkTerminalGreen else LightTerminalGreen
val TerminalGreenDim: Color @Composable get() = if (LocalThemeIsDark.current) DarkTerminalGreenDim else LightTerminalGreenDim
val TerminalAmber: Color @Composable get() = if (LocalThemeIsDark.current) DarkTerminalAmber else LightTerminalAmber
val TerminalRed: Color @Composable get() = if (LocalThemeIsDark.current) DarkTerminalRed else LightTerminalRed

// ── Text hierarchy ──
private val DarkTextPrimary      = Color(0xFFF0F6FC)
private val LightTextPrimary     = Color(0xFF0F172A)

private val DarkTextSecondary    = Color(0xFFC9D1D9)
private val LightTextSecondary   = Color(0xFF334155)

private val DarkTextTertiary     = Color(0xFF8B949E)
private val LightTextTertiary    = Color(0xFF64748B)

private val DarkTextMuted        = Color(0xFF484F58)
private val LightTextMuted       = Color(0xFF94A3B8)

private val DarkTextDisabled     = Color(0xFF30363D)
private val LightTextDisabled    = Color(0xFFCBD5E1)

val TextPrimary: Color @Composable get() = if (LocalThemeIsDark.current) DarkTextPrimary else LightTextPrimary
val TextSecondary: Color @Composable get() = if (LocalThemeIsDark.current) DarkTextSecondary else LightTextSecondary
val TextTertiary: Color @Composable get() = if (LocalThemeIsDark.current) DarkTextTertiary else LightTextTertiary
val TextMuted: Color @Composable get() = if (LocalThemeIsDark.current) DarkTextMuted else LightTextMuted
val TextDisabled: Color @Composable get() = if (LocalThemeIsDark.current) DarkTextDisabled else LightTextDisabled

// ── Aliases for Material3 compatibility ──
val OnBackground: Color @Composable get() = TextPrimary
val OnSurface: Color @Composable get() = TextSecondary
val OnSurfaceDim: Color @Composable get() = TextTertiary
val OnSurfaceMuted: Color @Composable get() = TextMuted

// ── Semantic ──
private val DarkColorSuccess     = Color(0xFF3FB950)
private val LightColorSuccess    = Color(0xFF16A34A)

private val DarkColorWarning     = Color(0xFFD29922)
private val LightColorWarning    = Color(0xFFD97706)

private val DarkColorError       = Color(0xFFFF453A)
private val LightColorError      = Color(0xFFDC2626)

private val DarkColorInfo        = Color(0xFF58A6FF)
private val LightColorInfo       = Color(0xFF2563EB)

val ColorSuccess: Color @Composable get() = if (LocalThemeIsDark.current) DarkColorSuccess else LightColorSuccess
val ColorWarning: Color @Composable get() = if (LocalThemeIsDark.current) DarkColorWarning else LightColorWarning
val ColorError: Color @Composable get() = if (LocalThemeIsDark.current) DarkColorError else LightColorError
val ColorInfo: Color @Composable get() = if (LocalThemeIsDark.current) DarkColorInfo else LightColorInfo

val ColorOnline: Color @Composable get() = ColorSuccess
val ColorOffline: Color @Composable get() = ColorError
val ColorChecking: Color @Composable get() = ColorWarning
val ColorUnknown: Color @Composable get() = TextMuted

// ── Borders ──
private val DarkBorderSubtle     = Color(0xFF1B2030)
private val LightBorderSubtle    = Color(0xFFE2E8F0)

private val DarkBorderDefault    = Color(0xFF252D3D)
private val LightBorderDefault   = Color(0xFFCBD5E1)

private val DarkBorderBright     = Color(0xFF30394D)
private val LightBorderBright    = Color(0xFF94A3B8)

val BorderSubtle: Color @Composable get() = if (LocalThemeIsDark.current) DarkBorderSubtle else LightBorderSubtle
val BorderDefault: Color @Composable get() = if (LocalThemeIsDark.current) DarkBorderDefault else LightBorderDefault
val BorderBright: Color @Composable get() = if (LocalThemeIsDark.current) DarkBorderBright else LightBorderBright

// ═══════════════════════════════════════════════════════
//  GRADIENT PRESETS
// ═══════════════════════════════════════════════════════

val GradientCyan: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(CyanPrimary, if (LocalThemeIsDark.current) Color(0xFF0088FF) else Color(0xFF005AC2))
    )

val GradientPurple: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(PurpleSecondary, if (LocalThemeIsDark.current) Color(0xFFBB86FC) else Color(0xFF7E5BEF))
    )

val GradientCyanPurple: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(CyanPrimary, PurpleSecondary)
    )

val GradientCardSurface: Brush
    @Composable get() = Brush.verticalGradient(
        colors = listOf(
            SurfaceVariant,
            Surface
        )
    )

val GradientGlassOverlay: Brush
    @Composable get() = Brush.verticalGradient(
        colors = listOf(
            (if (LocalThemeIsDark.current) Color.White else Color.Black).copy(alpha = 0.05f),
            (if (LocalThemeIsDark.current) Color.White else Color.Black).copy(alpha = 0.01f),
            Color.Transparent
        )
    )

// ── Glow helpers ──
@Composable fun cyanGlow(alpha: Float = 0.3f) = CyanPrimary.copy(alpha = alpha)
@Composable fun purpleGlow(alpha: Float = 0.3f) = PurpleSecondary.copy(alpha = alpha)
@Composable fun successGlow(alpha: Float = 0.25f) = ColorSuccess.copy(alpha = alpha)
@Composable fun errorGlow(alpha: Float = 0.25f) = ColorError.copy(alpha = alpha)

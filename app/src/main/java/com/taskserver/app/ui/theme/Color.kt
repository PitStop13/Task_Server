package com.taskserver.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════
//  PALETTE PREMIUM 2026 — REFINED DARK/LIGHT
// ═══════════════════════════════════════════════════════

// ── Base surfaces ──
private val DarkVoid             = Color(0xFF07080D)
private val LightVoid            = Color(0xFFFFFFFF)

private val DarkBackground       = Color(0xFF0B0D14) // Deep Space Blue-Black
private val LightBackground      = Color(0xFFF8FAFC)

private val DarkSurface          = Color(0xFF161A28) // Professional Navy
private val LightSurface         = Color(0xFFFFFFFF)

private val DarkSurfaceVariant   = Color(0xFF1E2338)
private val LightSurfaceVariant  = Color(0xFFF1F5F9)

private val DarkSurfaceContainer = Color(0xFF252B42)
private val LightSurfaceContainer= Color(0xFFE2E8F0)

private val DarkSurfaceHigh      = Color(0xFF2A314D)
private val LightSurfaceHigh     = Color(0xFFF8FAFC)

private val DarkSurfaceBright    = Color(0xFF323A5C)
private val LightSurfaceBright   = Color(0xFFF1F5F9)

val Void: Color @Composable get() = if (LocalThemeIsDark.current) DarkVoid else LightVoid
val Background: Color @Composable get() = if (LocalThemeIsDark.current) DarkBackground else LightBackground
val Surface: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurface else LightSurface
val SurfaceVariant: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurfaceVariant else LightSurfaceVariant
val SurfaceContainer: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurfaceContainer else LightSurfaceContainer
val SurfaceHigh: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurfaceHigh else LightSurfaceHigh
val SurfaceBright: Color @Composable get() = if (LocalThemeIsDark.current) DarkSurfaceBright else LightSurfaceBright

// ── Primary — Professional Electric Blue ──
private val DarkCyanPrimary      = Color(0xFF4D80FF) // Replaced neon cyan with elegant blue
private val LightCyanPrimary     = Color(0xFF2563EB)

private val DarkCyanBright       = Color(0xFF709AFF)
private val LightCyanBright      = Color(0xFF3B82F6)

private val DarkCyanDim          = Color(0xFF3B66D1)
private val LightCyanDim         = Color(0xFF1D4ED8)

private val DarkCyanMuted        = Color(0xFF2E458F)
private val LightCyanMuted       = Color(0xFF1E3A8A)

private val DarkOnCyan           = Color(0xFFFFFFFF)
private val LightOnCyan          = Color(0xFFFFFFFF)

val CyanPrimary: Color @Composable get() = if (LocalThemeIsDark.current) DarkCyanPrimary else LightCyanPrimary
val CyanBright: Color @Composable get() = if (LocalThemeIsDark.current) DarkCyanBright else LightCyanBright
val CyanDim: Color @Composable get() = if (LocalThemeIsDark.current) DarkCyanDim else LightCyanDim
val CyanMuted: Color @Composable get() = if (LocalThemeIsDark.current) DarkCyanMuted else LightCyanMuted
val OnCyan: Color @Composable get() = if (LocalThemeIsDark.current) DarkOnCyan else LightOnCyan

val CyanGlow: Color @Composable get() = if (LocalThemeIsDark.current) Color(0x404D80FF) else Color(0x402563EB)

// ── Secondary — Ethereal Lavender ──
private val DarkPurpleSecondary  = Color(0xFF9333EA)
private val LightPurpleSecondary = Color(0xFF7E22CE)

private val DarkPurpleBright     = Color(0xFFA855F7)
private val LightPurpleBright    = Color(0xFF9333EA)

private val DarkPurpleDim        = Color(0xFF7E22CE)
private val LightPurpleDim       = Color(0xFF6B21A8)

private val DarkPurpleMuted      = Color(0xFF581C87)
private val LightPurpleMuted     = Color(0xFF4C1D95)

private val DarkOnPurple         = Color(0xFFFFFFFF)
private val LightOnPurple        = Color(0xFFFFFFFF)

val PurpleSecondary: Color @Composable get() = if (LocalThemeIsDark.current) DarkPurpleSecondary else LightPurpleSecondary
val PurpleBright: Color @Composable get() = if (LocalThemeIsDark.current) DarkPurpleBright else LightPurpleBright
val PurpleDim: Color @Composable get() = if (LocalThemeIsDark.current) DarkPurpleDim else LightPurpleDim
val PurpleMuted: Color @Composable get() = if (LocalThemeIsDark.current) DarkPurpleMuted else LightPurpleMuted
val OnPurple: Color @Composable get() = if (LocalThemeIsDark.current) DarkOnPurple else LightOnPurple

val PurpleGlow: Color @Composable get() = if (LocalThemeIsDark.current) Color(0x409333EA) else Color(0x407E22CE)

// ── Tertiary — Modern Teal ──
private val DarkTerminalGreen    = Color(0xFF2DD4BF) // Elegant Teal Green
private val LightTerminalGreen   = Color(0xFF0D9488)

private val DarkTerminalGreenDim = Color(0xFF14B8A6)
private val LightTerminalGreenDim= Color(0xFF0F766E)

private val DarkTerminalAmber    = Color(0xFFFACC15) // Refined Gold
private val LightTerminalAmber   = Color(0xFFD97706)

private val DarkTerminalRed      = Color(0xFFFB7185) // Soft Coral Red
private val LightTerminalRed     = Color(0xFFE11D48)

val TerminalGreen: Color @Composable get() = if (LocalThemeIsDark.current) DarkTerminalGreen else LightTerminalGreen
val TerminalGreenDim: Color @Composable get() = if (LocalThemeIsDark.current) DarkTerminalGreenDim else LightTerminalGreenDim
val TerminalAmber: Color @Composable get() = if (LocalThemeIsDark.current) DarkTerminalAmber else LightTerminalAmber
val TerminalRed: Color @Composable get() = if (LocalThemeIsDark.current) DarkTerminalRed else LightTerminalRed

// ── Text hierarchy — Slighly off-white for premium feel ──
private val DarkTextPrimary      = Color(0xFFE2E8F0)
private val LightTextPrimary     = Color(0xFF0F172A)

private val DarkTextSecondary    = Color(0xFF94A3B8)
private val LightTextSecondary   = Color(0xFF475569)

private val DarkTextTertiary     = Color(0xFF64748B)
private val LightTextTertiary    = Color(0xFF94A3B8)

private val DarkTextMuted        = Color(0xFF475569)
private val LightTextMuted       = Color(0xFFCBD5E1)

private val DarkTextDisabled     = Color(0xFF334155)
private val LightTextDisabled    = Color(0xFFE2E8F0)

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
private val DarkColorSuccess     = Color(0xFF10B981)
private val LightColorSuccess    = Color(0xFF059669)

private val DarkColorWarning     = Color(0xFFF59E0B)
private val LightColorWarning    = Color(0xFFD97706)

private val DarkColorError       = Color(0xFFEF4444)
private val LightColorError      = Color(0xFFDC2626)

private val DarkColorInfo        = Color(0xFF3B82F6)
private val LightColorInfo       = Color(0xFF2563EB)

val ColorSuccess: Color @Composable get() = if (LocalThemeIsDark.current) DarkColorSuccess else LightColorSuccess
val ColorWarning: Color @Composable get() = if (LocalThemeIsDark.current) DarkColorWarning else LightColorWarning
val ColorError: Color @Composable get() = if (LocalThemeIsDark.current) DarkColorError else LightColorError
val ColorInfo: Color @Composable get() = if (LocalThemeIsDark.current) DarkColorInfo else LightColorInfo

val ColorOnline: Color @Composable get() = ColorSuccess
val ColorOffline: Color @Composable get() = ColorError
val ColorChecking: Color @Composable get() = ColorWarning
val ColorUnknown: Color @Composable get() = TextMuted

// ── Borders — Softened for 2026 ──
private val DarkBorderSubtle     = Color(0xFF1E293B)
private val LightBorderSubtle    = Color(0xFFF1F5F9)

private val DarkBorderDefault    = Color(0xFF334155)
private val LightBorderDefault   = Color(0xFFE2E8F0)

private val DarkBorderBright     = Color(0xFF475569)
private val LightBorderBright    = Color(0xFFCBD5E1)

val BorderSubtle: Color @Composable get() = if (LocalThemeIsDark.current) DarkBorderSubtle else LightBorderSubtle
val BorderDefault: Color @Composable get() = if (LocalThemeIsDark.current) DarkBorderDefault else LightBorderDefault
val BorderBright: Color @Composable get() = if (LocalThemeIsDark.current) DarkBorderBright else LightBorderBright

// ═══════════════════════════════════════════════════════
//  GRADIENT PRESETS — SMOOTHED
// ═══════════════════════════════════════════════════════

val GradientCyan: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(CyanPrimary, if (LocalThemeIsDark.current) Color(0xFF3B82F6) else Color(0xFF2563EB))
    )

val GradientPurple: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(PurpleSecondary, if (LocalThemeIsDark.current) Color(0xFFA855F7) else Color(0xFF9333EA))
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
            (if (LocalThemeIsDark.current) Color.White else Color.Black).copy(alpha = 0.04f),
            (if (LocalThemeIsDark.current) Color.White else Color.Black).copy(alpha = 0.01f),
            Color.Transparent
        )
    )

// ── Glow helpers ──
@Composable fun cyanGlow(alpha: Float = 0.3f) = CyanPrimary.copy(alpha = alpha)
@Composable fun purpleGlow(alpha: Float = 0.3f) = PurpleSecondary.copy(alpha = alpha)
@Composable fun successGlow(alpha: Float = 0.25f) = ColorSuccess.copy(alpha = alpha)
@Composable fun errorGlow(alpha: Float = 0.25f) = ColorError.copy(alpha = alpha)

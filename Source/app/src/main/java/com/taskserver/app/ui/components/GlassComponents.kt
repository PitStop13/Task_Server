package com.taskserver.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.taskserver.app.ui.theme.*

/**
 * GlassCard — Modern glassmorphism card with ambient glow and top reflection.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = CyanPrimary,
    glowAlpha: Float = 0.12f,
    borderAlpha: Float = 0.20f,
    cornerRadius: Dp = 22.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val isDark = LocalThemeIsDark.current

    val baseModifier = modifier
        .clip(shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = glowColor),
                    onClick = onClick
                )
            } else Modifier
        )
        .background(
            Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                } else {
                    listOf(
                        Color.White,
                        Color(0xFFF8FAFC)
                    )
                }
            )
        )
        // Glass specular top-highlight
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    (if (isDark) Color.White else glowColor).copy(alpha = if (isDark) 0.05f else 0.04f),
                    Color.Transparent,
                    Color.Transparent
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    glowColor.copy(alpha = borderAlpha),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            ),
            shape = shape
        )

    Box(
        modifier = baseModifier,
        content = content
    )
}

/**
 * Animated glow border for selected/active states.
 */
@Composable
fun GlowBorder(
    modifier: Modifier = Modifier,
    color: Color = CyanPrimary,
    cornerRadius: Dp = 22.dp,
    glowRadius: Dp = 8.dp,
    animate: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val usedAlpha = if (animate) alpha else 0.5f

    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = usedAlpha * 0.18f),
                    cornerRadius = CornerRadius(cornerRadius.toPx() + glowRadius.toPx()),
                    size = size
                )
            }
            .border(
                width = 1.5.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        color.copy(alpha = usedAlpha),
                        color.copy(alpha = usedAlpha * 0.25f),
                        color.copy(alpha = usedAlpha),
                        color.copy(alpha = usedAlpha * 0.25f),
                        color.copy(alpha = usedAlpha)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    )
}

/**
 * Shimmer loading effect modifier.
 */
@Composable
fun Modifier.shimmer(
    color: Color = CyanPrimary.copy(alpha = 0.12f),
    widthOfShadow: Dp = 100.dp
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return this.drawBehind {
        val shimmerWidth = widthOfShadow.toPx()
        val x = translateAnim * (size.width + shimmerWidth) - shimmerWidth
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    color,
                    Color.Transparent
                ),
                startX = x,
                endX = x + shimmerWidth
            )
        )
    }
}

/**
 * Accent line — thin gradient accent for section headers.
 */
@Composable
fun AccentLine(
    modifier: Modifier = Modifier,
    brush: Brush = GradientCyanPurple
) {
    Box(
        modifier = modifier
            .height(2.5.dp)
            .fillMaxWidth(0.25f)
            .background(brush, RoundedCornerShape(2.dp))
    )
}

/**
 * Glow dot — Pulsing high-tech status indicator.
 */
@Composable
fun GlowDot(
    color: Color,
    size: Dp = 8.dp,
    glowSize: Dp = 16.dp,
    animate: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    val currentGlowSize = if (animate) glowSize * scale else glowSize

    Box(
        modifier = modifier.size(glowSize),
        contentAlignment = Alignment.Center
    ) {
        // Outer soft glow
        Box(
            modifier = Modifier
                .size(currentGlowSize)
                .background(
                    color.copy(alpha = if (animate) 0.35f else 0.20f),
                    shape = CircleShape
                )
                .blur(3.dp)
        )
        // Core crisp dot
        Box(
            modifier = Modifier
                .size(size)
                .background(color, shape = CircleShape)
        )
    }
}

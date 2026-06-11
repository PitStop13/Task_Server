package com.taskserver.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.taskserver.app.ui.theme.*

/**
 * GlassCard — Glassmorphism card with subtle border glow.
 * The premium look-and-feel of the entire app comes from this component.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = CyanPrimary,
    glowAlpha: Float = 0.08f,
    borderAlpha: Float = 0.15f,
    cornerRadius: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            )
            // Glass overlay (top light reflection)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.04f),
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
                        MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ),
                shape = shape
            ),
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
    cornerRadius: Dp = 18.dp,
    glowRadius: Dp = 8.dp,
    animate: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val usedAlpha = if (animate) alpha else 0.5f

    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = usedAlpha * 0.15f),
                    cornerRadius = CornerRadius(cornerRadius.toPx() + glowRadius.toPx()),
                    size = size
                )
            }
            .border(
                width = 1.5.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        color.copy(alpha = usedAlpha),
                        color.copy(alpha = usedAlpha * 0.3f),
                        color.copy(alpha = usedAlpha),
                        color.copy(alpha = usedAlpha * 0.3f),
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
    color: Color = CyanPrimary.copy(alpha = 0.1f),
    widthOfShadow: Dp = 80.dp
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
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
            .height(2.dp)
            .fillMaxWidth(0.3f)
            .background(brush, RoundedCornerShape(1.dp))
    )
}

/**
 * Glow dot — Status indicator with soft glow.
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
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Box(modifier = modifier.size(glowSize), contentAlignment = androidx.compose.ui.Alignment.Center) {
        // Glow
        Box(
            modifier = Modifier
                .size(if (animate) glowSize * scale else glowSize)
                .background(
                    color.copy(alpha = 0.2f),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .blur(4.dp)
        )
        // Dot
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    color,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}

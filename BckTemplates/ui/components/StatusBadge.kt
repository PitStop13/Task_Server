package com.taskserver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskserver.app.data.model.ServerStatus
import com.taskserver.app.ui.theme.*

@Composable
fun StatusBadge(
    status: ServerStatus,
    showLabel: Boolean = true,
    dotSize: Dp = 8.dp
) {
    val color = when (status) {
        ServerStatus.ONLINE   -> ColorOnline
        ServerStatus.OFFLINE  -> ColorOffline
        ServerStatus.CHECKING -> ColorChecking
        ServerStatus.UNKNOWN  -> ColorUnknown
    }
    val label = when (status) {
        ServerStatus.ONLINE   -> "ONLINE"
        ServerStatus.OFFLINE  -> "OFFLINE"
        ServerStatus.CHECKING -> "CHECK..."
        ServerStatus.UNKNOWN  -> "—"
    }
    val animate = status == ServerStatus.CHECKING

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showLabel) {
            // Chip-style badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(color.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlowDot(color = color, size = dotSize, animate = animate)
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = MonospaceFontFamily,
                    letterSpacing = 1.2.sp
                )
            }
        } else {
            GlowDot(color = color, size = dotSize, animate = animate)
        }
    }
}

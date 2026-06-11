package com.taskserver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskserver.app.data.model.AuthType
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.ServerStatus
import com.taskserver.app.ui.theme.*

@Composable
fun ServerCard(
    server: Server,
    status: ServerStatus,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTerminal: () -> Unit
) {
    val glowColor = when {
        isSelected -> CyanPrimary
        status == ServerStatus.ONLINE -> ColorOnline
        status == ServerStatus.OFFLINE -> ColorOffline
        else -> CyanPrimary
    }

    val borderAlpha = if (isSelected) 0.5f else 0.15f

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = CyanPrimary), // <-- MODIFICATO QUI
                onClick = onClick
            ),
        glowColor = glowColor,
        borderAlpha = borderAlpha,
        cornerRadius = 20.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon container with glow based on status
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle background glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(glowColor.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                )
                Icon(
                    imageVector = Icons.Filled.Dns,
                    contentDescription = null,
                    tint = glowColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Utente",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = server.username,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonospaceFontFamily,
                        color = OnSurfaceDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${server.host}:${server.port}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonospaceFontFamily,
                        color = OnSurfaceDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (server.authType == AuthType.KEY) Icons.Filled.VpnKey else Icons.Filled.Lock,
                        contentDescription = "Auth type",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Trail
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = status, showLabel = true, dotSize = 8.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledIconButton(
                        onClick = onTerminal,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = CyanPrimary.copy(alpha = 0.15f),
                            contentColor = CyanPrimary
                        )
                    ) {
                        Icon(Icons.Filled.Terminal, contentDescription = "Terminale", modifier = Modifier.size(16.dp))
                    }
                    FilledIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = SurfaceHigh,
                            contentColor = OnSurface
                        )
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Modifica", modifier = Modifier.size(16.dp))
                    }
                    FilledIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = ColorError.copy(alpha = 0.15f),
                            contentColor = ColorError
                        )
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Elimina", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

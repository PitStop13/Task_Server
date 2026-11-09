package com.taskserver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onTerminal: () -> Unit,
    onMoveFolder: (() -> Unit)? = null
) {
    val glowColor = when {
        isSelected -> CyanPrimary
        status == ServerStatus.ONLINE -> ColorOnline
        status == ServerStatus.OFFLINE -> ColorOffline
        status == ServerStatus.CHECKING -> ColorChecking
        else -> CyanPrimary
    }

    val borderAlpha = if (isSelected) 0.5f else 0.20f

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = glowColor,
        borderAlpha = borderAlpha,
        cornerRadius = 22.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // ── Top Row: Icon, Title, Auth Badge, Status Badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Server icon container with status tint
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(glowColor.copy(alpha = 0.12f))
                        .border(1.dp, glowColor.copy(alpha = 0.25f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Dns,
                        contentDescription = null,
                        tint = glowColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Folder badge if present
                        if (!server.folder.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyanPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Folder,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = server.folder,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = CyanPrimary
                                    )
                                }
                            }
                        }

                        // Auth badge chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (server.authType == AuthType.KEY) Icons.Filled.VpnKey else Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = if (server.authType == AuthType.KEY) PurpleSecondary else CyanPrimary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = if (server.authType == AuthType.KEY) "KEY AUTH" else "PASSWORD",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MonospaceFontFamily
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Port chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PORT ${server.port}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MonospaceFontFamily
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                StatusBadge(status = status, showLabel = true, dotSize = 6.dp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Monospace Connection Address Pill ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Terminal,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${server.username}@${server.host}:${server.port}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = MonospaceFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Action Row: [>_ Terminal] (prominent) + [Folder] + [Edit] + [Delete] ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary action: Launch Terminal
                Button(
                    onClick = onTerminal,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary.copy(alpha = 0.16f),
                        contentColor = CyanPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Filled.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "SSH Terminal",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Move Folder Button (if provided)
                if (onMoveFolder != null) {
                    FilledIconButton(
                        onClick = onMoveFolder,
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = CyanPrimary
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.DriveFileMove,
                            contentDescription = "Sposta in cartella",
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Edit Button
                FilledIconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Server",
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Delete Button
                FilledIconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = ColorError.copy(alpha = 0.12f),
                        contentColor = ColorError
                    )
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Server",
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

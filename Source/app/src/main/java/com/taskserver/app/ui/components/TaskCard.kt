package com.taskserver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskserver.app.data.model.Task
import com.taskserver.app.ui.theme.*

fun iconFromName(name: String): ImageVector = when (name) {
    "terminal"   -> Icons.Filled.Terminal
    "docker"     -> Icons.Filled.Layers
    "system"     -> Icons.Filled.Settings
    "monitor"    -> Icons.Filled.Monitor
    "folder"     -> Icons.Filled.Folder
    "network"    -> Icons.Filled.Wifi
    "bolt"       -> Icons.Filled.Bolt
    "restart"    -> Icons.Filled.RestartAlt
    "update"     -> Icons.Filled.SystemUpdate
    else         -> Icons.Filled.Terminal
}

@Composable
fun TaskCard(
    task: Task,
    commandCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveFolder: (() -> Unit)? = null
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = PurpleSecondary,
        borderAlpha = 0.20f,
        cornerRadius = 22.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // ── Top Row: Icon, Title & Badges ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(PurpleSecondary.copy(alpha = 0.14f))
                        .border(1.dp, PurpleSecondary.copy(alpha = 0.28f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconFromName(task.iconName),
                        contentDescription = null,
                        tint = PurpleBright,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.name,
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
                        // Folder Badge if present
                        if (!task.folder.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PurpleSecondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Folder,
                                        contentDescription = null,
                                        tint = PurpleBright,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = task.folder,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PurpleBright
                                    )
                                }
                            }
                        }

                        // Commands Count Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$commandCount CMD${if (commandCount != 1) "S" else ""}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MonospaceFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Target Server Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (task.pinnedServerId != null) CyanPrimary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceContainer
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (task.pinnedServerId != null) "PINNED NODE" else "SELECT ON RUN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.4.sp
                                ),
                                color = if (task.pinnedServerId != null) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Action Row: [▶ Run Task] + [Folder] + [Edit] + [Delete] ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleSecondary.copy(alpha = 0.16f),
                        contentColor = PurpleBright
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Run Automation",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (onMoveFolder != null) {
                    FilledIconButton(
                        onClick = onMoveFolder,
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = PurpleBright
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.DriveFileMove,
                            contentDescription = "Sposta in cartella",
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

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
                        contentDescription = "Edit Task",
                        modifier = Modifier.size(17.dp)
                    )
                }

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
                        contentDescription = "Delete Task",
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

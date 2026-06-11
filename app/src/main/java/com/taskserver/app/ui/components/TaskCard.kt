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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taskserver.app.data.model.Task
import com.taskserver.app.ui.theme.*

fun iconFromName(name: String) = when (name) {
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
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = PurpleSecondary,
        borderAlpha = 0.15f,
        cornerRadius = 20.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = PurpleSecondary),
                    onClick = onClick
                )
                .padding(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconFromName(task.iconName),
                    contentDescription = null,
                    tint = PurpleBright,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Code,
                        contentDescription = null,
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$commandCount comandi",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDim
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Trail
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Primary Action: Run
                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier.size(42.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = PurpleSecondary.copy(alpha = 0.2f),
                        contentColor = PurpleBright
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Esegui", modifier = Modifier.size(22.dp))
                }
                
                // Secondary Actions: Edit, Delete
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Edit, "Modifica", tint = OnSurfaceDim, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Delete, "Elimina", tint = ColorError.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

package com.taskserver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskserver.app.data.model.Folder
import com.taskserver.app.ui.theme.CyanPrimary
import com.taskserver.app.ui.theme.MonospaceFontFamily
import com.taskserver.app.ui.theme.PurpleSecondary

@Composable
fun FolderFilterRow(
    folders: List<Folder>,
    selectedFolder: String?, // null = ALL, "" = UNGROUPED, or folder name
    onSelectFolder: (String?) -> Unit,
    onAddFolderClick: () -> Unit,
    onRenameFolder: (Folder) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    itemCountProvider: (String?) -> Int,
    accentColor: Color = CyanPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" chip
        FolderChipItem(
            label = "Tutti (${itemCountProvider(null)})",
            icon = Icons.Filled.Dashboard,
            isSelected = selectedFolder == null,
            accentColor = accentColor,
            onClick = { onSelectFolder(null) }
        )

        // "No Folder" chip (only if there are items without folder or if folders exist)
        val ungroupedCount = itemCountProvider("")
        if (folders.isNotEmpty() || ungroupedCount > 0) {
            FolderChipItem(
                label = "Senza cartella ($ungroupedCount)",
                icon = Icons.Filled.FolderOpen,
                isSelected = selectedFolder == "",
                accentColor = accentColor,
                onClick = { onSelectFolder("") }
            )
        }

        // Each user folder
        folders.forEach { folder ->
            var showMenu by remember { mutableStateOf(false) }
            val count = itemCountProvider(folder.name)

            Box {
                FolderChipItem(
                    label = "${folder.name} ($count)",
                    icon = Icons.Filled.Folder,
                    isSelected = selectedFolder == folder.name,
                    accentColor = accentColor,
                    onClick = { onSelectFolder(folder.name) },
                    onLongClick = { showMenu = true },
                    trailingIcon = {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "Opzioni cartella",
                                modifier = Modifier.size(14.dp),
                                tint = if (selectedFolder == folder.name) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rinomina") },
                        leadingIcon = { Icon(Icons.Filled.Edit, null) },
                        onClick = {
                            showMenu = false
                            onRenameFolder(folder)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Elimina", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDeleteFolder(folder)
                        }
                    )
                }
            }
        }

        // "+ Add Folder" button chip
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAddFolderClick() },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.CreateNewFolder,
                    contentDescription = "Nuova cartella",
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Nuova cartella",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = accentColor
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun FolderChipItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val bg = if (isSelected) accentColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant
    val borderCol = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
    val contentCol = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentCol,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
            )
            trailingIcon?.invoke()
        }
    }
}

@Composable
fun CreateFolderDialog(
    title: String = "Nuova Cartella",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.CreateNewFolder, contentDescription = null, tint = CyanPrimary)
                Text(title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Inserisci il nome della nuova cartella:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("es. HomeLab, Docker, Produzione...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim())
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Crea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun RenameFolderDialog(
    folder: Folder,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(folder.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = CyanPrimary)
                Text("Rinomina Cartella", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && name.trim() != folder.name) {
                        onConfirm(name.trim())
                    }
                },
                enabled = name.isNotBlank() && name.trim() != folder.name
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun DeleteFolderDialog(
    folder: Folder,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Eliminare la cartella \"${folder.name}\"?", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Gli elementi all'interno non verranno eliminati, ma spostati in 'Senza cartella'.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Elimina", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun MoveToFolderDialog(
    currentFolder: String?,
    availableFolders: List<Folder>,
    onSelectFolder: (String?) -> Unit,
    onCreateNewFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, tint = CyanPrimary)
                Text("Sposta in cartella", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Option: No Folder
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onSelectFolder(null)
                                onDismiss()
                            },
                        color = if (currentFolder == null) CyanPrimary.copy(alpha = 0.12f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.FolderOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Nessuna cartella", fontWeight = if (currentFolder == null) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                // Existing folders
                items(availableFolders) { folder ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onSelectFolder(folder.name)
                                onDismiss()
                            },
                        color = if (currentFolder == folder.name) CyanPrimary.copy(alpha = 0.12f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.Folder, contentDescription = null, tint = CyanPrimary)
                            Text(folder.name, fontWeight = if (currentFolder == folder.name) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                // Option: Create new folder
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onDismiss()
                                onCreateNewFolder()
                            },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.CreateNewFolder, contentDescription = null, tint = CyanPrimary)
                            Text("+ Crea nuova cartella", color = CyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

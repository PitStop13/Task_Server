package com.taskserver.app.ui.screens.ssh

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskserver.app.R
import com.taskserver.app.data.model.AuthType
import com.taskserver.app.data.model.Folder
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.ServerStatus
import com.taskserver.app.ui.components.CreateFolderDialog
import com.taskserver.app.ui.components.DeleteFolderDialog
import com.taskserver.app.ui.components.FolderFilterRow
import com.taskserver.app.ui.components.RenameFolderDialog
import com.taskserver.app.ui.components.StatusBadge
import com.taskserver.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SshScreen(
    navController: NavController,
    viewModel: com.taskserver.app.ui.screens.servers.ServerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val servers = uiState.servers
    val isChecking = uiState.statusMap.values.any { it == ServerStatus.CHECKING }

    val folders = uiState.folders
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<Folder?>(null) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }

    val filteredServers = remember(servers, selectedFolder) {
        when (selectedFolder) {
            null -> servers
            "" -> servers.filter { it.folder.isNullOrBlank() }
            else -> servers.filter { it.folder == selectedFolder }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pingRotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingRotation"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars.add(WindowInsets(top = 8.dp)),
                title = {
                    Column {
                        Text(
                            text = "Console SSH",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Sessioni interattive ANSI / VT100",
                            style = MaterialTheme.typography.labelSmall,
                            color = TerminalGreen
                        )
                    }
                },
                actions = {
                    if (folders.isNotEmpty()) {
                        IconButton(onClick = { showCreateFolderDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.CreateNewFolder,
                                contentDescription = "Nuova cartella",
                                tint = TerminalGreen
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.checkAllConnectivity() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Aggiorna stato",
                            modifier = Modifier.rotate(if (isChecking) rotation else 0f),
                            tint = CyanPrimary
                        )
                    }

                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (folders.isNotEmpty()) {
                FolderFilterRow(
                    folders = folders,
                    selectedFolder = selectedFolder,
                    onSelectFolder = { selectedFolder = it },
                    onAddFolderClick = { showCreateFolderDialog = true },
                    onRenameFolder = { folderToRename = it },
                    onDeleteFolder = { folderToDelete = it },
                    itemCountProvider = { f ->
                        when (f) {
                            null -> servers.size
                            "" -> servers.count { it.folder.isNullOrBlank() }
                            else -> servers.count { it.folder == f }
                        }
                    },
                    accentColor = TerminalGreen
                )
            }

            if (servers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(TerminalGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Terminal,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = TerminalGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Nessun server configurato",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Aggiungi un server per avviare una sessione di terminale SSH interattivo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Button(
                            onClick = { navController.navigate("server_edit?serverId=-1") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = OnCyan
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Aggiungi Server", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (filteredServers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun server in questa cartella",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredServers, key = { it.id }) { server ->
                        val status = uiState.statusMap[server.id] ?: ServerStatus.UNKNOWN
                        SshServerCard(
                            server = server,
                            status = status,
                            onConnect = { navController.navigate("terminal/-1/${server.id}") }
                        )
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name ->
                viewModel.createFolder(name)
                showCreateFolderDialog = false
            }
        )
    }

    folderToRename?.let { folder ->
        RenameFolderDialog(
            folder = folder,
            onDismiss = { folderToRename = null },
            onConfirm = { newName ->
                viewModel.renameFolder(folder, newName)
                if (selectedFolder == folder.name) {
                    selectedFolder = newName
                }
                folderToRename = null
            }
        )
    }

    folderToDelete?.let { folder ->
        DeleteFolderDialog(
            folder = folder,
            onDismiss = { folderToDelete = null },
            onConfirm = {
                viewModel.deleteFolder(folder)
                if (selectedFolder == folder.name) {
                    selectedFolder = null
                }
                folderToDelete = null
            }
        )
    }
}

@Composable
private fun SshServerCard(
    server: Server,
    status: ServerStatus,
    onConnect: () -> Unit
) {
    val statusColor = when (status) {
        ServerStatus.ONLINE   -> ColorOnline
        ServerStatus.OFFLINE  -> ColorOffline
        ServerStatus.CHECKING -> ColorChecking
        ServerStatus.UNKNOWN  -> CyanPrimary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onConnect)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                RoundedCornerShape(18.dp)
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TerminalGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Terminal,
                    contentDescription = null,
                    tint = TerminalGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    StatusBadge(status = status, showLabel = true, dotSize = 6.dp)
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    text = "${server.username}@${server.host}:${server.port}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MonospaceFontFamily,
                        fontSize = 11.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = if (server.authType == AuthType.KEY) "Autenticazione con chiave SSH" else "Autenticazione con password",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            FilledIconButton(
                onClick = onConnect,
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = TerminalGreen,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Connetti",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

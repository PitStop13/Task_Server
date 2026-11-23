package com.taskserver.app.ui.screens.servers

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskserver.app.R
import com.taskserver.app.data.model.Folder
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.ServerStatus
import com.taskserver.app.ui.components.*
import com.taskserver.app.ui.theme.*
import kotlinx.coroutines.launch

enum class ServerFilter { ALL, ONLINE, OFFLINE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    navController: NavController,
    viewModel: ServerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val servers = uiState.servers
    val statusMap = uiState.statusMap
    val folders = uiState.folders
    val isChecking = statusMap.values.any { it == ServerStatus.CHECKING }

    var selectedFilter by remember { mutableStateOf(ServerFilter.ALL) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }

    // Folder dialog states
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<Folder?>(null) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    var serverToMoveFolder by remember { mutableStateOf<Server?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var serverToDelete by remember { mutableStateOf<Server?>(null) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            SnackbarController.showAlert(snackbarHostState, it)
            viewModel.clearMessages()
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

    val onlineCount = statusMap.count { it.value == ServerStatus.ONLINE }
    val offlineCount = statusMap.count { it.value == ServerStatus.OFFLINE }

    val filteredServers = remember(servers, statusMap, selectedFilter, selectedFolder) {
        val byStatus = when (selectedFilter) {
            ServerFilter.ALL -> servers
            ServerFilter.ONLINE -> servers.filter { statusMap[it.id] == ServerStatus.ONLINE }
            ServerFilter.OFFLINE -> servers.filter { statusMap[it.id] == ServerStatus.OFFLINE }
        }
        when (selectedFolder) {
            null -> byStatus
            "" -> byStatus.filter { it.folder.isNullOrBlank() }
            else -> byStatus.filter { it.folder == selectedFolder }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                CustomSnackbarVisuals(data = data, alertType = AlertType.SUCCESS)
            }
        },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars.add(WindowInsets(top = 8.dp)),
                title = {
                    Column {
                        Text(
                            text = "I tuoi Server",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isChecking) "Verifica connettività in corso..." else "$onlineCount Online • $offlineCount Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isChecking) ColorChecking else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (folders.isNotEmpty()) {
                        IconButton(onClick = { showCreateFolderDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.CreateNewFolder,
                                contentDescription = "Nuova cartella",
                                tint = CyanPrimary
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("server_edit?serverId=-1") },
                containerColor = CyanPrimary,
                contentColor = OnCyan,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Filled.Add, stringResource(R.string.add), modifier = Modifier.size(20.dp)) },
                text = { Text("Nuovo Server", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Folders row - visible only when folders exist
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
                    accentColor = CyanPrimary
                )
            }

            // Quick Status Filter Row
            if (servers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipTab(
                        label = "Tutti (${servers.size})",
                        selected = selectedFilter == ServerFilter.ALL,
                        activeColor = CyanPrimary,
                        onClick = { selectedFilter = ServerFilter.ALL }
                    )
                    FilterChipTab(
                        label = "Online ($onlineCount)",
                        selected = selectedFilter == ServerFilter.ONLINE,
                        activeColor = ColorOnline,
                        onClick = { selectedFilter = ServerFilter.ONLINE }
                    )
                    FilterChipTab(
                        label = "Offline ($offlineCount)",
                        selected = selectedFilter == ServerFilter.OFFLINE,
                        activeColor = ColorOffline,
                        onClick = { selectedFilter = ServerFilter.OFFLINE }
                    )
                }
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
                                .background(CyanPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Dns,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = CyanPrimary
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
                                text = "Aggiungi i tuoi server Linux, NAS o Raspberry Pi per monitorarli ed eseguire sessioni SSH.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
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
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Nessun server corrispondente",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Modifica i filtri per visualizzare altri server.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredServers, key = { it.id }) { server ->
                        ServerCard(
                            server = server,
                            status = statusMap[server.id] ?: ServerStatus.UNKNOWN,
                            onClick = { navController.navigate("server_edit?serverId=${server.id}") },
                            onEdit = { navController.navigate("server_edit?serverId=${server.id}") },
                            onDelete = { serverToDelete = server },
                            onTerminal = { navController.navigate("terminal/-1/${server.id}") },
                            onMoveFolder = { serverToMoveFolder = server }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    serverToDelete?.let { server ->
        AlertDialog(
            onDismissRequest = { serverToDelete = null },
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ColorError.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = ColorError,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Elimina \"${server.name}\"?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "La configurazione e le credenziali di accesso per questo server verranno rimosse dal dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteServer(server)
                        serverToDelete = null
                        scope.launch {
                            SnackbarController.showAlert(snackbarHostState, "Server eliminato")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorError,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Elimina", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { serverToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            title = "Nuova Cartella Server",
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { folderName ->
                viewModel.createFolder(folderName)
                showCreateFolderDialog = false
            }
        )
    }

    // Rename Folder Dialog
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

    // Delete Folder Dialog
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

    // Move Server to Folder Dialog
    serverToMoveFolder?.let { server ->
        MoveToFolderDialog(
            currentFolder = server.folder,
            availableFolders = folders,
            onSelectFolder = { newFolder ->
                viewModel.updateServerFolder(server.id, newFolder)
                serverToMoveFolder = null
            },
            onCreateNewFolder = {
                showCreateFolderDialog = true
            },
            onDismiss = {
                serverToMoveFolder = null
            }
        )
    }
}

@Composable
private fun FilterChipTab(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (selected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

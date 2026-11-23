package com.taskserver.app.ui.screens.tasks

import androidx.compose.animation.*
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
import com.taskserver.app.data.model.Task
import com.taskserver.app.ui.components.*
import com.taskserver.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val servers by viewModel.servers.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedFolder by remember { mutableStateOf<String?>(null) }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<Folder?>(null) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    var taskToMoveFolder by remember { mutableStateOf<Task?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var taskToDelete by remember { mutableStateOf<Long?>(null) }
    var taskToRun by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            SnackbarController.showAlert(snackbarHostState, it)
            viewModel.clearMessages()
        }
    }

    val filteredTasks = remember(tasks, selectedFolder) {
        when (selectedFolder) {
            null -> tasks
            "" -> tasks.filter { it.folder.isNullOrBlank() }
            else -> tasks.filter { it.folder == selectedFolder }
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
                            text = "Automazioni",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${tasks.size} ${if (tasks.size == 1) "automazione" else "automazioni"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PurpleBright
                        )
                    }
                },
                actions = {
                    if (folders.isNotEmpty()) {
                        IconButton(
                            onClick = { showCreateFolderDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CreateNewFolder,
                                contentDescription = "Nuova cartella",
                                tint = PurpleBright
                            )
                        }
                    }
                    IconButton(
                        onClick = { navController.navigate("settings") }
                    ) {
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
                onClick = { navController.navigate("task_builder?taskId=-1") },
                containerColor = PurpleSecondary,
                contentColor = OnPurple,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Filled.Add, stringResource(R.string.add), modifier = Modifier.size(20.dp)) },
                text = { Text("Nuova Automazione", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Folders row - only visible if folders exist
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
                            null -> tasks.size
                            "" -> tasks.count { it.folder.isNullOrBlank() }
                            else -> tasks.count { it.folder == f }
                        }
                    },
                    accentColor = PurpleBright
                )
            }

            if (tasks.isEmpty()) {
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
                                .background(PurpleSecondary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = PurpleSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Nessuna automazione",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Crea sequenze di comandi (es. aggiornamento pacchetti, riavvio servizi, backup) ed eseguile con un tocco.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                        Button(
                            onClick = { navController.navigate("task_builder?taskId=-1") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurpleSecondary,
                                contentColor = OnPurple
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Crea Automazione", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (filteredTasks.isEmpty()) {
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
                            text = if (!selectedFolder.isNullOrEmpty()) "Nessuna automazione nella cartella \"$selectedFolder\"" else "Nessuna automazione trovata",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Aggiungi una nuova automazione a questa cartella o seleziona un altro filtro.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { navController.navigate("task_builder?taskId=-1") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurpleSecondary,
                                contentColor = OnPurple
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Crea Automazione", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        val commands = viewModel.getCommandCount(task.commandsJson)
                        TaskCard(
                            task = task,
                            commandCount = commands,
                            onClick = {
                                if (task.pinnedServerId != null) {
                                    navController.navigate("terminal/${task.id}/${task.pinnedServerId}")
                                } else {
                                    taskToRun = task.id
                                }
                            },
                            onEdit = { navController.navigate("task_builder?taskId=${task.id}") },
                            onDelete = { taskToDelete = task.id },
                            onMoveFolder = { taskToMoveFolder = task }
                        )
                    }
                }
            }
        }
    }

    // Modal server selector for executing an unpinned task
    if (taskToRun != null) {
        val selectedTaskId = taskToRun!!
        ModalBottomSheet(
            onDismissRequest = { taskToRun = null },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Seleziona Server",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Scegli il server su cui eseguire l'automazione",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (servers.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Nessun server disponibile. Configura prima un server.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        items(servers, key = { it.id }) { srv ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        taskToRun = null
                                        navController.navigate("terminal/$selectedTaskId/${srv.id}")
                                    }
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CyanPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Dns,
                                            contentDescription = null,
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = srv.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${srv.username}@${srv.host}:${srv.port}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonospaceFontFamily),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    FilledIconButton(
                                        onClick = {
                                            taskToRun = null
                                            navController.navigate("terminal/$selectedTaskId/${srv.id}")
                                        },
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = PurpleSecondary,
                                            contentColor = OnPurple
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = "Esegui",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    taskToDelete?.let { taskId ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
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
                    text = "Elimina automazione",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Sei sicuro di voler eliminare questa automazione? L'operazione non può essere annullata.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(taskId)
                        taskToDelete = null
                        scope.launch {
                            SnackbarController.showAlert(snackbarHostState, "Automazione eliminata")
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
                    onClick = { taskToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Move to folder dialog
    taskToMoveFolder?.let { task ->
        MoveToFolderDialog(
            currentFolder = task.folder,
            availableFolders = folders,
            onSelectFolder = { folderName ->
                viewModel.updateTaskFolder(task.id, folderName)
                taskToMoveFolder = null
                scope.launch {
                    SnackbarController.showAlert(
                        snackbarHostState,
                        if (folderName != null) "Spostato in \"$folderName\"" else "Rimosso dalla cartella"
                    )
                }
            },
            onCreateNewFolder = {
                showCreateFolderDialog = true
            },
            onDismiss = { taskToMoveFolder = null }
        )
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
                folderToDelete = null
                if (selectedFolder == folder.name) {
                    selectedFolder = null
                }
            }
        )
    }
}

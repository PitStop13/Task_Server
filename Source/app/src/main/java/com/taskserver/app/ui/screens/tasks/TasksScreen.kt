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
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Automations",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PurpleSecondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${tasks.size} SCRIPTS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = MonospaceFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = PurpleSecondary
                                )
                            }
                        }
                        Text(
                            text = "Single-tap remote commands & workflows",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Button to add folder
                    IconButton(
                        onClick = { showCreateFolderDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CreateNewFolder,
                            contentDescription = "Aggiungi cartella",
                            modifier = Modifier.size(20.dp),
                            tint = PurpleBright
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    IconButton(
                        onClick = { navController.navigate("settings") },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
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
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                icon = { Icon(Icons.Filled.Add, stringResource(R.string.add), modifier = Modifier.size(20.dp)) },
                text = { Text("New Automation", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Folders row
            if (tasks.isNotEmpty() || folders.isNotEmpty()) {
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
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(PurpleSecondary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = PurpleSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No Automations Defined",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Create multi-command shell sequences (e.g. restart service, backup db, update packages) and execute them in one tap.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Button(
                            onClick = { navController.navigate("task_builder?taskId=-1") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurpleSecondary,
                                contentColor = OnPurple
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create Automation", fontWeight = FontWeight.Bold)
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching automations",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "No automations found in this folder.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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

    // Modern Modal server selector
    if (taskToRun != null) {
        ModalBottomSheet(
            onDismissRequest = { taskToRun = null },
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp, start = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Select Target Server",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Choose the node to execute this automation on",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (servers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ColorError.copy(alpha = 0.1f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No servers available. Add a server node first.",
                            color = ColorError,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(servers) { srv ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        val tid = taskToRun
                                        taskToRun = null
                                        navController.navigate("terminal/$tid/${srv.id}")
                                    }
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                                color = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CyanPrimary.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Dns,
                                            contentDescription = null,
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = srv.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "${srv.username}@${srv.host}:${srv.port}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonospaceFontFamily),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = "Run",
                                        tint = PurpleBright,
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

    // Modern Delete confirmation dialog
    taskToDelete?.let { taskId ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ColorError.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = null,
                        tint = ColorError,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Delete Automation?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "This will remove the task and all its configured commands permanently.",
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
                            SnackbarController.showAlert(snackbarHostState, context.getString(R.string.tasks_deleted))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorError,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { taskToDelete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            title = "Nuova Cartella Task",
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

    // Move Task to Folder Dialog
    taskToMoveFolder?.let { task ->
        MoveToFolderDialog(
            currentFolder = task.folder,
            availableFolders = folders,
            onSelectFolder = { newFolder ->
                viewModel.updateTaskFolder(task.id, newFolder)
                taskToMoveFolder = null
            },
            onCreateNewFolder = {
                showCreateFolderDialog = true
            },
            onDismiss = {
                taskToMoveFolder = null
            }
        )
    }
}

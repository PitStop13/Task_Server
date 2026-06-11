package com.taskserver.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var taskToDelete by remember { mutableStateOf<Long?>(null) }
    var taskToRun by remember { mutableStateOf<Long?>(null) }
    val servers by viewModel.servers.collectAsState()

    Scaffold(
        containerColor = Background,
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                CustomSnackbarVisuals(data = data, alertType = AlertType.SUCCESS)
            }
        },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 24.dp),
                title = { 
                    Column {
                        Text(
                            "Automazioni Task ⚡", 
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), 
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text("${tasks.size} script totali", style = MaterialTheme.typography.labelMedium, color = PurpleSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Impostazioni",
                            tint = PurpleSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("task_builder?taskId=-1") },
                containerColor = PurpleSecondary,
                contentColor = OnPurple,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Filled.Add, "Aggiungi") },
                text = { Text("Nuovo Task", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("Nessun task", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text("Crea un task da eseguire via SSH.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(tasks, key = { it.id }) { task ->
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
                        onDelete = { taskToDelete = task.id }
                    )
                }
            }
        }
    }

    // Modal server selector per l'esecuzione
    if (taskToRun != null) {
        ModalBottomSheet(
            onDismissRequest = { taskToRun = null },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Esegui Task:", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text("Seleziona su quale server eseguire questo task.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                if (servers.isEmpty()) {
                    Text("Nessun server disponibile.", color = MaterialTheme.colorScheme.error)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(servers) { srv ->
                            OutlinedButton(
                                onClick = {
                                    val tid = taskToRun
                                    taskToRun = null
                                    navController.navigate("terminal/$tid/${srv.id}")
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Dns, null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(16.dp))
                                    Text(srv.name, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.weight(1f))
                                    Text(srv.host, style = MaterialTheme.typography.labelSmall, fontFamily = MonospaceFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete dialog
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Eliminare task?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Quest'azione è irreversibile.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToDelete?.let { viewModel.deleteTask(it) }
                        taskToDelete = null
                        scope.launch { SnackbarController.showAlert(snackbarHostState, "Task eliminato") }
                    }
                ) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

package com.taskserver.app.ui.screens.servers

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.ServerStatus
import com.taskserver.app.ui.components.*
import com.taskserver.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    navController: NavController,
    viewModel: ServerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val servers = uiState.servers
    val statusMap = uiState.statusMap
    val isChecking = statusMap.values.any { it == ServerStatus.CHECKING }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var serverToDelete by remember { mutableStateOf<Server?>(null) }

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
                            "Server SSH", 
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), 
                            color = TextPrimary
                        )
                        Text("${servers.size} configurati", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.checkAllConnectivity() }) {
                        Icon(
                            Icons.Filled.NetworkPing,
                            contentDescription = "Ping All",
                            tint = if (isChecking) TextTertiary else CyanPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Impostazioni",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("server_edit?serverId=-1") },
                containerColor = CyanPrimary,
                contentColor = Void,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Filled.Add, "Aggiungi") },
                text = { Text("Nuovo Server", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (servers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Dns, null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                    Spacer(Modifier.height(16.dp))
                    Text("Nessun server", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text("Aggiungi un server per iniziare.", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(servers, key = { it.id }) { server ->
                    ServerCard(
                        server = server,
                        status = statusMap[server.id] ?: ServerStatus.UNKNOWN,
                        onClick = { navController.navigate("server_edit?serverId=${server.id}") },
                        onEdit = { navController.navigate("server_edit?serverId=${server.id}") },
                        onDelete = { serverToDelete = server },
                        onTerminal = { navController.navigate("terminal/-1/${server.id}") }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    serverToDelete?.let { server ->
        AlertDialog(
            onDismissRequest = { serverToDelete = null },
            containerColor = SurfaceVariant,
            title = { Text("Eliminare server?", color = TextPrimary) },
            text = { Text("Quest'azione è irreversibile.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteServer(server)
                        serverToDelete = null
                        scope.launch {
                           SnackbarController.showAlert(snackbarHostState, "Server eliminato")
                        }
                    }
                ) {
                    Text("Elimina", color = ColorError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { serverToDelete = null }) {
                    Text("Annulla", color = TextTertiary)
                }
            }
        )
    }
}

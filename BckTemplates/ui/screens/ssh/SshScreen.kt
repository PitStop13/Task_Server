package com.taskserver.app.ui.screens.ssh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskserver.app.data.model.AuthType
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.ServerStatus
import com.taskserver.app.ui.components.AccentLine
import com.taskserver.app.ui.components.GlassCard
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

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 24.dp),
                title = {
                    Column {
                        Text(
                            "SSH Manuale",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimary
                        )
                        Text(
                            "${servers.size} server salvati",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.checkAllConnectivity() }) {
                        Icon(
                            Icons.Filled.NetworkPing,
                            contentDescription = "Aggiorna stato",
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
        }
    ) { padding ->
        if (servers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Terminal, null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                    Spacer(Modifier.height(16.dp))
                    Text("Nessun server salvato", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Aggiungi un server per aprire una shell SSH manuale.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { navController.navigate("server_edit?serverId=-1") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Void),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Nuovo Server", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Seleziona un server e apri una sessione SSH interattiva.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        AccentLine(brush = GradientCyan)
                    }
                }

                items(servers, key = { it.id }) { server ->
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

@Composable
private fun SshServerCard(
    server: Server,
    status: ServerStatus,
    onConnect: () -> Unit
) {
    val glowColor = when (status) {
        ServerStatus.ONLINE -> ColorOnline
        ServerStatus.OFFLINE -> ColorOffline
        ServerStatus.CHECKING -> ColorChecking
        ServerStatus.UNKNOWN -> CyanPrimary
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onConnect),
        glowColor = glowColor,
        borderAlpha = if (status == ServerStatus.ONLINE) 0.3f else 0.15f,
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceHigh, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Dns,
                        null,
                        tint = glowColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Person,
                            null,
                            tint = TextTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = server.username,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            fontFamily = MonospaceFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${server.host}:${server.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        fontFamily = MonospaceFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusBadge(status = status, showLabel = true, dotSize = 8.dp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (server.authType == AuthType.KEY) Icons.Filled.VpnKey else Icons.Filled.Lock,
                    null,
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (server.authType == AuthType.KEY) "Auth con chiave SSH" else "Auth con password SSH",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Void),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Terminal, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Apri SSH", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

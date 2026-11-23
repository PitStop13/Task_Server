package com.taskserver.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskserver.app.R
import com.taskserver.app.data.model.LogStatus
import com.taskserver.app.data.model.ServerStatus
import com.taskserver.app.ui.components.*
import com.taskserver.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val onlineCount = uiState.statusMap.count { it.value == ServerStatus.ONLINE }
    val offlineCount = uiState.statusMap.count { it.value == ServerStatus.OFFLINE }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars.add(WindowInsets(top = 8.dp)),
                title = {
                    Column {
                        Text(
                            text = "TaskServer",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Monitoraggio & Automazione",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary
                        )
                    }
                },
                actions = {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ═══════════════════════════════════════════════════════
            //  STATISTICHE PRINCIPALI
            // ═══════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoStatCard(
                    title = "Server",
                    value = "${uiState.serverCount}",
                    subtext = if (uiState.serverCount > 0) "$onlineCount Online" else "Nessun server",
                    icon = Icons.Filled.Dns,
                    accentColor = CyanPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("servers") }
                )
                BentoStatCard(
                    title = "Automazioni",
                    value = "${uiState.taskCount}",
                    subtext = if (uiState.taskCount > 0) "${uiState.taskCount} create" else "Nessun task",
                    icon = Icons.Filled.Bolt,
                    accentColor = PurpleSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("tasks") }
                )
            }

            // ═══════════════════════════════════════════════════════
            //  AZIONI RAPIDE
            // ═══════════════════════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Azioni Rapide",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "Nuovo Server",
                        icon = Icons.Filled.Add,
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("server_edit?serverId=-1") }
                    )
                    QuickActionButton(
                        title = "Nuovo Task",
                        icon = Icons.Filled.Bolt,
                        accentColor = PurpleSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("task_builder?taskId=-1") }
                    )
                    QuickActionButton(
                        title = "Terminale",
                        icon = Icons.Filled.Terminal,
                        accentColor = TerminalGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("ssh") }
                    )
                }
            }

            // ═══════════════════════════════════════════════════════
            //  I TUOI SERVER
            // ═══════════════════════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "I tuoi Server",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.servers.isNotEmpty()) {
                        TextButton(
                            onClick = { navController.navigate("servers") },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "Vedi tutti",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyanPrimary
                            )
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = CyanPrimary
                            )
                        }
                    }
                }

                if (uiState.servers.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { navController.navigate("server_edit?serverId=-1") }
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Dns,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Nessun server configurato",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Tocca qui per aggiungere il tuo primo server",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.servers.take(4).forEach { server ->
                            val status = uiState.statusMap[server.id] ?: ServerStatus.UNKNOWN
                            HomeServerNodeCard(
                                serverName = server.name,
                                host = server.host,
                                port = server.port,
                                username = server.username,
                                status = status,
                                onNodeClick = { navController.navigate("server_edit?serverId=${server.id}") },
                                onTerminalClick = { navController.navigate("terminal/-1/${server.id}") }
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════
            //  ATTIVITÀ RECENTE
            // ═══════════════════════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attività Recente",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.recentLogs.isNotEmpty()) {
                        TextButton(
                            onClick = { navController.navigate("logs") },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "Tutti i Log",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyanPrimary
                            )
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = CyanPrimary
                            )
                        }
                    }
                }

                if (uiState.recentLogs.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Nessuna esecuzione recente registrata",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.recentLogs.take(4).forEach { log ->
                            RecentActivityRow(
                                taskName = log.taskName,
                                serverName = log.serverName,
                                status = log.status,
                                timestamp = log.startedAt
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BentoStatCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, accentColor.copy(alpha = 0.22f), RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeServerNodeCard(
    serverName: String,
    host: String,
    port: Int,
    username: String,
    status: ServerStatus,
    onNodeClick: () -> Unit,
    onTerminalClick: () -> Unit
) {
    val statusColor = when (status) {
        ServerStatus.ONLINE -> ColorOnline
        ServerStatus.OFFLINE -> ColorOffline
        ServerStatus.CHECKING -> ColorChecking
        ServerStatus.UNKNOWN -> CyanPrimary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onNodeClick)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                RoundedCornerShape(16.dp)
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Dns,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = serverName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$username@$host:$port",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MonospaceFontFamily,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            StatusBadge(status = status, showLabel = true, dotSize = 6.dp)

            Spacer(Modifier.width(8.dp))

            FilledIconButton(
                onClick = onTerminalClick,
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CyanPrimary,
                    contentColor = OnCyan
                )
            ) {
                Icon(
                    Icons.Filled.Terminal,
                    contentDescription = "Terminale SSH",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentActivityRow(
    taskName: String,
    serverName: String,
    status: LogStatus,
    timestamp: Long
) {
    val (statusColor, iconVec) = when (status) {
        LogStatus.SUCCESS -> Pair(ColorSuccess, Icons.Filled.CheckCircle)
        LogStatus.ERROR   -> Pair(ColorError, Icons.Filled.Error)
        LogStatus.RUNNING -> Pair(ColorWarning, Icons.Filled.HourglassTop)
    }

    val timeStr = remember(timestamp) {
        SimpleDateFormat("HH:mm • dd MMM", Locale.getDefault()).format(Date(timestamp))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            ),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    iconVec,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = taskName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$serverName • $timeStr",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MonospaceFontFamily,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = status.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MonospaceFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = statusColor
                )
            }
        }
    }
}

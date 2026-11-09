package com.taskserver.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
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
    val isDark = LocalThemeIsDark.current
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    val onlineCount = uiState.statusMap.count { it.value == ServerStatus.ONLINE }
    val offlineCount = uiState.statusMap.count { it.value == ServerStatus.OFFLINE }
    val checkingCount = uiState.statusMap.count { it.value == ServerStatus.CHECKING }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(CyanPrimary, Color(0xFF0072FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Terminal,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "TaskServer",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                // Active indicator pill
                                val statusColor = when {
                                    uiState.servers.isEmpty() -> TextTertiary
                                    offlineCount > 0 -> ColorError
                                    checkingCount > 0 -> ColorChecking
                                    else -> ColorOnline
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (uiState.servers.isEmpty()) "READY" else "$onlineCount/${uiState.serverCount} UP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = MonospaceFontFamily
                                        ),
                                        color = statusColor
                                    )
                                }
                            }
                            Text(
                                text = "SSH & AUTOMATION ENGINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = CyanPrimary
                            )
                        }
                    }
                },
                actions = {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // ═══════════════════════════════════════════════════════
            //  HERO / FLEET OVERVIEW CARD
            // ═══════════════════════════════════════════════════════
            FleetOverviewHero(
                serverCount = uiState.serverCount,
                onlineCount = onlineCount,
                offlineCount = offlineCount,
                checkingCount = checkingCount,
                onAddServer = { navController.navigate("server_edit?serverId=-1") },
                onNavigateServers = { navController.navigate("servers") }
            )

            // ═══════════════════════════════════════════════════════
            //  BENTO STATS GRID
            // ═══════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BentoStatCard(
                    title = "Nodes",
                    value = "${uiState.serverCount}",
                    subtext = if (uiState.serverCount > 0) "$onlineCount Online" else "Add server",
                    icon = Icons.Filled.Dns,
                    glowColor = CyanPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("servers") }
                )
                BentoStatCard(
                    title = "Automations",
                    value = "${uiState.taskCount}",
                    subtext = "Shell scripts",
                    icon = Icons.Filled.Bolt,
                    glowColor = PurpleSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("tasks") }
                )
            }

            // ═══════════════════════════════════════════════════════
            //  QUICK ACTIONS ROW
            // ═══════════════════════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ModernSectionHeader(
                    title = "Quick Actions",
                    tag = "SHORTCUTS"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "New Node",
                        icon = Icons.Filled.Add,
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("server_edit?serverId=-1") }
                    )
                    QuickActionButton(
                        title = "New Task",
                        icon = Icons.Filled.Bolt,
                        accentColor = PurpleSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("task_builder?taskId=-1") }
                    )
                    QuickActionButton(
                        title = "SSH Shell",
                        icon = Icons.Filled.Terminal,
                        accentColor = TerminalGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("ssh") }
                    )
                    QuickActionButton(
                        title = "New Folder",
                        icon = Icons.Filled.CreateNewFolder,
                        accentColor = Color(0xFFFFB74D),
                        modifier = Modifier.weight(1f),
                        onClick = { showCreateFolderDialog = true }
                    )
                }
            }

            // ═══════════════════════════════════════════════════════
            //  CONFIGURED SERVERS / LIVE PREVIEW
            // ═══════════════════════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModernSectionHeader(
                        title = "Server Nodes",
                        tag = "${uiState.servers.size} TOTAL"
                    )
                    if (uiState.servers.isNotEmpty()) {
                        TextButton(
                            onClick = { navController.navigate("servers") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "View All",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyanPrimary
                            )
                            Spacer(Modifier.width(4.dp))
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
                    // Empty state onboarding card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = CyanPrimary,
                        borderAlpha = 0.15f,
                        cornerRadius = 20.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Dns,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "No server nodes yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Add your remote Linux servers, Raspberry Pis, or NAS to monitor uptime and run single-tap tasks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(18.dp))
                            Button(
                                onClick = { navController.navigate("server_edit?serverId=-1") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanPrimary,
                                    contentColor = OnCyan
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Add Server", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Display up to 3 servers with rich cards
                    uiState.servers.take(3).forEach { server ->
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

            // ═══════════════════════════════════════════════════════
            //  RECENT ACTIVITY STREAM
            // ═══════════════════════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModernSectionHeader(
                        title = "Recent Activity",
                        tag = "AUDIT"
                    )
                    if (uiState.recentLogs.isNotEmpty()) {
                        TextButton(
                            onClick = { navController.navigate("logs") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "All Logs",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyanPrimary
                            )
                            Spacer(Modifier.width(4.dp))
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
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = PurpleSecondary,
                        borderAlpha = 0.12f,
                        cornerRadius = 18.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    "No executions yet",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    "Run an automation script to inspect execution logs.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    uiState.recentLogs.take(3).forEach { log ->
                        RecentActivityRow(
                            taskName = log.taskName,
                            serverName = log.serverName,
                            status = log.status,
                            timestamp = log.startedAt
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
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
}

// ═══════════════════════════════════════════════════════
//  SUB-COMPONENTS FOR HOMESCREEN
// ═══════════════════════════════════════════════════════

@Composable
private fun FleetOverviewHero(
    serverCount: Int,
    onlineCount: Int,
    offlineCount: Int,
    checkingCount: Int,
    onAddServer: () -> Unit,
    onNavigateServers: () -> Unit
) {
    val isDark = LocalThemeIsDark.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = CyanPrimary,
        borderAlpha = 0.22f,
        cornerRadius = 24.dp,
        onClick = onNavigateServers
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlowDot(
                            color = if (serverCount > 0 && offlineCount == 0) ColorOnline else if (offlineCount > 0) ColorOffline else CyanPrimary,
                            size = 8.dp,
                            animate = true
                        )
                        Text(
                            text = "FLEET STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (serverCount == 0) {
                            "No Active Nodes"
                        } else if (offlineCount == 0) {
                            "All Nodes Operational"
                        } else {
                            "$offlineCount Node${if (offlineCount > 1) "s" else ""} Offline"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Interactive badge pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyanPrimary.copy(alpha = 0.12f))
                        .border(1.dp, CyanPrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$onlineCount / $serverCount UP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MonospaceFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CyanPrimary
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Multi-segment telemetry progress bar
            val total = if (serverCount == 0) 1f else serverCount.toFloat()
            val onlineFraction = if (serverCount == 0) 0f else (onlineCount / total)
            val checkingFraction = if (serverCount == 0) 0f else (checkingCount / total)
            val offlineFraction = if (serverCount == 0) 0f else (offlineCount / total)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                if (onlineFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(onlineFraction)
                            .fillMaxHeight()
                            .background(ColorOnline)
                    )
                }
                if (checkingFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(checkingFraction)
                            .fillMaxHeight()
                            .background(ColorChecking)
                    )
                }
                if (offlineFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(offlineFraction)
                            .fillMaxHeight()
                            .background(ColorOffline)
                    )
                }
                if (serverCount == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FleetMiniStat("Online", onlineCount, ColorOnline)
                    FleetMiniStat("Offline", offlineCount, ColorOffline)
                    if (checkingCount > 0) {
                        FleetMiniStat("Checking", checkingCount, ColorChecking)
                    }
                }

                Text(
                    text = "Tap for nodes →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = CyanPrimary
                )
            }
        }
    }
}

@Composable
private fun FleetMiniStat(label: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BentoStatCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    glowColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        glowColor = glowColor,
        borderAlpha = 0.20f,
        cornerRadius = 22.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(glowColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = glowColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
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
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, accentColor.copy(alpha = 0.22f), RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onNodeClick)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                RoundedCornerShape(18.dp)
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Dns,
                    contentDescription = null,
                    tint = if (status == ServerStatus.ONLINE) ColorOnline else CyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

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

            // Status Badge
            StatusBadge(status = status, showLabel = true, dotSize = 6.dp)

            Spacer(Modifier.width(8.dp))

            // Quick terminal launcher button
            FilledIconButton(
                onClick = onTerminalClick,
                modifier = Modifier.size(34.dp),
                shape = RoundedCornerShape(10.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CyanPrimary.copy(alpha = 0.15f),
                    contentColor = CyanPrimary
                )
            ) {
                Icon(
                    Icons.Filled.Terminal,
                    contentDescription = "Quick SSH",
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
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            ),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
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

            // Status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
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

@Composable
private fun ModernSectionHeader(
    title: String,
    tag: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (tag != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

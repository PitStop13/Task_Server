package com.taskserver.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 24.dp), 
                title = {
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            stringResource(R.string.home_title),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary,
                            letterSpacing = 2.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = CyanPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(16.dp)) 

            // ---- Bento-grid stats ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardStatCard(
                    title = "Server",
                    value = "${uiState.serverCount}",
                    icon = Icons.Filled.Dns,
                    glowColor = CyanPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("servers") }
                )
                DashboardStatCard(
                    title = "Task",
                    value = "${uiState.taskCount}",
                    icon = Icons.Filled.Bolt,
                    glowColor = PurpleSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("tasks") }
                )
            }

            // ---- Online servers ----
            val onlineCount = uiState.statusMap.count { it.value == ServerStatus.ONLINE }
            val offlineCount = uiState.statusMap.count { it.value == ServerStatus.OFFLINE }
            val checkingCount = uiState.statusMap.count { it.value == ServerStatus.CHECKING }

            if (uiState.servers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(stringResource(R.string.home_server_status))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatusChip("Online $onlineCount", ColorOnline, onlineCount > 0)
                        StatusChip("Offline $offlineCount", ColorOffline, offlineCount > 0)
                        if (checkingCount > 0) {
                            StatusChip("Check... $checkingCount", ColorChecking, true)
                        }
                    }

                    // Mini server list
                    uiState.servers.take(3).forEach { server ->
                        val status = uiState.statusMap[server.id] ?: ServerStatus.UNKNOWN
                        MiniServerRow(
                            name = server.name,
                            host = server.host,
                            status = status,
                            onClick = { navController.navigate("server_edit?serverId=${server.id}") }
                        )
                    }

                    if (uiState.servers.size > 3) {
                        TextButton(
                            onClick = { navController.navigate("servers") },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.home_show_all), color = CyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ---- Quick Actions ----
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionHeader(stringResource(R.string.home_quick_actions))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        stringResource(R.string.home_new_server),
                        Icons.Filled.Add,
                        CyanPrimary,
                        Modifier.weight(1f)
                    ) { navController.navigate("server_edit?serverId=-1") }

                    QuickActionCard(
                        stringResource(R.string.home_new_task),
                        Icons.Filled.Bolt,
                        PurpleSecondary,
                        Modifier.weight(1f)
                    ) { navController.navigate("task_builder?taskId=-1") }
                }
            }

            // ---- Recent Logs ----
            if (uiState.recentLogs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(stringResource(R.string.home_recent_logs))
                    uiState.recentLogs.forEach { log ->
                        RecentLogRow(
                            taskName = log.taskName,
                            serverName = log.serverName,
                            status = log.status,
                            timestamp = log.startedAt
                        )
                    }
                    TextButton(
                        onClick = { navController.navigate("logs") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.home_all_logs), color = CyanPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        AccentLine()
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    glowColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        glowColor = glowColor,
        borderAlpha = 0.2f,
        cornerRadius = 24.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(glowColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = glowColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                value,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatusChip(label: String, color: androidx.compose.ui.graphics.Color, active: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (active) color.copy(0.1f) else MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, if (active) color.copy(0.2f) else MaterialTheme.colorScheme.outline, RoundedCornerShape(30.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlowDot(color = if (active) color else MaterialTheme.colorScheme.onSurfaceVariant, size = 6.dp, animate = false)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MiniServerRow(
    name: String,
    host: String,
    status: ServerStatus,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Dns, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            Text(
                host,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MonospaceFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        StatusBadge(status = status, showLabel = false, dotSize = 8.dp)
    }
}

@Composable
private fun RecentLogRow(
    taskName: String,
    serverName: String,
    status: LogStatus,
    timestamp: Long
) {
    val (color, icon) = when (status) {
        LogStatus.SUCCESS -> Pair(ColorSuccess, Icons.Filled.CheckCircle)
        LogStatus.ERROR   -> Pair(ColorError, Icons.Filled.Error)
        LogStatus.RUNNING -> Pair(ColorWarning, Icons.Filled.HourglassTop)
    }

    val timeStr = remember(timestamp) {
        SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(Date(timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlowDot(color = color, animate = status == LogStatus.RUNNING)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                taskName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "$serverName • $timeStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = MonospaceFontFamily
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

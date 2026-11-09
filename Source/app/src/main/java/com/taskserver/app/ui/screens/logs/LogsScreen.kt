package com.taskserver.app.ui.screens.logs

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskserver.app.R
import com.taskserver.app.data.model.ExecutionLog
import com.taskserver.app.data.model.LogStatus
import com.taskserver.app.ui.components.*
import com.taskserver.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class LogFilter { ALL, SUCCESS, FAILED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    navController: NavController,
    viewModel: LogsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val logsList by viewModel.logs.collectAsState()

    var selectedFilter by remember { mutableStateOf(LogFilter.ALL) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirm by remember { mutableStateOf(false) }

    val filteredLogs = remember(logsList, selectedFilter) {
        when (selectedFilter) {
            LogFilter.ALL -> logsList
            LogFilter.SUCCESS -> logsList.filter { it.status == LogStatus.SUCCESS }
            LogFilter.FAILED -> logsList.filter { it.status == LogStatus.ERROR }
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            SnackbarController.showAlert(snackbarHostState, it)
            viewModel.clearMessages()
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
                                text = "Audit History",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${logsList.size} RECORDS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = MonospaceFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = CyanPrimary
                                )
                            }
                        }
                        Text(
                            text = "Historical execution logs & terminal output",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (logsList.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ColorError.copy(alpha = 0.12f))
                                .border(1.dp, ColorError.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                contentDescription = stringResource(R.string.logs_clear_all),
                                tint = ColorError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Pills Row
            if (logsList.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterTab(
                            label = "All (${logsList.size})",
                            selected = selectedFilter == LogFilter.ALL,
                            activeColor = CyanPrimary,
                            onClick = { selectedFilter = LogFilter.ALL }
                        )
                    }
                    item {
                        val successCount = logsList.count { it.status == LogStatus.SUCCESS }
                        FilterTab(
                            label = "Success ($successCount)",
                            selected = selectedFilter == LogFilter.SUCCESS,
                            activeColor = ColorSuccess,
                            onClick = { selectedFilter = LogFilter.SUCCESS }
                        )
                    }
                    item {
                        val failedCount = logsList.count { it.status == LogStatus.ERROR }
                        FilterTab(
                            label = "Failed ($failedCount)",
                            selected = selectedFilter == LogFilter.FAILED,
                            activeColor = ColorError,
                            onClick = { selectedFilter = LogFilter.FAILED }
                        )
                    }
                }
            }

            if (logsList.isEmpty()) {
                EmptyLogsPlaceholder(modifier = Modifier.fillMaxSize())
            } else if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No logs match the selected filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        LogItem(
                            log = log,
                            onClick = { viewModel.selectLog(log) }
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
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
                        Icons.Filled.DeleteSweep,
                        contentDescription = null,
                        tint = ColorError,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Clear History?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.logs_clear_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllLogs()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorError,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirm = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    uiState.selectedLog?.let { log ->
        LogDetailDialog(
            log = log,
            onDismiss = { viewModel.clearSelection() }
        )
    }
}

@Composable
private fun FilterTab(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) activeColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (selected) activeColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.5.sp
            ),
            color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LogItem(
    log: ExecutionLog,
    onClick: () -> Unit
) {
    val (statusColor, statusIcon) = when (log.status) {
        LogStatus.SUCCESS -> Pair(ColorSuccess, Icons.Filled.CheckCircle)
        LogStatus.ERROR   -> Pair(ColorError, Icons.Filled.Error)
        LogStatus.RUNNING -> Pair(ColorWarning, Icons.Filled.HourglassTop)
    }

    val timeStr = remember(log.startedAt) {
        SimpleDateFormat("HH:mm:ss • dd MMM", Locale.getDefault()).format(Date(log.startedAt))
    }

    val durationStr = remember(log.startedAt, log.finishedAt) {
        if (log.finishedAt != null) {
            val dur = log.finishedAt - log.startedAt
            val secs = dur / 1000
            if (secs < 60) "${secs}s" else "${secs / 60}m ${secs % 60}s"
        } else "running"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                statusColor.copy(alpha = 0.22f),
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.taskName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.serverName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MonospaceFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (log.exitCode != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "EXIT ${log.exitCode}",
                            fontFamily = MonospaceFontFamily,
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = durationStr,
                    fontFamily = MonospaceFontFamily,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LogDetailDialog(
    log: ExecutionLog,
    onDismiss: () -> Unit
) {
    val timeStr = remember(log.startedAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(log.startedAt))
    }
    val listState = rememberLazyListState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(0.96f),
        title = {
            Column {
                Text(
                    text = log.taskName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${log.serverName} (${log.serverHost})",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonospaceFontFamily),
                            color = CyanPrimary
                        )
                    }
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            val outputLines = log.output.lines()
            if (outputLines.isEmpty() || (outputLines.size == 1 && outputLines[0].isBlank())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Void)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.logs_detail_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = MonospaceFontFamily,
                        fontSize = 12.sp
                    )
                }
            } else {
                TerminalOutput(
                    lines = outputLines,
                    listState = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 460.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = OnCyan
                )
            ) {
                Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun EmptyLogsPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(24.dp),
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
                    .background(CyanPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Terminal,
                    null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Execution Records",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "When you execute automations on your servers, the full console output and exit codes will be logged here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

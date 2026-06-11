package com.taskserver.app.ui.screens.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskserver.app.data.model.ExecutionLog
import com.taskserver.app.data.model.LogStatus
import com.taskserver.app.ui.components.*
import com.taskserver.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    navController: NavController,
    viewModel: LogsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            SnackbarController.showAlert(snackbarHostState, it)
            viewModel.clearMessages()
        }
    }

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
                            "Log Esecuzioni", 
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), 
                            color = TextPrimary
                        )
                        Text(
                            "${uiState.logs.size} record salvati",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.logs.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Filled.DeleteSweep, "Cancella log", tint = ColorError.copy(0.8f))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        if (uiState.logs.isEmpty()) {
            EmptyLogsPlaceholder(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(uiState.logs, key = { it.id }) { log ->
                    LogItem(
                        log = log,
                        onClick = { viewModel.selectLog(log) }
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = SurfaceVariant,
            title = { Text("Cancella storico", color = TextPrimary) },
            text = { Text("Tutti i log delle esecuzioni verranno eliminati definitivamente. Procedere?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllLogs()
                    showClearConfirm = false
                }) {
                    Text("Elimina Tutto", color = ColorError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Annulla", color = TextTertiary)
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
        SimpleDateFormat("HH:mm:ss • dd/MM", Locale.getDefault()).format(Date(log.startedAt))
    }

    val durationStr = remember(log.startedAt, log.finishedAt) {
        if (log.finishedAt != null) {
            val dur = log.finishedAt - log.startedAt
            val secs = dur / 1000
            if (secs < 60) "${secs}s" else "${secs / 60}m ${secs % 60}s"
        } else "..."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
            .border(1.dp, statusColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(statusColor.copy(0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                log.taskName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(log.serverName, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(" • $timeStr", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            if (log.exitCode != null) {
                Text(
                    "exit ${log.exitCode}",
                    fontFamily = MonospaceFontFamily,
                    fontSize = 11.sp,
                    color = if (log.exitCode == 0) ColorSuccess else ColorError,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                durationStr,
                fontFamily = MonospaceFontFamily,
                fontSize = 10.sp,
                color = TextTertiary
            )
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
        containerColor = Surface,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Column {
                Text(log.taskName, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Dns, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Text(
                        "${log.serverName} (${log.serverHost})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Text(
                    timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = MonospaceFontFamily,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        text = {
            val outputLines = log.output.lines()
            if (outputLines.isEmpty() || (outputLines.size == 1 && outputLines[0].isBlank())) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp).background(Void, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Esecuzione vuota o interrotta", color = TextTertiary, fontFamily = MonospaceFontFamily)
                }
            } else {
                TerminalOutput(
                    lines = outputLines,
                    listState = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 500.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi", color = CyanPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun EmptyLogsPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(SurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Terminal, null, tint = TextTertiary, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Sistema pulito", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Nessuna operazione registrata nel log", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

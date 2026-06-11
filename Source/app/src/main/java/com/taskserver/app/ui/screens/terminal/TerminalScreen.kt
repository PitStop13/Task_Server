package com.taskserver.app.ui.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.taskserver.app.R
import com.taskserver.app.ui.components.GlowDot
import com.taskserver.app.ui.components.TerminalOutput
import com.taskserver.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    navController: NavController,
    taskId: Long,
    serverId: Long,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(taskId, serverId) {
        viewModel.execute(taskId, serverId)
    }

    LaunchedEffect(uiState.lines.size) {
        if (uiState.lines.isNotEmpty()) {
            listState.animateScrollToItem(uiState.lines.size - 1)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            uiState.taskName.ifEmpty {
                                if (uiState.isInteractive) stringResource(R.string.terminal_manual_ssh) else stringResource(R.string.terminal_remote)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        if (uiState.serverName.isNotEmpty()) {
                            Text(
                                uiState.serverName,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = MonospaceFontFamily,
                                color = TextTertiary
                            )
                        }
                        if (uiState.serverHost.isNotEmpty()) {
                            Text(
                                if (uiState.serverUsername.isNotEmpty()) {
                                    "${uiState.serverUsername}@${uiState.serverHost}"
                                } else {
                                    uiState.serverHost
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = MonospaceFontFamily,
                                color = TextTertiary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back), tint = TextPrimary)
                    }
                },
                actions = {
                    if (uiState.isRunning) {
                        GlowDot(color = CyanPrimary, size = 10.dp, animate = true)
                        Spacer(Modifier.width(16.dp))
                    } else if (uiState.exitCode != null) {
                        val color = if (uiState.exitCode == 0) ColorSuccess else ColorError
                        GlowDot(color = color, size = 10.dp, animate = false)
                        Spacer(Modifier.width(16.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Execution Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Terminal,
                    null,
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                
                val statusText: String
                val statusColor: androidx.compose.ui.graphics.Color
                when {
                    uiState.isRunning -> {
                        statusText = stringResource(R.string.terminal_running)
                        statusColor = TerminalAmber
                    }
                    uiState.exitCode == 0 -> {
                        statusText = stringResource(R.string.terminal_success)
                        statusColor = TerminalGreen
                    }
                    uiState.exitCode != null -> {
                        statusText = stringResource(R.string.terminal_error, uiState.exitCode!!)
                        statusColor = TerminalRed
                    }
                    uiState.error != null -> {
                        statusText = stringResource(R.string.terminal_failed)
                        statusColor = TerminalRed
                    }
                    else -> {
                        statusText = stringResource(R.string.terminal_init)
                        statusColor = CyanPrimary
                    }
                }
                
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = MonospaceFontFamily,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // The actual terminal output block
            TerminalOutput(
                lines = uiState.lines,
                listState = listState,
                serverHost = uiState.serverHost,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(Modifier.height(16.dp))

            // Control Actions
            if (uiState.isInteractive) {
                var commandText by remember { mutableStateOf("") }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commandText,
                        onValueChange = { commandText = it },
                        placeholder = { Text(stringResource(R.string.terminal_command_placeholder), color = TextTertiary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = BorderDefault,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CyanPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledIconButton(
                        onClick = { 
                            if (commandText.isNotBlank()) {
                                viewModel.sendInteractiveCommand(commandText)
                                commandText = ""
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = CyanPrimary,
                            contentColor = Void
                        ),
                        enabled = uiState.isRunning
                    ) {
                        Icon(Icons.Filled.Send, stringResource(R.string.terminal_send))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.isRunning) {
                        Button(
                            onClick = { viewModel.cancelExecution() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorError.copy(0.15f),
                                contentColor = ColorError
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.StopCircle, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.terminal_terminate), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.execute(taskId, serverId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = Void
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Replay, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.terminal_run_again), fontWeight = FontWeight.Bold)
                        }
    
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDefault)
                        ) {
                            Text(stringResource(R.string.terminal_go_back), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Color

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
        if (uiState.lines.isNotEmpty() && !uiState.isInteractive) {
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
            if (uiState.isInteractive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF07080D)) // Deep dark for xterm
                ) {
                    XTermWebView(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = viewModel,
                        onTerminalReady = { }
                    )
                }
            } else {
                TerminalOutput(
                    lines = uiState.lines,
                    listState = listState,
                    serverHost = uiState.serverHost,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Control Actions
            if (uiState.isInteractive) {
                // Interactive Toolbar for SSH
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Utility Buttons
                    val textButtons = listOf(
                        "ESC" to byteArrayOf(0x1B),
                        "TAB" to byteArrayOf(0x09),
                        "CTRL+C" to byteArrayOf(0x03)
                    )

                    textButtons.forEach { (label, bytes) ->
                        OutlinedButton(
                            onClick = { viewModel.sendRawInteractiveBytes(bytes) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SurfaceContainer,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDefault),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Divider
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderSubtle))
                    Spacer(modifier = Modifier.width(4.dp))

                    // Arrow Buttons
                    val arrowButtons = listOf(
                        Icons.Filled.KeyboardArrowLeft to byteArrayOf(0x1B, 0x5B, 0x44),
                        Icons.Filled.KeyboardArrowDown to byteArrayOf(0x1B, 0x5B, 0x42),
                        Icons.Filled.KeyboardArrowUp to byteArrayOf(0x1B, 0x5B, 0x41),
                        Icons.Filled.KeyboardArrowRight to byteArrayOf(0x1B, 0x5B, 0x43)
                    )

                    arrowButtons.forEach { (icon, bytes) ->
                        FilledIconButton(
                            onClick = { viewModel.sendRawInteractiveBytes(bytes) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = SurfaceHigh,
                                contentColor = CyanPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                        }
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

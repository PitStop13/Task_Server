package com.taskserver.app.ui.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
        if (uiState.lines.isNotEmpty() && !uiState.isInteractive) {
            listState.scrollToItem(uiState.lines.size - 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(top = 12.dp, start = 8.dp, end = 8.dp),
                title = {
                    Column {
                        Text(
                            text = uiState.taskName.ifEmpty {
                                if (uiState.isInteractive) "SSH Console" else "Remote Execution"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (uiState.serverName.isNotEmpty() || uiState.serverHost.isNotEmpty()) {
                            Text(
                                text = if (uiState.serverUsername.isNotEmpty()) {
                                    "${uiState.serverUsername}@${uiState.serverHost}"
                                } else {
                                    "${uiState.serverName} • ${uiState.serverHost}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MonospaceFontFamily,
                                    fontSize = 11.sp
                                ),
                                color = CyanPrimary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            val (statusText, statusColor) = when {
                uiState.isRunning -> Pair(stringResource(R.string.terminal_running), TerminalAmber)
                uiState.exitCode == 0 -> Pair(stringResource(R.string.terminal_success), TerminalGreen)
                uiState.exitCode != null -> Pair(stringResource(R.string.terminal_error, uiState.exitCode!!), TerminalRed)
                uiState.error != null -> Pair(stringResource(R.string.terminal_failed), TerminalRed)
                else -> Pair(stringResource(R.string.terminal_init), CyanPrimary)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlowDot(color = statusColor, size = 8.dp, animate = uiState.isRunning)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = MonospaceFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // The actual terminal output block
            if (uiState.isInteractive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .background(Color(0xFF06080F))
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

            Spacer(Modifier.height(14.dp))

            // Control Actions
            if (uiState.isInteractive) {
                var commandInput by remember { mutableStateOf("") }

                // Quick command input bar for effortless typing and sending commands
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick SUDO prefix button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable {
                                commandInput = if (commandInput.startsWith("sudo ")) {
                                    commandInput.removePrefix("sudo ")
                                } else {
                                    "sudo $commandInput"
                                }
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "SUDO",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = MonospaceFontFamily,
                                fontSize = 11.sp
                            ),
                            color = CyanPrimary
                        )
                    }

                    // Input field
                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { commandInput = it },
                        placeholder = {
                            Text(
                                "Comando shell...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = MonospaceFontFamily,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = MonospaceFontFamily,
                            fontSize = 13.sp
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = {
                                if (commandInput.isNotBlank()) {
                                    viewModel.sendInteractiveCommand(commandInput.trim())
                                    commandInput = ""
                                }
                            }
                        ),
                        trailingIcon = {
                            if (commandInput.isNotEmpty()) {
                                IconButton(onClick = { commandInput = "" }) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = "Cancella",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = CyanPrimary
                        )
                    )

                    // Send Button
                    IconButton(
                        onClick = {
                            if (commandInput.isNotBlank()) {
                                viewModel.sendInteractiveCommand(commandInput.trim())
                                commandInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanPrimary)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Invia",
                            tint = OnCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Interactive Toolbar for SSH Mobile Accessory Keys
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val toolbarButtons = listOf(
                        "ESC" to byteArrayOf(0x1B),
                        "TAB" to byteArrayOf(0x09),
                        "CTRL+C" to byteArrayOf(0x03),
                        "ENTER" to byteArrayOf(0x0D),
                        "▲" to byteArrayOf(0x1B, 0x5B, 0x41),
                        "▼" to byteArrayOf(0x1B, 0x5B, 0x42),
                        "◀" to byteArrayOf(0x1B, 0x5B, 0x44),
                        "▶" to byteArrayOf(0x1B, 0x5B, 0x43),
                        "CLEAR" to byteArrayOf(0x0C)
                    )

                    toolbarButtons.forEach { (label, bytes) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable {
                                    if (bytes.isNotEmpty()) {
                                        viewModel.sendRawInteractiveBytes(bytes)
                                    }
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = MonospaceFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.isRunning) {
                        Button(
                            onClick = { viewModel.cancelExecution() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorError.copy(alpha = 0.15f),
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
                                contentColor = OnCyan
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
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(stringResource(R.string.terminal_go_back), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

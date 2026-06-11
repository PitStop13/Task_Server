package com.taskserver.app.ui.screens.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.taskserver.app.data.model.*
import com.taskserver.app.ui.components.*
import com.taskserver.app.ui.theme.*

private val ICON_OPTIONS = listOf(
    "terminal", "docker", "system", "monitor", "folder", "network", "bolt", "restart", "update"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBuilderScreen(
    navController: NavController,
    taskId: Long,
    viewModel: TaskBuilderViewModel = hiltViewModel()
) {
    val builderState by viewModel.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var iconName by remember { mutableStateOf("terminal") }
    var commands by remember { mutableStateOf(listOf<TaskCommand>()) }
    var pinnedServerId by remember { mutableStateOf<Long?>(null) }
    var isEditMode by remember { mutableStateOf(false) }

    var showPredefined by remember { mutableStateOf(false) }
    var customCommandText by remember { mutableStateOf("") }

    LaunchedEffect(taskId) {
        if (taskId > 0) {
            viewModel.getTask(taskId)?.let { task ->
                isEditMode = true
                name = task.name
                description = task.description
                iconName = task.iconName
                pinnedServerId = task.pinnedServerId
                commands = viewModel.deserializeCommands(task.commandsJson)
            }
        }
    }

    LaunchedEffect(builderState.saved) {
        if (builderState.saved) navController.popBackStack()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Configurazione Task" else "Nuovo Task",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Indietro", tint = TextPrimary)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // -- General --
            Text("ANAGRAFICA".uppercase(), style = MaterialTheme.typography.labelSmall, color = PurpleSecondary, letterSpacing = 2.sp)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Task") },
                leadingIcon = { Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = cyberFieldColors()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrizione (opzionale)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = cyberFieldColors()
            )

            // -- Icon --
            Spacer(Modifier.height(8.dp))
            Text("ICONA".uppercase(), style = MaterialTheme.typography.labelSmall, color = PurpleSecondary, letterSpacing = 2.sp)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ICON_OPTIONS.forEach { ico ->
                    val selected = iconName == ico
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) PurpleSecondary.copy(0.2f) else SurfaceContainer)
                            .border(1.dp, if (selected) PurpleSecondary else BorderDefault, RoundedCornerShape(12.dp))
                            .clickable { iconName = ico },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconFromName(ico),
                            contentDescription = ico,
                            tint = if (selected) PurpleBright else TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // -- Server associato --
            Spacer(Modifier.height(8.dp))
            Text("ATTACCA AL SERVER".uppercase(), style = MaterialTheme.typography.labelSmall, color = PurpleSecondary, letterSpacing = 2.sp)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = pinnedServerId == null,
                    onClick = { pinnedServerId = null },
                    label = { Text("Chiedi sempre") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleSecondary.copy(0.2f),
                        selectedLabelColor = PurpleBright,
                        containerColor = SurfaceContainer,
                        labelColor = TextTertiary
                    )
                )
                if (builderState.servers.isNotEmpty()) {
                    builderState.servers.forEach { srv ->
                        val sel = pinnedServerId == srv.id
                        FilterChip(
                            selected = sel,
                            onClick = { pinnedServerId = if (sel) null else srv.id },
                            label = { Text("${srv.name}") },
                            leadingIcon = { if (sel) Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleSecondary.copy(0.2f),
                                selectedLabelColor = PurpleBright,
                                containerColor = SurfaceContainer,
                                labelColor = TextTertiary
                            )
                        )
                    }
                }
            }

            // -- Comandi --
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SEQUENCE (${commands.size})".uppercase(), style = MaterialTheme.typography.labelSmall, color = CyanPrimary, letterSpacing = 2.sp)
                AccentLine(brush = GradientCyan)
            }

            // Aggiungi comando custom
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customCommandText,
                    onValueChange = { customCommandText = it },
                    placeholder = { Text("sudo apt update...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = MonospaceFontFamily,
                        fontSize = 13.sp,
                        color = TextPrimary
                    ),
                    colors = cyberFieldColors()
                )
                FilledIconButton(
                    onClick = {
                        if (customCommandText.isNotBlank()) {
                            commands = commands + TaskCommand(
                                text = customCommandText.trim(),
                                order = commands.size,
                                isCustom = true
                            )
                            customCommandText = ""
                        }
                    },
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CyanPrimary,
                        contentColor = Void
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, "Aggiungi", modifier = Modifier.size(24.dp))
                }
            }

            // Preset commands toggle
            TextButton(onClick = { showPredefined = !showPredefined }) {
                Icon(
                    if (showPredefined) Icons.Filled.ExpandLess else Icons.Filled.LocalLibrary,
                    null, modifier = Modifier.size(18.dp), tint = TextTertiary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (showPredefined) "Nascondi catalogo" else "Sfoglia catalogo comandi",
                    color = TextTertiary,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = showPredefined) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val categories = PREDEFINED_COMMANDS.groupBy { it.category }
                    categories.forEach { (cat, cmds) ->
                        Column {
                            Text(cat.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.padding(bottom = 8.dp))
                            cmds.forEach { pre ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceVariant)
                                        .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
                                        .clickable {
                                            commands = commands + TaskCommand(
                                                text = pre.command,
                                                order = commands.size,
                                                isCustom = false
                                            )
                                            showPredefined = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pre.label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            pre.command,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = MonospaceFontFamily,
                                            color = CyanPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(Icons.Filled.AddCircleOutline, null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Command List
            commands.forEachIndexed { index, cmd ->
                CommandItem(
                    command = cmd,
                    index = index,
                    onRemove = { commands = commands.toMutableList().also { it.removeAt(index) } },
                    onMoveUp = {
                        if (index > 0) {
                            commands = commands.toMutableList().also {
                                val temp = it[index]
                                it[index] = it[index - 1]
                                it[index - 1] = temp
                            }
                        }
                    },
                    onMoveDown = {
                        if (index < commands.size - 1) {
                            commands = commands.toMutableList().also {
                                val temp = it[index]
                                it[index] = it[index + 1]
                                it[index + 1] = temp
                            }
                        }
                    }
                )
            }

            builderState.error?.let {
                Text(it, color = ColorError, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.save(
                        taskId = if (isEditMode) taskId else -1,
                        name = name,
                        description = description,
                        iconName = iconName,
                        commands = commands,
                        pinnedServerId = pinnedServerId
                    )
                },
                enabled = name.isNotBlank() && commands.isNotEmpty() && !builderState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleSecondary,
                    contentColor = Void
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (builderState.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Void, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditMode) "Aggiorna Task" else "Salva Task", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CommandItem(
    command: TaskCommand,
    index: Int,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Void)
            .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
            .padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp).background(SurfaceHigh, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = CyanPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            command.text,
            fontFamily = MonospaceFontFamily,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) { Icon(Icons.Filled.KeyboardArrowUp, null, tint = TextTertiary, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) { Icon(Icons.Filled.KeyboardArrowDown, null, tint = TextTertiary, modifier = Modifier.size(16.dp)) }
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Close, null, tint = ColorError.copy(0.8f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun cyberFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextSecondary,
    cursorColor = CyanPrimary,
    focusedBorderColor = CyanPrimary,
    unfocusedBorderColor = BorderDefault,
    focusedLabelColor = CyanPrimary,
    unfocusedLabelColor = TextTertiary,
    focusedLeadingIconColor = CyanPrimary,
    unfocusedLeadingIconColor = TextTertiary,
    focusedContainerColor = SurfaceContainer,
    unfocusedContainerColor = SurfaceContainer
)

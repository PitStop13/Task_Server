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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.taskserver.app.data.model.*
import com.taskserver.app.ui.components.*
import com.taskserver.app.ui.theme.*
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var iconName by remember { mutableStateOf("terminal") }
    var commands by remember { mutableStateOf(listOf<TaskCommand>()) }
    var pinnedServerId by remember { mutableStateOf<Long?>(null) }
    var folder by remember { mutableStateOf<String?>(null) }
    var isEditMode by remember { mutableStateOf(false) }

    var showFolderPicker by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

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
                folder = task.folder
                commands = viewModel.deserializeCommands(task.commandsJson)
            }
        }
    }

    LaunchedEffect(builderState.saved) {
        if (builderState.saved) navController.popBackStack()
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
                    Text(
                        text = if (isEditMode) "Configure Automation" else "New Automation",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── General ──
            Text(
                text = "BASIC INFORMATION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = PurpleSecondary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Task Name (e.g. Reboot Services)") },
                leadingIcon = {
                    Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(18.dp), tint = PurpleSecondary)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = modernFieldColors(PurpleSecondary)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = modernFieldColors(PurpleSecondary)
            )

            // ── Section: Folder Organization ──
            Text(
                text = "FOLDER ORGANIZATION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = PurpleSecondary
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .clickable { showFolderPicker = true },
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (folder != null) Icons.Filled.Folder else Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = PurpleBright,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Cartella",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = folder ?: "Nessuna cartella",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Seleziona cartella",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Icon Selection ──
            Text(
                text = "SELECT ICON",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = PurpleSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ICON_OPTIONS.forEach { ico ->
                    val selected = iconName == ico
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) PurpleSecondary.copy(alpha = 0.20f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.5.dp,
                                if (selected) PurpleSecondary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { iconName = ico },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconFromName(ico),
                            contentDescription = ico,
                            tint = if (selected) PurpleBright else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // ── Target Server Pinning ──
            Text(
                text = "TARGET SERVER BINDING",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = PurpleSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = pinnedServerId == null,
                    onClick = { pinnedServerId = null },
                    label = { Text("Prompt each time") },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleSecondary.copy(0.20f),
                        selectedLabelColor = PurpleBright,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                if (builderState.servers.isNotEmpty()) {
                    builderState.servers.forEach { srv ->
                        val sel = pinnedServerId == srv.id
                        FilterChip(
                            selected = sel,
                            onClick = { pinnedServerId = if (sel) null else srv.id },
                            label = { Text(srv.name) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(0.20f),
                                selectedLabelColor = CyanPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // ── Commands Sequence ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "COMMAND SEQUENCE (${commands.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = PurpleSecondary
                )

                TextButton(
                    onClick = { showPredefined = !showPredefined }
                ) {
                    Icon(
                        if (showPredefined) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = CyanPrimary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (showPredefined) "Hide Catalog" else "Browse Catalog",
                        color = CyanPrimary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Add Custom Command Input Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customCommandText,
                        onValueChange = { customCommandText = it },
                        placeholder = { Text("sudo systemctl restart nginx", fontFamily = MonospaceFontFamily, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = MonospaceFontFamily),
                        colors = modernFieldColors(CyanPrimary),
                        trailingIcon = {
                            if (customCommandText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        commands = commands + TaskCommand(
                                            text = customCommandText.trim(),
                                            order = commands.size
                                        )
                                        customCommandText = ""
                                    }
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = CyanPrimary)
                                }
                            }
                        }
                    )

                    Button(
                        onClick = {
                            if (customCommandText.isNotBlank()) {
                                commands = commands + TaskCommand(
                                    text = customCommandText.trim(),
                                    order = commands.size
                                )
                                customCommandText = ""
                            }
                        },
                        enabled = customCommandText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary.copy(alpha = 0.15f),
                            contentColor = CyanPrimary
                        )
                    ) {
                        Icon(Icons.Filled.Terminal, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Shell Command", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Predefined Commands Catalog Dropdown Box
            AnimatedVisibility(visible = showPredefined) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CyanPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PREDEFINED RECIPES (TAP TO ADD)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MonospaceFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = CyanPrimary
                        )

                        // Group commands by category
                        val categories = PREDEFINED_COMMANDS.groupBy { it.category }
                        categories.forEach { (cat, list) ->
                            Text(
                                text = cat.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            list.forEach { pre ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            commands = commands + TaskCommand(
                                                text = pre.command,
                                                order = commands.size,
                                                isCustom = false
                                            )
                                        },
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pre.label,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = pre.command,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = MonospaceFontFamily,
                                                    fontSize = 11.sp
                                                ),
                                                color = CyanPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            Icons.Filled.AddCircleOutline,
                                            contentDescription = "Add command",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Command List Items
            commands.forEachIndexed { index, cmd ->
                CommandItemRow(
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

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    viewModel.save(
                        taskId = if (isEditMode) taskId else -1,
                        name = name,
                        description = description,
                        iconName = iconName,
                        commands = commands,
                        pinnedServerId = pinnedServerId,
                        folder = folder
                    )
                },
                enabled = name.isNotBlank() && commands.isNotEmpty() && !builderState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleSecondary,
                    contentColor = OnPurple
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (builderState.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = OnPurple, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "Save Automation" else "Create Automation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }

    // Folder Picker Dialog
    if (showFolderPicker) {
        MoveToFolderDialog(
            currentFolder = folder,
            availableFolders = builderState.folders,
            onSelectFolder = { selected ->
                folder = selected
                showFolderPicker = false
            },
            onCreateNewFolder = {
                showFolderPicker = false
                showCreateFolderDialog = true
            },
            onDismiss = { showFolderPicker = false }
        )
    }

    // Create New Folder Dialog
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            title = "Nuova Cartella Task",
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { newFolderName ->
                scope.launch {
                    viewModel.createFolder(newFolderName)
                    folder = newFolderName
                }
                showCreateFolderDialog = false
            }
        )
    }
}

@Composable
private fun CommandItemRow(
    command: TaskCommand,
    index: Int,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
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
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MonospaceFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = CyanPrimary
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = command.text,
                fontFamily = MonospaceFontFamily,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, null, tint = ColorError, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun modernFieldColors(accentColor: Color) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = accentColor,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor = accentColor,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = accentColor
)

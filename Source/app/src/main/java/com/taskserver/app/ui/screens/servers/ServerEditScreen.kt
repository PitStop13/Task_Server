package com.taskserver.app.ui.screens.servers

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskserver.app.R
import com.taskserver.app.data.model.AuthType
import com.taskserver.app.data.model.Server
import com.taskserver.app.ui.components.*
import com.taskserver.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerEditScreen(
    navController: NavController,
    serverId: Long,
    viewModel: ServerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf("root") }
    var authType by remember { mutableStateOf(AuthType.PASSWORD) }
    var password by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    var sudoPassword by remember { mutableStateOf("") }
    var folder by remember { mutableStateOf<String?>(null) }

    var sshPasswordVisible by remember { mutableStateOf(false) }
    var sudoPasswordVisible by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    var showFolderPicker by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(serverId) {
        if (serverId > 0) {
            viewModel.getServer(serverId)?.let { srv ->
                isEditMode = true
                name = srv.name
                host = srv.host
                port = srv.port.toString()
                username = srv.username
                authType = srv.authType
                folder = srv.folder

                val credentials = viewModel.getCredential(srv)
                password = credentials.sshPassword ?: ""
                privateKey = credentials.privateKey ?: ""
                sudoPassword = credentials.sudoPassword ?: ""
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                CustomSnackbarVisuals(data = data, alertType = AlertType.ERROR)
            }
        },
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                title = {
                    Text(
                        text = if (isEditMode) "Edit Server" else "New Server Node",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Section: Identification ──
            FormSectionHeader(title = "Node Identity")
            ModernTextField(
                value = name,
                onValueChange = { name = it },
                label = "Display Name",
                placeholder = "es. Production Web, HomeLab NAS...",
                icon = Icons.AutoMirrored.Filled.Label
            )

            // ── Section: Organization / Folder ──
            FormSectionHeader(title = "Folder Organization")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
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
                            tint = CyanPrimary,
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

            // ── Section: Network ──
            FormSectionHeader(title = "Connection Settings")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = "Host or IP Address",
                    placeholder = "192.168.1.100 o dominio",
                    icon = Icons.Filled.Language,
                    modifier = Modifier.weight(1f),
                    isMonospace = true
                )
                ModernTextField(
                    value = port,
                    onValueChange = { if (it.all { char -> char.isDigit() }) port = it },
                    label = "Port",
                    placeholder = "22",
                    icon = Icons.Filled.Numbers,
                    modifier = Modifier.width(100.dp),
                    isMonospace = true
                )
            }

            // ── Section: Credentials ──
            FormSectionHeader(title = "Authentication")
            ModernTextField(
                value = username,
                onValueChange = { username = it },
                label = "SSH Username",
                placeholder = "root, ubuntu, pit...",
                icon = Icons.Filled.Person,
                isMonospace = true
            )

            // Auth Type Selector
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    AuthTypeSegment(
                        text = "Password",
                        icon = Icons.Filled.Lock,
                        selected = authType == AuthType.PASSWORD,
                        onClick = { authType = AuthType.PASSWORD },
                        modifier = Modifier.weight(1f)
                    )
                    AuthTypeSegment(
                        text = "SSH Private Key",
                        icon = Icons.Filled.VpnKey,
                        selected = authType == AuthType.KEY,
                        onClick = { authType = AuthType.KEY },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedVisibility(
                visible = authType == AuthType.PASSWORD,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ModernTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "SSH Password",
                    icon = Icons.Filled.Password,
                    visualTransformation = if (sshPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { sshPasswordVisible = !sshPasswordVisible }) {
                            Icon(
                                if (sshPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = authType == AuthType.KEY,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModernTextField(
                        value = privateKey,
                        onValueChange = { privateKey = it },
                        label = "Private Key (RSA, Ed25519 PEM)",
                        icon = Icons.Filled.VpnKey,
                        singleLine = false,
                        maxLines = 6,
                        isMonospace = true,
                        modifier = Modifier.height(140.dp)
                    )
                }
            }

            // ── Section: Privilege Escalation ──
            FormSectionHeader(title = "Privileges (Sudo)")
            ModernTextField(
                value = sudoPassword,
                onValueChange = { sudoPassword = it },
                label = "Sudo Password (optional)",
                icon = Icons.Filled.Security,
                visualTransformation = if (sudoPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { sudoPasswordVisible = !sudoPasswordVisible }) {
                        Icon(
                            if (sudoPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle sudo visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            // Save / Update Action Button
            Button(
                onClick = {
                    val authValid = if (authType == AuthType.PASSWORD) password.isNotBlank() else privateKey.isNotBlank()
                    if (name.isBlank() || host.isBlank() || port.isBlank() || username.isBlank() || !authValid) {
                        scope.launch { SnackbarController.showAlert(snackbarHostState, context.getString(R.string.server_edit_fill_all)) }
                        return@Button
                    }
                    viewModel.saveServer(
                        id = if (isEditMode) serverId else 0,
                        name = name.trim(),
                        host = host.trim(),
                        port = port.toIntOrNull() ?: 22,
                        username = username.trim(),
                        authType = authType,
                        password = password,
                        privateKey = privateKey,
                        sudoPassword = sudoPassword,
                        folder = folder
                    )
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = OnCyan
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Update Configuration" else "Save Server Node",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Folder Picker Dialog
    if (showFolderPicker) {
        MoveToFolderDialog(
            currentFolder = folder,
            availableFolders = uiState.folders,
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
            title = "Nuova Cartella Server",
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { newFolderName ->
                viewModel.createFolder(newFolderName)
                folder = newFolderName
                showCreateFolderDialog = false
            }
        )
    }
}

@Composable
private fun FormSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = MonospaceFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        ),
        color = CyanPrimary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isMonospace: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = if (isMonospace) MonospaceFontFamily else MaterialTheme.typography.bodyMedium.fontFamily
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = CyanPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = CyanPrimary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = CyanPrimary
        )
    )
}

@Composable
private fun AuthTypeSegment(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) CyanPrimary.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

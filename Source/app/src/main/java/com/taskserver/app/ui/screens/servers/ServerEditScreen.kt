package com.taskserver.app.ui.screens.servers

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf("root") }
    var authType by remember { mutableStateOf(AuthType.PASSWORD) }
    var password by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    var sudoPassword by remember { mutableStateOf("") }
    
    var sshPasswordVisible by remember { mutableStateOf(false) }
    var sudoPasswordVisible by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

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

                val credentials = viewModel.getCredential(srv)
                password = credentials.sshPassword ?: ""
                privateKey = credentials.privateKey ?: ""
                sudoPassword = credentials.sudoPassword ?: ""
            }
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
                title = { 
                    Text(
                        if (isEditMode) stringResource(R.string.server_edit_title_edit) else stringResource(R.string.server_edit_title_new),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back), tint = TextPrimary)
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
            Text(stringResource(R.string.server_edit_general), style = MaterialTheme.typography.labelSmall, color = CyanPrimary, letterSpacing = 2.sp)
            GlassTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.server_edit_name),
                icon = Icons.Filled.Label
            )

            // -- Network --
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.server_edit_network), style = MaterialTheme.typography.labelSmall, color = CyanPrimary, letterSpacing = 2.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = stringResource(R.string.server_edit_host),
                    icon = Icons.Filled.Language,
                    modifier = Modifier.weight(1f)
                )
                GlassTextField(
                    value = port,
                    onValueChange = { port = it.filter { c -> c.isDigit() } },
                    label = stringResource(R.string.server_edit_port),
                    icon = Icons.Filled.Numbers,
                    modifier = Modifier.width(100.dp)
                )
            }

            // -- Auth --
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.server_edit_auth), style = MaterialTheme.typography.labelSmall, color = CyanPrimary, letterSpacing = 2.sp)
            GlassTextField(
                value = username,
                onValueChange = { username = it },
                label = stringResource(R.string.server_edit_username),
                icon = Icons.Filled.Person
            )

            // Auth Type Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainer)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AuthTypeButton(
                    text = stringResource(R.string.server_edit_password),
                    selected = authType == AuthType.PASSWORD,
                    onClick = { authType = AuthType.PASSWORD },
                    modifier = Modifier.weight(1f)
                )
                AuthTypeButton(
                    text = stringResource(R.string.server_edit_ssh_key),
                    selected = authType == AuthType.KEY,
                    onClick = { authType = AuthType.KEY },
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedContent(targetState = authType, label = "auth_type_animation") { type ->
                if (type == AuthType.PASSWORD) {
                    GlassTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.server_edit_ssh_password),
                        icon = Icons.Filled.Lock,
                        visualTransformation = if (sshPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { sshPasswordVisible = !sshPasswordVisible }) {
                                Icon(
                                    if (sshPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    null,
                                    tint = TextTertiary
                                )
                            }
                        }
                    )
                } else {
                    GlassTextField(
                        value = privateKey,
                        onValueChange = { privateKey = it },
                        label = stringResource(R.string.server_edit_private_key),
                        icon = Icons.Filled.VpnKey,
                        singleLine = false,
                        maxLines = 6,
                        modifier = Modifier.height(140.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.server_edit_sudo), style = MaterialTheme.typography.labelSmall, color = CyanPrimary, letterSpacing = 2.sp)
            GlassTextField(
                value = sudoPassword,
                onValueChange = { sudoPassword = it },
                label = stringResource(R.string.server_edit_sudo_password),
                icon = Icons.Filled.Lock,
                visualTransformation = if (sudoPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { sudoPasswordVisible = !sudoPasswordVisible }) {
                        Icon(
                            if (sudoPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            null,
                            tint = TextTertiary
                        )
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

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
                        sudoPassword = sudoPassword
                    )
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Void)
            ) {
                Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isEditMode) stringResource(R.string.server_edit_update) else stringResource(R.string.server_edit_save), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextSecondary,
            focusedContainerColor = SurfaceContainer,
            unfocusedContainerColor = SurfaceContainer,
            focusedBorderColor = CyanPrimary,
            unfocusedBorderColor = BorderDefault,
            focusedLabelColor = CyanPrimary,
            unfocusedLabelColor = TextTertiary,
            focusedLeadingIconColor = CyanPrimary,
            unfocusedLeadingIconColor = TextTertiary,
            cursorColor = CyanPrimary
        )
    )
}

@Composable
private fun AuthTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) CyanPrimary else TextTertiary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

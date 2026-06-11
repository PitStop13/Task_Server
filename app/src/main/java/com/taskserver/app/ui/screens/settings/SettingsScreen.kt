package com.taskserver.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskserver.app.ui.components.AccentLine
import com.taskserver.app.ui.components.GlassCard
import com.taskserver.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onThemeToggle: (Boolean) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 16.dp),
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(SurfaceVariant, CircleShape)
                            .border(1.dp, BorderSubtle, CircleShape)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header
            Column {
                Text(
                    "TaskServer ⚙️",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextPrimary
                )
                Text(
                    "Pannello di Controllo",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyanDim,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(16.dp))
                AccentLine(modifier = Modifier.fillMaxWidth(0.6f), brush = GradientCyanPurple)
            }

            // ---- SEZIONE TEMA E ASPETTO ----
            SettingsSection(title = "Aspetto e Stile ✨", icon = Icons.Filled.Palette, glowColor = CyanPrimary) {
                SettingsToggleItem(
                    title = "Tema Scuro App",
                    subtitle = "Attiva la modalità Dark Mode 2026",
                    icon = Icons.Filled.DarkMode,
                    checked = uiState.isDarkMode,
                    onCheckedChange = { 
                        viewModel.toggleDarkMode(it)
                        onThemeToggle(it)
                    },
                    activeColor = CyanPrimary
                )
            }

            // ---- SICUREZZA ----
            SettingsSection(title = "Sicurezza e Privacy 🔒", icon = Icons.Filled.Shield, glowColor = PurpleSecondary) {
                SettingsToggleItem(
                    title = "Autenticazione Biometrica",
                    subtitle = "Richiedi impronta all'avvio app",
                    icon = Icons.Filled.Fingerprint,
                    checked = uiState.isBiometricEnabled,
                    onCheckedChange = { viewModel.toggleBiometric(it) },
                    activeColor = PurpleSecondary
                )
                CustomDivider()
                SettingsToggleItem(
                    title = "Crittografia SSH",
                    subtitle = "Forza scambio chiavi X25519 (Alta Sicurezza)",
                    icon = Icons.Filled.Key,
                    checked = uiState.isSshEncryptionEnabled,
                    onCheckedChange = { viewModel.toggleSshEncryption(it) },
                    activeColor = PurpleSecondary
                )
            }

            // ---- SEZIONE INFO APP ----
            SettingsSection(title = "Architettura Sistema 🧠", icon = Icons.Filled.Architecture, glowColor = TerminalGreen) {
                InfoCard(
                    title = "TaskServer Architecture",
                    description = "Motore nativo basato su coroutines e SSHJ, progettato per connessioni sicure continue.",
                    details = listOf(
                        "Kotlin Coroutines & State Flow",
                        "Material 3 Glassmorphism UI",
                        "SSHJ + BouncyCastle Crypto",
                        "Dependency Injection via Hilt"
                    )
                )
            }

            Spacer(Modifier.height(8.dp))
            
            Text(
                "Versione 1.0.0 — Build 2026\nProgettato da TaskServer Lab",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String, 
    icon: ImageVector,
    glowColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        GlassCard(glowColor = glowColor, glowAlpha = 0.2f, borderAlpha = 0.4f, cornerRadius = 24.dp) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeColor: Color,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (checked && enabled) activeColor.copy(alpha = 0.15f) else SurfaceBright, 
                    CircleShape
                )
                .border(
                    1.dp, 
                    if (checked && enabled) activeColor.copy(alpha = 0.3f) else BorderDefault, 
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, 
                null, 
                tint = if (checked && enabled) activeColor else if (enabled) TextSecondary else TextTertiary, 
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, 
                style = MaterialTheme.typography.titleMedium, 
                color = if (enabled) TextPrimary else TextTertiary, 
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle, 
                style = MaterialTheme.typography.bodySmall, 
                color = TextTertiary,
                lineHeight = 16.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = SurfaceContainer,
                disabledCheckedTrackColor = activeColor.copy(alpha = 0.3f),
                disabledUncheckedTrackColor = SurfaceContainer.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun CustomDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(1.dp)
            .background(BorderDefault)
    )
}

@Composable
private fun InfoCard(title: String, description: String, details: List<String>) {
    Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(TerminalGreen, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            description, 
            style = MaterialTheme.typography.bodyMedium, 
            color = TextSecondary, 
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(24.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBright.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            details.forEach { detail ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CheckCircle, 
                        null, 
                        tint = CyanDim, 
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        detail, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = TextPrimary, 
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

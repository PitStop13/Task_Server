package com.taskserver.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.taskserver.app.R
import com.taskserver.app.ui.navigation.AppNavigation
import com.taskserver.app.ui.theme.CyanPrimary
import com.taskserver.app.ui.theme.OnCyan
import com.taskserver.app.ui.theme.TaskServerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.taskserver.app.data.settings.SettingsManager
import com.taskserver.app.security.BiometricHelper

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var biometricHelper: BiometricHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            var isDarkMode by remember { mutableStateOf(settingsManager.isDarkMode()) }
            var authErrorMessage by remember { mutableStateOf<String?>(null) }
            var isAuthenticated by remember { 
                mutableStateOf(!settingsManager.isBiometricEnabled() || !biometricHelper.isBiometricAvailable()) 
            }
            
            if (!isAuthenticated) {
                LaunchedEffect(Unit) {
                    biometricHelper.showBiometricPrompt(
                        activity = this@MainActivity,
                        onSuccess = { isAuthenticated = true },
                        onError = { err -> authErrorMessage = err }
                    )
                }
            }

            TaskServerTheme(darkTheme = isDarkMode) {
                if (isAuthenticated) {
                    AppNavigation(
                        settingsManager = settingsManager,
                        isDarkMode = isDarkMode,
                        onThemeToggle = { 
                            isDarkMode = it
                            settingsManager.setDarkMode(it)
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.12f))
                                    .border(1.5.dp, CyanPrimary.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Fingerprint, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(42.dp), 
                                    tint = CyanPrimary
                                )
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Text(
                                text = "Vault Locked",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Text(
                                text = "Authenticate with biometric sensor to access saved SSH credentials and servers.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    biometricHelper.showBiometricPrompt(
                                        activity = this@MainActivity,
                                        onSuccess = { isAuthenticated = true },
                                        onError = { }
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanPrimary,
                                    contentColor = OnCyan
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.auth_unlock),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

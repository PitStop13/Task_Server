package com.taskserver.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.taskserver.app.R
import com.taskserver.app.ui.navigation.AppNavigation
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
            var isAuthenticated by remember { 
                mutableStateOf(!settingsManager.isBiometricEnabled()) 
            }
            
            if (!isAuthenticated) {
                LaunchedEffect(Unit) {
                    biometricHelper.showBiometricPrompt(
                        activity = this@MainActivity,
                        onSuccess = { isAuthenticated = true },
                        onError = { }
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Shield, 
                                null, 
                                modifier = Modifier.size(64.dp), 
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                stringResource(R.string.auth_required),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    biometricHelper.showBiometricPrompt(
                                        activity = this@MainActivity,
                                        onSuccess = { isAuthenticated = true },
                                        onError = { }
                                    )
                                },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                            ) {
                                Text(stringResource(R.string.auth_unlock), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

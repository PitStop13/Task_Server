package com.taskserver.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.taskserver.app.data.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val isSshEncryptionEnabled: Boolean = true,
    val isBiometricEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isDarkMode = settingsManager.isDarkMode(),
            isSshEncryptionEnabled = settingsManager.isSshEncryptionEnabled(),
            isBiometricEnabled = settingsManager.isBiometricEnabled()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        settingsManager.setDarkMode(enabled)
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun toggleSshEncryption(enabled: Boolean) {
        settingsManager.setSshEncryptionEnabled(enabled)
        _uiState.value = _uiState.value.copy(isSshEncryptionEnabled = enabled)
    }

    fun toggleBiometric(enabled: Boolean) {
        settingsManager.setBiometricEnabled(enabled)
        _uiState.value = _uiState.value.copy(isBiometricEnabled = enabled)
    }
}

package com.taskserver.app.ui.screens.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.ExecutionLog
import com.taskserver.app.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogsUiState(
    val selectedLog: ExecutionLog? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    // Correctly exposing logs as a StateFlow for reusability and bug prevention
    val logs = repository.getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    fun selectLog(log: ExecutionLog) {
        _uiState.update { it.copy(selectedLog = log) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedLog = null) }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
            _uiState.update { it.copy(successMessage = "Tutti i log eliminati") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

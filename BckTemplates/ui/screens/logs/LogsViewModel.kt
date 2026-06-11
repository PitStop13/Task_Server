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
    val logs: List<ExecutionLog> = emptyList(),
    val selectedLog: ExecutionLog? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRecentLogs().collect { list ->
                _uiState.update { it.copy(logs = list, isLoading = false) }
            }
        }
    }

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

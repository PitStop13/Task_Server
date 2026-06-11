package com.taskserver.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.*
import com.taskserver.app.data.repository.LogRepository
import com.taskserver.app.data.repository.ServerRepository
import com.taskserver.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val serverCount: Int = 0,
    val taskCount: Int = 0,
    val recentLogs: List<ExecutionLog> = emptyList(),
    val servers: List<Server> = emptyList(),
    val statusMap: Map<Long, ServerStatus> = emptyMap()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val taskRepository: TaskRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            serverRepository.getAllServers().collect { servers ->
                _uiState.update {
                    it.copy(
                        servers = servers,
                        serverCount = servers.size
                    )
                }
                // Check connectivity veloce
                servers.forEach { server ->
                    launch {
                        _uiState.update { state ->
                            state.copy(statusMap = state.statusMap + (server.id to ServerStatus.CHECKING))
                        }
                        val online = serverRepository.checkConnectivity(server)
                        _uiState.update { state ->
                            state.copy(
                                statusMap = state.statusMap + (server.id to
                                    if (online) ServerStatus.ONLINE else ServerStatus.OFFLINE)
                            )
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                _uiState.update { it.copy(taskCount = tasks.size) }
            }
        }
        viewModelScope.launch {
            logRepository.getRecentLogs().collect { logs ->
                _uiState.update { it.copy(recentLogs = logs.take(5)) }
            }
        }
    }
}

package com.taskserver.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.*
import com.taskserver.app.data.repository.FolderRepository
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
    private val logRepository: LogRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    fun createFolder(name: String, type: String = "SERVER") {
        viewModelScope.launch {
            folderRepository.createFolder(name, type)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        serverRepository.getAllServers(),
        serverRepository.statusMap,
        taskRepository.getAllTasks(),
        logRepository.getRecentLogs()
    ) { servers, statusMap, tasks, logs ->
        HomeUiState(
            serverCount = servers.size,
            taskCount = tasks.size,
            recentLogs = logs.take(5),
            servers = servers,
            statusMap = statusMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}

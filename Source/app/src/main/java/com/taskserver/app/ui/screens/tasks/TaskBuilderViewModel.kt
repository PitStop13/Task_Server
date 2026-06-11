package com.taskserver.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.Task
import com.taskserver.app.data.model.TaskCommand
import com.taskserver.app.data.repository.ServerRepository
import com.taskserver.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskBuilderUiState(
    val servers: List<Server> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class TaskBuilderViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TaskBuilderUiState())
    val state: StateFlow<TaskBuilderUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            serverRepository.getAllServers().collect { servers ->
                _state.update { it.copy(servers = servers) }
            }
        }
    }

    suspend fun getTask(id: Long): Task? {
        return taskRepository.getTaskById(id)
    }

    fun deserializeCommands(json: String): List<TaskCommand> {
        return taskRepository.deserializeCommands(json)
    }

    fun save(
        taskId: Long,
        name: String,
        description: String,
        iconName: String,
        commands: List<TaskCommand>,
        pinnedServerId: Long?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val commandsJson = taskRepository.serializeCommands(commands)
                val task = Task(
                    id = if (taskId > 0) taskId else 0,
                    name = name,
                    description = description,
                    iconName = iconName,
                    commandsJson = commandsJson,
                    pinnedServerId = pinnedServerId
                )

                if (taskId > 0) {
                    taskRepository.updateTask(task)
                } else {
                    taskRepository.saveTask(task)
                }
                _state.update { it.copy(isLoading = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Errore durante il salvataggio") }
            }
        }
    }
}

package com.taskserver.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.Folder
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.Task
import com.taskserver.app.data.model.TaskCommand
import com.taskserver.app.data.repository.FolderRepository
import com.taskserver.app.data.repository.ServerRepository
import com.taskserver.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskBuilderUiState(
    val servers: List<Server> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class TaskBuilderViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val serverRepository: ServerRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TaskBuilderUiState())
    val state: StateFlow<TaskBuilderUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                serverRepository.getAllServers(),
                folderRepository.getTaskFolders()
            ) { servers, folders ->
                servers to folders
            }.collect { (servers, folders) ->
                _state.update { it.copy(servers = servers, folders = folders) }
            }
        }
    }

    suspend fun getTask(id: Long): Task? {
        return taskRepository.getTaskById(id)
    }

    fun deserializeCommands(json: String): List<TaskCommand> {
        return taskRepository.deserializeCommands(json)
    }

    suspend fun createFolder(name: String): Long {
        return folderRepository.createFolder(name, "TASK")
    }

    fun save(
        taskId: Long,
        name: String,
        description: String,
        iconName: String,
        commands: List<TaskCommand>,
        pinnedServerId: Long?,
        folder: String? = null
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
                    pinnedServerId = pinnedServerId,
                    folder = folder?.takeIf { it.isNotBlank() }
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

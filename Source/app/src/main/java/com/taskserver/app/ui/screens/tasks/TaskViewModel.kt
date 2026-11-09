package com.taskserver.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.Folder
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.Task
import com.taskserver.app.data.repository.FolderRepository
import com.taskserver.app.data.repository.ServerRepository
import com.taskserver.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val serverRepository: ServerRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    val tasks = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val servers = serverRepository.getAllServers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders = folderRepository.getTaskFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    fun getCommandCount(commandsJson: String): Int {
        return repository.deserializeCommands(commandsJson).size
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val id = folderRepository.createFolder(name, "TASK")
            if (id > 0) {
                _uiState.update { it.copy(successMessage = "Cartella \"$name\" creata") }
            }
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            folderRepository.deleteTaskFolder(folder)
            _uiState.update { it.copy(successMessage = "Cartella \"${folder.name}\" eliminata") }
        }
    }

    fun renameFolder(folder: Folder, newName: String) {
        viewModelScope.launch {
            folderRepository.renameTaskFolder(folder, newName)
            _uiState.update { it.copy(successMessage = "Cartella rinominata in \"$newName\"") }
        }
    }

    fun updateTaskFolder(taskId: Long, folderName: String?) {
        viewModelScope.launch {
            repository.updateTaskFolder(taskId, folderName)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                repository.deleteTask(task)
                _uiState.update { it.copy(successMessage = "Task \"${task.name}\" deleted") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

package com.taskserver.app.ui.screens.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.AuthType
import com.taskserver.app.data.model.Folder
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.ServerCredentials
import com.taskserver.app.data.model.ServerStatus
import com.taskserver.app.data.repository.FolderRepository
import com.taskserver.app.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServerUiState(
    val servers: List<Server> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val statusMap: Map<Long, ServerStatus> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val repository: ServerRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<Pair<String?, String?>>(null to null)

    val uiState: StateFlow<ServerUiState> = combine(
        repository.getAllServers(),
        folderRepository.getServerFolders(),
        repository.statusMap,
        _messages
    ) { servers, folders, statusMap, (error, successMsg) ->
        ServerUiState(
            servers = servers,
            folders = folders,
            statusMap = statusMap,
            error = error,
            successMessage = successMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServerUiState()
    )

    fun checkAllConnectivity() {
        repository.refreshAllServerStatuses()
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val id = folderRepository.createFolder(name, "SERVER")
            if (id > 0) {
                _messages.update { null to "Cartella \"$name\" creata" }
            }
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            folderRepository.deleteServerFolder(folder)
            _messages.update { null to "Cartella \"${folder.name}\" eliminata" }
        }
    }

    fun renameFolder(folder: Folder, newName: String) {
        viewModelScope.launch {
            folderRepository.renameServerFolder(folder, newName)
            _messages.update { null to "Cartella rinominata in \"$newName\"" }
        }
    }

    fun updateServerFolder(serverId: Long, folderName: String?) {
        viewModelScope.launch {
            repository.updateServerFolder(serverId, folderName)
        }
    }

    fun deleteServer(server: Server) {
        viewModelScope.launch {
            repository.deleteServer(server)
            _messages.update { null to "Server \"${server.name}\" deleted" }
        }
    }

    fun clearMessages() {
        _messages.update { null to null }
    }

    // Credenziali per la schermata di edit
    fun getCredential(server: Server): ServerCredentials =
        repository.getCredential(server)

    suspend fun getServer(id: Long): Server? = repository.getServerById(id)
    suspend fun getServerById(id: Long): Server? = repository.getServerById(id)

    fun saveServer(
        id: Long,
        name: String,
        host: String,
        port: Int,
        username: String,
        authType: AuthType,
        password: String?,
        privateKey: String?,
        sudoPassword: String?,
        folder: String? = null
    ) {
        viewModelScope.launch {
            val server = Server(
                id = if (id > 0) id else 0,
                name = name,
                host = host,
                port = port,
                username = username,
                authType = authType,
                folder = folder?.takeIf { it.isNotBlank() }
            )
            if (id > 0) {
                repository.updateServer(server, password, privateKey, sudoPassword)
            } else {
                repository.saveServer(server, password, privateKey, sudoPassword)
            }
        }
    }
}

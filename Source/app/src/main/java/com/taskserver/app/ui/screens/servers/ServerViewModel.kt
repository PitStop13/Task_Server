package com.taskserver.app.ui.screens.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.AuthType
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.ServerCredentials
import com.taskserver.app.data.model.ServerStatus
import com.taskserver.app.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServerUiState(
    val servers: List<Server> = emptyList(),
    val statusMap: Map<Long, ServerStatus> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val repository: ServerRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<Pair<String?, String?>>(null to null)

    val uiState: StateFlow<ServerUiState> = combine(
        repository.getAllServers(),
        repository.statusMap,
        _messages
    ) { servers, statusMap, (error, successMsg) ->
        ServerUiState(
            servers = servers,
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

    fun saveServer(
        id: Long,
        name: String,
        host: String,
        port: Int,
        username: String,
        authType: AuthType,
        password: String?,
        privateKey: String?,
        sudoPassword: String?
    ) {
        viewModelScope.launch {
            val server = Server(
                id = if (id > 0) id else 0,
                name = name,
                host = host,
                port = port,
                username = username,
                authType = authType
            )
            if (id > 0) {
                repository.updateServer(server, password, privateKey, sudoPassword)
            } else {
                repository.saveServer(server, password, privateKey, sudoPassword)
            }
        }
    }
}

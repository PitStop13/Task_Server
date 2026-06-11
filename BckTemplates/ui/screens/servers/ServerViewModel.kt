package com.taskserver.app.ui.screens.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.AuthType
import com.taskserver.app.data.model.ServerCredentials
import com.taskserver.app.data.model.Server
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

    private val _uiState = MutableStateFlow(ServerUiState())
    val uiState: StateFlow<ServerUiState> = _uiState.asStateFlow()

    init {
        observeServers()
    }

    private fun observeServers() {
        viewModelScope.launch {
            repository.getAllServers().collect { list ->
                _uiState.update { it.copy(servers = list) }
                checkAllConnectivity(list)
            }
        }
    }

    fun checkAllConnectivity(servers: List<Server> = _uiState.value.servers) {
        servers.forEach { server ->
            viewModelScope.launch {
                _uiState.update { state ->
                    state.copy(statusMap = state.statusMap + (server.id to ServerStatus.CHECKING))
                }
                val online = repository.checkConnectivity(server)
                _uiState.update { state ->
                    state.copy(
                        statusMap = state.statusMap + (server.id to
                            if (online) ServerStatus.ONLINE else ServerStatus.OFFLINE)
                    )
                }
            }
        }
    }

    fun deleteServer(server: Server) {
        viewModelScope.launch {
            repository.deleteServer(server)
            _uiState.update { it.copy(successMessage = "Server \"${server.name}\" eliminato") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
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

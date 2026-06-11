package com.taskserver.app.ui.screens.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskserver.app.data.model.*
import com.taskserver.app.data.repository.LogRepository
import com.taskserver.app.data.repository.ServerRepository
import com.taskserver.app.data.repository.TaskRepository
import com.taskserver.app.data.ssh.InteractiveSshSession
import com.taskserver.app.data.ssh.SshManager
import com.taskserver.app.data.ssh.SshOutput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TerminalUiState(
    val taskName: String = "",
    val serverName: String = "",
    val serverUsername: String = "",
    val serverHost: String = "",
    val lines: List<String> = emptyList(),
    val isRunning: Boolean = false,
    val exitCode: Int? = null,
    val error: String? = null,
    val logId: Long? = null,
    val isInteractive: Boolean = false
)

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val serverRepository: ServerRepository,
    private val logRepository: LogRepository,
    private val sshManager: SshManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private var sshJob: Job? = null
    private var interactiveSession: InteractiveSshSession? = null

    fun execute(taskId: Long, serverId: Long) {
        if (_uiState.value.isRunning) return

        if (taskId == -1L) {
            startInteractiveSession(serverId)
            return
        }

        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            val server = serverRepository.getServerById(serverId)

            if (task == null || server == null) {
                _uiState.update { it.copy(error = "Task o server non trovato") }
                return@launch
            }

            val commands = taskRepository.deserializeCommands(task.commandsJson)
            if (commands.isEmpty()) {
                _uiState.update { it.copy(error = "Nessun comando da eseguire") }
                return@launch
            }

            val credentials = serverRepository.getCredential(server)

            _uiState.update {
                it.copy(
                    taskName = task.name,
                    serverName = server.name,
                    serverUsername = server.username,
                    serverHost = server.host,
                    lines = listOf("$ Connessione a ${server.host}:${server.port}..."),
                    isRunning = true,
                    exitCode = null,
                    error = null
                )
            }

            // Crea log
            val log = ExecutionLog(
                taskId = task.id,
                taskName = task.name,
                serverId = server.id,
                serverName = server.name,
                serverHost = server.host,
                status = LogStatus.RUNNING
            )
            val logId = logRepository.startLog(log)
            _uiState.update { it.copy(logId = logId) }

            val outputBuffer = StringBuilder()

            sshJob = launch {
                sshManager.executeCommands(
                    host = server.host,
                    port = server.port,
                    username = server.username,
                    password = credentials.sshPassword,
                    privateKeyContent = credentials.privateKey,
                    commands = commands.map { it.text },
                    sudoPassword = credentials.sudoPassword
                ).collect { output ->
                    when (output) {
                        is SshOutput.Started -> {
                            _uiState.update { state ->
                                state.copy(
                                    lines = state.lines + "$ Connesso. Esecuzione comandi..."
                                )
                            }
                        }
                        is SshOutput.Data -> {
                            outputBuffer.append(output.text)
                            val newLine = output.text.trimEnd('\n')
                            _uiState.update { state ->
                                state.copy(lines = state.lines + newLine)
                            }
                        }
                        is SshOutput.Error -> {
                            outputBuffer.append("ERRORE: ${output.message}\n")
                            _uiState.update { state ->
                                state.copy(
                                    lines = state.lines + "❌ ERRORE: ${output.message}",
                                    isRunning = false,
                                    error = output.message
                                )
                            }
                            logRepository.completeLog(
                                logId = logId,
                                output = outputBuffer.toString(),
                                exitCode = -1,
                                status = LogStatus.ERROR
                            )
                        }
                        is SshOutput.Complete -> {
                            val status = if (output.exitCode == 0) LogStatus.SUCCESS else LogStatus.ERROR
                            _uiState.update { state ->
                                state.copy(
                                    lines = state.lines +
                                        "" +
                                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" +
                                        "Completato con exit code: ${output.exitCode}",
                                    isRunning = false,
                                    exitCode = output.exitCode
                                )
                            }
                            logRepository.completeLog(
                                logId = logId,
                                output = outputBuffer.toString(),
                                exitCode = output.exitCode,
                                status = status
                            )
                        }
                        is SshOutput.Cancelled -> {
                            _uiState.update { state ->
                                state.copy(
                                    lines = state.lines + "⚠️ Esecuzione annullata",
                                    isRunning = false
                                )
                            }
                            logRepository.completeLog(
                                logId = logId,
                                output = outputBuffer.toString(),
                                exitCode = -1,
                                status = LogStatus.ERROR
                            )
                        }
                    }
                }
            }
        }
    }

    fun cancelExecution() {
        sshJob?.cancel()
        sshJob = null
        try { interactiveSession?.close() } catch (e: Exception) {}
        interactiveSession = null
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun startInteractiveSession(serverId: Long) {
        viewModelScope.launch {
            val server = serverRepository.getServerById(serverId) ?: return@launch
            val credentials = serverRepository.getCredential(server)

            _uiState.update {
                it.copy(
                    taskName = "SSH Manuale",
                    serverName = server.name,
                    serverUsername = server.username,
                    serverHost = server.host,
                    lines = listOf("$ Connessione a ${server.host}:${server.port}..."),
                    isRunning = true,
                    isInteractive = true,
                    exitCode = null,
                    error = null
                )
            }

            try {
                interactiveSession = sshManager.startInteractiveSession(
                    server.host,
                    server.port,
                    server.username,
                    credentials.sshPassword,
                    credentials.privateKey,
                    credentials.sudoPassword
                )

                _uiState.update { state ->
                    state.copy(lines = state.lines + "Connesso. Avvio shell interattiva...")
                }

                sshJob = launch {
                    interactiveSession?.outputFlow?.collect { chunk ->
                        handleInteractiveChunk(chunk)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isRunning = false) }
            }
        }
    }

    private fun handleInteractiveChunk(chunk: String) {
        val ansiRegex = Regex("\u001B\\[[0-?]*[ -/]*[@-~]")
        val cleanChunk = chunk.replace(ansiRegex, "").replace("\r", "")

        if (cleanChunk.isEmpty() && chunk.isNotEmpty()) {
            return
        }

        _uiState.update { state ->
            val updatedLines = state.lines + cleanChunk
            val trimmedLines = if (updatedLines.size > 500) updatedLines.takeLast(500) else updatedLines
            state.copy(lines = trimmedLines)
        }
    }

    fun sendInteractiveCommand(command: String) {
        viewModelScope.launch {
            val sanitizedCommand = command.trim()
            if (sanitizedCommand.isBlank()) return@launch

            val session = interactiveSession
            if (session == null) {
                _uiState.update { state ->
                    state.copy(lines = state.lines + "❌ Sessione interattiva non disponibile")
                }
                return@launch
            }

            val prompt = buildInteractivePrompt(_uiState.value)
            _uiState.update { state ->
                val updatedLines = state.lines + "$prompt $sanitizedCommand"
                val trimmedLines = if (updatedLines.size > 500) updatedLines.takeLast(500) else updatedLines
                state.copy(lines = trimmedLines)
            }

            try {
                session.sendCommand(sanitizedCommand)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(lines = state.lines + "❌ ${e.localizedMessage ?: "Errore durante l'invio del comando"}")
                }
            }
        }
    }

    private fun buildInteractivePrompt(state: TerminalUiState): String {
        val username = state.serverUsername.ifBlank { "ssh" }
        val host = state.serverHost.ifBlank { "server" }
        return "$username@$host:~$"
    }

    override fun onCleared() {
        super.onCleared()
        cancelExecution()
    }
}

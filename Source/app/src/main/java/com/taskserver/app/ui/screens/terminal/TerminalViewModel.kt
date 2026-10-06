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
                _uiState.update { it.copy(error = "Task or server not found") }
                return@launch
            }

            val commands = taskRepository.deserializeCommands(task.commandsJson)
            if (commands.isEmpty()) {
                _uiState.update { it.copy(error = "No commands to execute") }
                return@launch
            }

            val credentials = serverRepository.getCredential(server)

            _uiState.update {
                it.copy(
                    taskName = task.name,
                    serverName = server.name,
                    serverUsername = server.username,
                    serverHost = server.host,
                    lines = listOf("$ Connecting to ${server.host}:${server.port}..."),
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
                                    lines = state.lines + "$ Connected. Executing commands..."
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
                            outputBuffer.append("ERROR: ${output.message}\n")
                            _uiState.update { state ->
                                state.copy(
                                    lines = state.lines + "❌ ERROR: ${output.message}",
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
                                        "Completed with exit code: ${output.exitCode}",
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
                                    lines = state.lines + "⚠️ Execution cancelled",
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

    private val _rawOutputFlow = MutableSharedFlow<ByteArray>(replay = 50, extraBufferCapacity = 256)
    val rawOutputFlow = _rawOutputFlow.asSharedFlow()

    private var isTerminalReady = false

    private fun startInteractiveSession(serverId: Long) {
        viewModelScope.launch {
            val server = serverRepository.getServerById(serverId) ?: return@launch
            val credentials = serverRepository.getCredential(server)

            _uiState.update {
                it.copy(
                    taskName = "Manual SSH",
                    serverName = server.name,
                    serverUsername = server.username,
                    serverHost = server.host,
                    lines = listOf(),
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

                sshJob = launch {
                    interactiveSession?.outputFlow?.collect { chunk ->
                        if (isTerminalReady) {
                             _rawOutputFlow.emit(chunk)
                        } else {
                             // Buffer internally while JS is loading so we don't drop the motd
                             _rawOutputFlow.tryEmit(chunk)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isRunning = false) }
            }
        }
    }

    fun setTerminalReady() {
        isTerminalReady = true
    }

    fun resizeTerminal(cols: Int, rows: Int) {
        viewModelScope.launch {
            interactiveSession?.resize(cols, rows)
        }
    }

    fun sendRawInteractiveBytes(bytes: ByteArray) {
        viewModelScope.launch {
            try {
                interactiveSession?.sendBytes(bytes)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = "Error sending data: ${e.localizedMessage}")
                }
            }
        }
    }

    fun sendInteractiveCommand(command: String) {
        viewModelScope.launch {
            try {
                interactiveSession?.sendCommand(command)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = "Error sending command: ${e.localizedMessage}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelExecution()
    }
}

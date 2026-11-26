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
import kotlinx.coroutines.CancellationException
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
    val isConnected: Boolean = false,
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
            val effectiveSudoPassword = credentials.sudoPassword?.takeIf { it.isNotBlank() } ?: credentials.sshPassword

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
                    sudoPassword = effectiveSudoPassword
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
                            _uiState.update { state ->
                                state.copy(lines = appendTerminalLines(state.lines, output.text))
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
        try { interactiveSession?.close() } catch (_: Exception) {}
        interactiveSession = null
        _uiState.update { it.copy(isRunning = false, isConnected = false) }
    }

    private val _rawOutputFlow = MutableSharedFlow<ByteArray>(replay = 50, extraBufferCapacity = 256)
    val rawOutputFlow = _rawOutputFlow.asSharedFlow()

    @Volatile
    private var isTerminalReady = false

    @Volatile
    private var pendingCols = 80

    @Volatile
    private var pendingRows = 24

    fun reconnectInteractive(serverId: Long) {
        cancelExecution()
        startInteractiveSession(serverId)
    }

    private fun startInteractiveSession(serverId: Long) {
        sshJob?.cancel()
        sshJob = null
        try { interactiveSession?.close() } catch (_: Exception) {}
        interactiveSession = null

        viewModelScope.launch {
            val server = serverRepository.getServerById(serverId) ?: run {
                _uiState.update { it.copy(error = "Server non trovato", isRunning = false, isConnected = false, isInteractive = true) }
                return@launch
            }
            val credentials = serverRepository.getCredential(server)
            val effectiveSudoPassword = credentials.sudoPassword?.takeIf { it.isNotBlank() } ?: credentials.sshPassword

            _uiState.update {
                it.copy(
                    taskName = "Manual SSH",
                    serverName = server.name,
                    serverUsername = server.username,
                    serverHost = server.host,
                    lines = listOf(),
                    isRunning = true,
                    isConnected = false,
                    isInteractive = true,
                    exitCode = null,
                    error = null
                )
            }

            try {
                val session = sshManager.startInteractiveSession(
                    server.host,
                    server.port,
                    server.username,
                    credentials.sshPassword,
                    credentials.privateKey,
                    sudoPassword = effectiveSudoPassword,
                    initialCols = pendingCols,
                    initialRows = pendingRows
                )
                interactiveSession = session
                _uiState.update { it.copy(isConnected = true, isRunning = true, error = null) }
                session.resize(pendingCols, pendingRows)

                sshJob = launch {
                    try {
                        session.outputFlow.collect { chunk ->
                            if (isTerminalReady) {
                                _rawOutputFlow.emit(chunk)
                            } else {
                                // Buffer internally while JS is loading so we don't drop the motd
                                _rawOutputFlow.tryEmit(chunk)
                            }
                        }
                        // EOF: il server ha chiuso la shell o la connessione è terminata
                        if (interactiveSession === session) {
                            try { session.close() } catch (_: Exception) {}
                            interactiveSession = null
                            _uiState.update {
                                it.copy(
                                    isRunning = false,
                                    isConnected = false,
                                    error = "Sessione SSH terminata dal server"
                                )
                            }
                            emitTerminalNotice("\r\n\u001B[33m[Sessione SSH terminata — premi Riconnetti per continuare]\u001B[0m\r\n")
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (interactiveSession === session) {
                            try { session.close() } catch (_: Exception) {}
                            interactiveSession = null
                            val msg = e.localizedMessage ?: "Connessione SSH interrotta"
                            _uiState.update {
                                it.copy(
                                    isRunning = false,
                                    isConnected = false,
                                    error = msg
                                )
                            }
                            emitTerminalNotice("\r\n\u001B[31m[Errore SSH: $msg]\u001B[0m\r\n")
                        }
                    }
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Impossibile connettersi via SSH"
                _uiState.update { it.copy(error = msg, isRunning = false, isConnected = false) }
                emitTerminalNotice("\r\n\u001B[31m[Connessione fallita: $msg]\u001B[0m\r\n")
            }
        }
    }

    private suspend fun emitTerminalNotice(text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        if (isTerminalReady) {
            _rawOutputFlow.emit(bytes)
        } else {
            _rawOutputFlow.tryEmit(bytes)
        }
    }

    fun setTerminalReady() {
        isTerminalReady = true
    }

    fun resizeTerminal(cols: Int, rows: Int) {
        if (cols > 0 && rows > 0) {
            pendingCols = cols
            pendingRows = rows
        }
        viewModelScope.launch {
            interactiveSession?.resize(cols, rows)
        }
    }

    fun sendRawInteractiveBytes(bytes: ByteArray) {
        viewModelScope.launch {
            val session = interactiveSession
            if (session == null || !session.isOpen()) {
                val msg = if (_uiState.value.isRunning && !_uiState.value.isConnected) {
                    "Connessione SSH in corso..."
                } else {
                    "Sessione SSH disconnessa. Premi Riconnetti."
                }
                _uiState.update { state ->
                    state.copy(
                        isRunning = state.isRunning && !state.isConnected,
                        isConnected = false,
                        error = msg
                    )
                }
                return@launch
            }
            try {
                session.sendBytes(bytes)
                if (_uiState.value.error != null) {
                    _uiState.update { it.copy(error = null) }
                }
            } catch (e: Exception) {
                val stillOpen = session.isOpen()
                if (!stillOpen) {
                    interactiveSession = null
                }
                _uiState.update { state ->
                    state.copy(
                        isRunning = stillOpen,
                        isConnected = stillOpen,
                        error = "Errore invio dati: ${e.localizedMessage ?: "connessione chiusa"}"
                    )
                }
            }
        }
    }

    fun sendInteractiveCommand(command: String, onSent: () -> Unit = {}) {
        viewModelScope.launch {
            val session = interactiveSession
            if (session == null || !session.isOpen()) {
                val msg = if (_uiState.value.isRunning && !_uiState.value.isConnected) {
                    "Connessione SSH in corso..."
                } else {
                    "Sessione SSH disconnessa. Premi Riconnetti."
                }
                _uiState.update { state ->
                    state.copy(
                        isRunning = state.isRunning && !state.isConnected,
                        isConnected = false,
                        error = msg
                    )
                }
                return@launch
            }
            try {
                session.sendCommand(command)
                if (_uiState.value.error != null) {
                    _uiState.update { it.copy(error = null) }
                }
                onSent()
            } catch (e: Exception) {
                val stillOpen = session.isOpen()
                if (!stillOpen) {
                    interactiveSession = null
                }
                _uiState.update { state ->
                    state.copy(
                        isRunning = stillOpen,
                        isConnected = stillOpen,
                        error = "Errore invio comando: ${e.localizedMessage ?: "connessione chiusa"}"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelExecution()
    }

    private fun appendTerminalLines(currentLines: List<String>, chunk: String): List<String> {
        if (chunk.isEmpty()) return currentLines
        val clean = chunk.replace("\r\n", "\n").replace("\r", "")
        val parts = clean.split("\n")
        val result = currentLines.toMutableList()

        if (result.isEmpty()) {
            result.addAll(parts.filter { it.isNotEmpty() })
            return result
        }

        val lastIdx = result.size - 1
        result[lastIdx] = result[lastIdx] + parts[0]
        for (i in 1 until parts.size) {
            if (i == parts.size - 1 && parts[i].isEmpty()) {
                continue
            }
            result.add(parts[i])
        }
        return result
    }
}

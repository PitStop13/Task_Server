package com.taskserver.app.data.ssh

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.coroutineContext

private val ANSI_REGEX = Regex("\u001B\\[[0-?]*[ -/]*[@-~]")

interface InteractiveSshSession : AutoCloseable {
    val outputFlow: Flow<String>
    suspend fun sendCommand(command: String)
}

class InteractiveSshSessionImpl(
    private val sshClient: SSHClient,
    private val session: Session,
    private val shell: Session.Shell,
    private val commandTransformer: (String) -> String = { it }
) : InteractiveSshSession {

    private val outputStream: OutputStream = shell.outputStream
    private val inputStream: InputStream = shell.inputStream
    private val pendingEchoes = ConcurrentLinkedQueue<String>()

    override val outputFlow: Flow<String> = flow {
        val buffer = ByteArray(4096)
        val textBuffer = StringBuilder()

        suspend fun emitLine(rawLine: String) {
            if (rawLine.isEmpty()) {
                emit("")
                return
            }

            val cleaned = sanitizeTerminalText(rawLine)
            if (cleaned.isEmpty()) return

            val expectedEcho = pendingEchoes.peek()
            if (expectedEcho != null && normalizeTerminalText(cleaned) == normalizeTerminalText(expectedEcho)) {
                pendingEchoes.poll()
                return
            }

            emit(cleaned)
        }

        suspend fun drainCompletedLines() {
            var newlineIndex = textBuffer.indexOf("\n")
            while (newlineIndex >= 0) {
                val rawLine = textBuffer.substring(0, newlineIndex)
                textBuffer.delete(0, newlineIndex + 1)
                emitLine(rawLine)
                newlineIndex = textBuffer.indexOf("\n")
            }
        }

        suspend fun flushPromptIfVisible() {
            val trailing = textBuffer.toString()
            if (trailing.isBlank()) return

            val cleaned = sanitizeTerminalText(trailing)
            if (looksLikePrompt(cleaned)) {
                textBuffer.clear()
                emitLine(cleaned)
            }
        }

        while (coroutineContext.isActive) {
            val bytesRead = withContext(Dispatchers.IO) {
                try {
                    inputStream.read(buffer)
                } catch (_: Exception) {
                    -1
                }
            }

            if (bytesRead == -1) break
            if (bytesRead <= 0) continue

            textBuffer.append(String(buffer, 0, bytesRead, Charsets.UTF_8))
            drainCompletedLines()
            flushPromptIfVisible()
        }

        if (textBuffer.isNotEmpty()) {
            val remaining = sanitizeTerminalText(textBuffer.toString())
            textBuffer.clear()
            if (remaining.isNotEmpty()) {
                emitLine(remaining)
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun sendCommand(command: String) {
        withContext(Dispatchers.IO) {
            val trimmed = command.trim()
            if (trimmed.isBlank()) return@withContext

            val transformed = commandTransformer(trimmed)
            pendingEchoes.add(transformed)

            val cmdFull = if (transformed.endsWith("\n")) transformed else "$transformed\n"
            outputStream.write(cmdFull.toByteArray(Charsets.UTF_8))
            outputStream.flush()
        }
    }

    private fun sanitizeTerminalText(text: String): String =
        text.replace(ANSI_REGEX, "").replace("\r", "")

    private fun normalizeTerminalText(text: String): String =
        sanitizeTerminalText(text).trim()

    private fun looksLikePrompt(text: String): Boolean {
        val candidate = normalizeTerminalText(text)
        return candidate.isNotEmpty() && candidate.contains("@") && (candidate.endsWith("$") || candidate.endsWith("#"))
    }

    override fun close() {
        try { shell.close() } catch (_: Exception) {}
        try { session.close() } catch (_: Exception) {}
        try { sshClient.disconnect() } catch (_: Exception) {}
    }
}

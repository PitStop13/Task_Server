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
import kotlin.coroutines.coroutineContext

interface InteractiveSshSession : AutoCloseable {
    val outputFlow: Flow<ByteArray>
    suspend fun sendCommand(command: String)
    suspend fun sendBytes(bytes: ByteArray)
    suspend fun resize(cols: Int, rows: Int)
}

class InteractiveSshSessionImpl(
    private val sshClient: SSHClient,
    private val session: Session,
    private val shell: Session.Shell,
    private val commandTransformer: (String) -> String = { it }
) : InteractiveSshSession {

    private val outputStream: OutputStream = shell.outputStream
    private val inputStream: InputStream = shell.inputStream

    override val outputFlow: Flow<ByteArray> = flow {
        val buffer = ByteArray(4096)

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

            val chunk = buffer.copyOfRange(0, bytesRead)
            emit(chunk)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun sendCommand(command: String) {
        withContext(Dispatchers.IO) {
            val transformed = commandTransformer(command)
            val cmdFull = if (transformed.endsWith("\r") || transformed.endsWith("\n")) transformed else "$transformed\r"
            outputStream.write(cmdFull.toByteArray(Charsets.UTF_8))
            outputStream.flush()
        }
    }

    override suspend fun sendBytes(bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            outputStream.write(bytes)
            outputStream.flush()
        }
    }

    override suspend fun resize(cols: Int, rows: Int) {
        withContext(Dispatchers.IO) {
            try {
                shell.changeWindowDimensions(cols, rows, 0, 0)
            } catch (_: Exception) {}
        }
    }

    override fun close() {
        try { shell.close() } catch (_: Exception) {}
        try { session.close() } catch (_: Exception) {}
        try { sshClient.disconnect() } catch (_: Exception) {}
    }
}

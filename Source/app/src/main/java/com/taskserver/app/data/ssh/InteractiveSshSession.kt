package com.taskserver.app.data.ssh

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

interface InteractiveSshSession : AutoCloseable {
    val outputFlow: Flow<ByteArray>
    fun isOpen(): Boolean
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
    private val errorStream: InputStream = shell.errorStream
    private val closed = AtomicBoolean(false)
    private val writeMutex = Mutex()

    override fun isOpen(): Boolean =
        !closed.get() && sshClient.isConnected && session.isOpen && shell.isOpen

    override val outputFlow: Flow<ByteArray> = channelFlow {
        val errorDrain = launch(Dispatchers.IO) {
            val errBuf = ByteArray(2048)
            while (isActive && !closed.get()) {
                val read = try {
                    errorStream.read(errBuf)
                } catch (_: Exception) {
                    -1
                }
                if (read == -1) break
                if (read > 0) {
                    send(errBuf.copyOfRange(0, read))
                }
            }
        }

        val buffer = ByteArray(4096)
        try {
            while (isActive && !closed.get()) {
                val bytesRead = try {
                    inputStream.read(buffer)
                } catch (e: Exception) {
                    if (closed.get()) {
                        -1
                    } else {
                        throw e
                    }
                }

                if (bytesRead == -1) break
                if (bytesRead <= 0) continue

                send(buffer.copyOfRange(0, bytesRead))
            }
        } finally {
            errorDrain.cancel()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun sendCommand(command: String) {
        withContext(Dispatchers.IO) {
            writeMutex.withLock {
                if (!isOpen()) {
                    throw IllegalStateException("Sessione SSH chiusa o disconnessa")
                }
                val transformed = commandTransformer(command)
                val clean = transformed.trimEnd('\r', '\n')
                val cmdFull = "$clean\r"
                outputStream.write(cmdFull.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
        }
    }

    override suspend fun sendBytes(bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            writeMutex.withLock {
                if (!isOpen()) {
                    throw IllegalStateException("Sessione SSH chiusa o disconnessa")
                }
                outputStream.write(bytes)
                outputStream.flush()
            }
        }
    }

    override suspend fun resize(cols: Int, rows: Int) {
        withContext(Dispatchers.IO) {
            if (!isOpen() || cols <= 0 || rows <= 0) return@withContext
            try {
                shell.changeWindowDimensions(cols, rows, 0, 0)
            } catch (_: Exception) {}
        }
    }

    override fun close() {
        closed.set(true)
        try { shell.close() } catch (_: Exception) {}
        try { session.close() } catch (_: Exception) {}
        try { sshClient.disconnect() } catch (_: Exception) {}
    }
}

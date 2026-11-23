package com.taskserver.app.data.ssh

import com.taskserver.app.data.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.net.InetSocketAddress
import java.net.Socket
import java.security.Security
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SshManager @Inject constructor(
    private val settingsManager: SettingsManager
) {

    init {
        // Registra BouncyCastle in prima posizione per sovrascrivere il provider "BC" limitato di Android.
        // Questo è necessario per algoritmi come X25519.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    private fun createSshClient(): SSHClient {
        val config = DefaultConfig()
        if (settingsManager.isSshEncryptionEnabled()) {
            config.keyExchangeFactories = listOf(
                net.schmizz.sshj.transport.kex.Curve25519SHA256.Factory(),
                net.schmizz.sshj.transport.kex.Curve25519SHA256.FactoryLibSsh()
            )
        }
        return SSHClient(config)
    }

    fun executeCommands(
        host: String,
        port: Int,
        username: String,
        password: String?,
        privateKeyContent: String?,
        commands: List<String>,
        sudoPassword: String? = null
    ): Flow<SshOutput> = flow {
        val ssh = createSshClient()
        try {
            ssh.addHostKeyVerifier(PromiscuousVerifier())
            ssh.connect(host, port)
            
            // Timeout per evitare che il thread rimanga appeso
            ssh.timeout = 15000 
            
            if (!privateKeyContent.isNullOrBlank()) {
                val keyProvider: KeyProvider = ssh.loadKeys(privateKeyContent, null, null)
                ssh.authPublickey(username, keyProvider)
            } else if (!password.isNullOrBlank()) {
                ssh.authPassword(username, password)
            } else {
                throw IllegalArgumentException("Nessuna credenziale fornita")
            }

            emit(SshOutput.Started)

            val effectiveSudo = sudoPassword?.takeIf { it.isNotBlank() } ?: password?.takeIf { it.isNotBlank() }

            val validCommands = commands
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.startsWith("#") }

            if (validCommands.isEmpty()) {
                emit(SshOutput.Complete(0))
                return@flow
            }

            val scriptLines = mutableListOf<String>()
            scriptLines.add("set -e")

            // Se disponibile una password sudo, pre-autentica sudo se presente
            if (!effectiveSudo.isNullOrBlank()) {
                scriptLines.add("printf '%s\\n' ${shellQuote(effectiveSudo)} | sudo -S -p '' -v 2>/dev/null || true")
            }

            validCommands.forEach { cmd ->
                scriptLines.add(transformCommandForSudo(cmd, effectiveSudo, requirePassword = false))
            }

            val fullCommand = scriptLines.joinToString("\n")

            ssh.startSession().use { session ->
                session.allocateDefaultPTY()
                val cmd: Session.Command = session.exec(fullCommand)
                try {
                    cmd.outputStream.close()
                } catch (_: Exception) {}

                val buffer = ByteArray(4096)
                while (currentCoroutineContext().isActive) {
                    val read = withContext(Dispatchers.IO) {
                        try {
                            cmd.inputStream.read(buffer)
                        } catch (_: Exception) {
                            -1
                        }
                    }
                    if (read <= 0) break
                    val chunk = String(buffer, 0, read, Charsets.UTF_8)
                    emit(SshOutput.Data(chunk))
                }

                // Check for any remaining stderr data
                withContext(Dispatchers.IO) {
                    try {
                        val errAvail = cmd.errorStream.available()
                        if (errAvail > 0) {
                            val errBuf = ByteArray(errAvail)
                            val errRead = cmd.errorStream.read(errBuf)
                            if (errRead > 0) {
                                emit(SshOutput.Data(String(errBuf, 0, errRead, Charsets.UTF_8)))
                            }
                        }
                    } catch (_: Exception) {}
                }

                cmd.join()
                emit(SshOutput.Complete(cmd.exitStatus ?: 0))
            }
        } catch (e: Exception) {
            emit(SshOutput.Error(e.localizedMessage ?: "Errore SSH"))
        } finally {
            if (ssh.isConnected) {
                try { ssh.disconnect() } catch (_: Exception) {}
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Controlla se il server è raggiungibile via TCP sulla porta SSH.
     * Timeout configurabile. Non usa SSH, solo TCP.
     */
    suspend fun checkConnectivity(
        host: String,
        port: Int,
        timeoutMs: Int = 3000
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun startInteractiveSession(
        host: String,
        port: Int,
        username: String,
        password: String?,
        privateKeyContent: String?,
        sudoPassword: String? = null
    ): InteractiveSshSession = withContext(Dispatchers.IO) {
        val ssh = createSshClient()
        ssh.addHostKeyVerifier(PromiscuousVerifier())
        ssh.connect(host, port)
        ssh.timeout = 0 // No timeout per sessione interattiva

        if (!privateKeyContent.isNullOrBlank()) {
            val keyProvider: KeyProvider = ssh.loadKeys(privateKeyContent, null, null)
            ssh.authPublickey(username, keyProvider)
        } else if (!password.isNullOrBlank()) {
            ssh.authPassword(username, password)
        } else {
            ssh.disconnect()
            throw IllegalArgumentException("Nessuna credenziale fornita")
        }

        val session = ssh.startSession()
        session.allocateDefaultPTY()
        val shell = session.startShell()

        val effectiveSudo = sudoPassword?.takeIf { it.isNotBlank() } ?: password

        return@withContext InteractiveSshSessionImpl(
            sshClient = ssh,
            session = session,
            shell = shell,
            commandTransformer = { command -> transformCommandForSudo(command, effectiveSudo, requirePassword = false) }
        )
    }
}

private val SUDO_PREFIX_REGEX = Regex("^sudo(?:\\s|$)")

private fun transformCommandForSudo(
    command: String,
    sudoPassword: String?,
    requirePassword: Boolean = true
): String {
    val trimmed = command.trim()
    if (!SUDO_PREFIX_REGEX.containsMatchIn(trimmed)) return trimmed

    val afterSudo = trimmed.replaceFirst(SUDO_PREFIX_REGEX, "").trimStart()
    if (afterSudo.isBlank()) {
        throw IllegalArgumentException("Comando sudo non valido")
    }

    val password = sudoPassword?.takeIf { it.isNotBlank() }
        ?: if (requirePassword) {
            throw IllegalStateException("Password sudo non configurata per questo server")
        } else {
            return trimmed
        }

    return if (afterSudo.startsWith("-")) {
        "printf '%s\\n' ${shellQuote(password)} | sudo -S -p '' $afterSudo"
    } else {
        "printf '%s\\n' ${shellQuote(password)} | sudo -S -p '' sh -c ${shellQuote(afterSudo)}"
    }
}

private fun shellQuote(value: String): String =
    "'" + value.replace("'", "'\\''") + "'"

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
            
            // Nessun timeout socket durante l'esecuzione del comando
            ssh.timeout = 0
            
            if (!privateKeyContent.isNullOrBlank()) {
                val keyProvider: KeyProvider = ssh.loadKeys(privateKeyContent, null, null)
                ssh.authPublickey(username, keyProvider)
            } else if (!password.isNullOrBlank()) {
                ssh.authPassword(username, password)
            } else {
                throw IllegalArgumentException("Nessuna credenziale fornita")
            }

            emit(SshOutput.Started)

            val fullScript = buildShellScript(commands, sudoPassword)
            val fullCommand = "sh -c " + shellQuote(fullScript)

            ssh.startSession().use { session ->
                session.allocateDefaultPTY()
                val cmd: Session.Command = session.exec(fullCommand)

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
     * Costruisce uno script di shell robusto che:
     * - Esegue tutti i comandi in sequenza nello stesso processo shell (mantenendo 'cd' e env vars).
     * - 'set -e' per interrompere lo script al primo errore e restituire l'exit code corretto.
     * - Se fornita una sudoPassword, la inietta automaticamente validando il timestamp di sudo
     *   e definendo una funzione sudo wrapper per gestire sia comandi diretti (es. 'sudo apt update')
     *   sia comandi concatenati o innestati (es. 'cd /dir && sudo restart', 'echo x | sudo tee y').
     */
    private fun buildShellScript(commands: List<String>, sudoPassword: String?): String {
        return buildString {
            append("set -e\n")
            if (!sudoPassword.isNullOrBlank()) {
                val escapedPwd = shellQuote(sudoPassword)
                append("_SUDO_PWD=$escapedPwd\n")
                append("if command -v sudo >/dev/null 2>&1; then\n")
                append("  printf '%s\\n' \"\$_SUDO_PWD\" | command sudo -S -p '' -v 2>/dev/null || true\n")
                append("  sudo() {\n")
                append("    if [ -n \"\$_SUDO_PWD\" ]; then\n")
                append("      if command sudo -n true 2>/dev/null; then\n")
                append("        command sudo \"\$@\"\n")
                append("      else\n")
                append("        printf '%s\\n' \"\$_SUDO_PWD\" | command sudo -S -p '' \"\$@\"\n")
                append("      fi\n")
                append("    else\n")
                append("      command sudo -n \"\$@\"\n")
                append("    fi\n")
                append("  }\n")
                append("fi\n")
            } else {
                // Nessuna password sudo configurata: usa -n (non-interactive) per evitare blocchi infiniti su tty
                append("if command -v sudo >/dev/null 2>&1; then\n")
                append("  sudo() {\n")
                append("    command sudo -n \"\$@\"\n")
                append("  }\n")
                append("fi\n")
            }
            commands.forEach { cmd ->
                val trimmed = cmd.trim()
                if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                    append(trimmed)
                    append("\n")
                }
            }
        }
    }

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

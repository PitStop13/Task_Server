package com.taskserver.app.data.ssh

import com.taskserver.app.data.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.security.Security
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext

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
            ssh.timeout = 10000 
            
            if (!privateKeyContent.isNullOrBlank()) {
                val keyProvider: KeyProvider = ssh.loadKeys(privateKeyContent, null, null)
                ssh.authPublickey(username, keyProvider)
            } else if (!password.isNullOrBlank()) {
                ssh.authPassword(username, password)
            } else {
                throw IllegalArgumentException("Nessuna credenziale fornita")
            }

            emit(SshOutput.Started)

            // Tutti i comandi vengono eseguiti nello stesso processo bash.
            // Questo è l'unico modo per far persistere 'cd' tra un comando e l'altro.
            // Ogni comando è separato da '\n' all'interno di bash -c '...',
            // quindi la working directory e le variabili d'ambiente sono condivise.
            val fullCommand = buildShellScript(commands.map { transformCommandForSudo(it, sudoPassword) })

            ssh.startSession().use { session ->
                session.allocateDefaultPTY()
                val cmd: Session.Command = session.exec(fullCommand)
                
                BufferedReader(InputStreamReader(cmd.inputStream)).use { reader ->
                    var line: String?
                    while (currentCoroutineContext().isActive) {
                        line = reader.readLine()
                        if (line != null) {
                            emit(SshOutput.Data(line + "\n"))
                        } else break
                    }
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
     * Costruisce un singolo comando bash che esegue tutti i comandi in sequenza
     * nello stesso processo shell, mantenendo la working directory e le variabili
     * d'ambiente tra un comando e l'altro (es. 'cd' persiste).
     *
     * Strategia: bash -c 'cmd1\ncmd2\ncmd3'
     *   - Le single quotes in bash -c delimitano lo script
     *   - Le single quotes nei comandi vengono escaped come '\''
     *   - 'set -e' fa terminare lo script al primo errore (comportamento corretto per task)
     *
     * Esempio input:  ["cd /home/pit/immich-app", "docker compose pull", "docker compose up -d"]
     * Esempio output: bash -c 'set -e\ncd /home/pit/immich-app\ndocker compose pull\ndocker compose up -d'
     */
    private fun buildShellScript(commands: List<String>): String {
        // Unisce i comandi con newline in un unico script
        val script = buildString {
            append("set -e\n")         // esce al primo errore, restituisce exit code corretto
            commands.forEach { cmd ->
                append(cmd.trim())
                append("\n")
            }
        }
        // Esegue l'escape delle single quotes per poter usare bash -c '...'
        // In bash, dentro single quotes non si può avere ' direttamente.
        // La sequenza corretta è: chiudi le quote ('), scrivi \', riapri le quote (')
        val escaped = singleQuoteEscape(script)
        return "bash -c '$escaped'"
    }

    /**
     * Fa l'escape di una stringa per usarla dentro single quotes in bash.
     * Ogni ' nella stringa diventa '\''
     */
    private fun singleQuoteEscape(s: String): String =
        s.replace("'", "'\\''")

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

        return@withContext InteractiveSshSessionImpl(
            sshClient = ssh,
            session = session,
            shell = shell,
            commandTransformer = { command -> transformCommandForSudo(command, sudoPassword, requirePassword = false) }
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

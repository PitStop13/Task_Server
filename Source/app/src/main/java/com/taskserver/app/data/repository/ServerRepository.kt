package com.taskserver.app.data.repository

import com.taskserver.app.data.db.ServerDao
import com.taskserver.app.data.model.AuthType
import com.taskserver.app.data.model.ServerCredentials
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.ssh.SshManager
import com.taskserver.app.security.CredentialManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(
    private val dao: ServerDao,
    private val credentialManager: CredentialManager,
    private val sshManager: SshManager
) {
    fun getAllServers(): Flow<List<Server>> = dao.getAllServers()

    suspend fun getServerById(id: Long): Server? = dao.getServerById(id)

    /**
     * Salva server + credenziali cifrate.
     * Restituisce l'ID del server inserito/aggiornato.
     */
    suspend fun saveServer(
        server: Server,
        sshPassword: String?,
        privateKey: String?,
        sudoPassword: String?
    ): Long {
        val id = dao.insertServer(server)
        when (server.authType) {
            AuthType.PASSWORD -> {
                if (sshPassword.isNullOrBlank()) {
                    credentialManager.deleteSshPassword(id)
                } else {
                    credentialManager.saveSshPassword(id, sshPassword)
                }
                credentialManager.deletePrivateKey(id)
            }
            AuthType.KEY -> {
                if (privateKey.isNullOrBlank()) {
                    credentialManager.deletePrivateKey(id)
                } else {
                    credentialManager.savePrivateKey(id, privateKey)
                }
                credentialManager.deleteSshPassword(id)
            }
        }
        if (sudoPassword.isNullOrBlank()) {
            credentialManager.deleteSudoPassword(id)
        } else {
            credentialManager.saveSudoPassword(id, sudoPassword)
        }
        return id
    }

    suspend fun updateServer(
        server: Server,
        sshPassword: String?,
        privateKey: String?,
        sudoPassword: String?
    ) {
        dao.updateServer(server)
        when (server.authType) {
            AuthType.PASSWORD -> {
                if (sshPassword.isNullOrBlank()) {
                    credentialManager.deleteSshPassword(server.id)
                } else {
                    credentialManager.saveSshPassword(server.id, sshPassword)
                }
                credentialManager.deletePrivateKey(server.id)
            }
            AuthType.KEY -> {
                if (privateKey.isNullOrBlank()) {
                    credentialManager.deletePrivateKey(server.id)
                } else {
                    credentialManager.savePrivateKey(server.id, privateKey)
                }
                credentialManager.deleteSshPassword(server.id)
            }
        }
        if (sudoPassword.isNullOrBlank()) {
            credentialManager.deleteSudoPassword(server.id)
        } else {
            credentialManager.saveSudoPassword(server.id, sudoPassword)
        }
    }

    suspend fun deleteServer(server: Server) {
        credentialManager.deleteCredentials(server.id)
        dao.deleteServer(server)
    }

    suspend fun checkConnectivity(server: Server): Boolean =
        sshManager.checkConnectivity(server.host, server.port)

    fun getCredential(server: Server): ServerCredentials {
        return when (server.authType) {
            AuthType.PASSWORD -> ServerCredentials(
                sshPassword = credentialManager.getSshPassword(server.id),
                sudoPassword = credentialManager.getSudoPassword(server.id)
            )
            AuthType.KEY      -> ServerCredentials(
                privateKey = credentialManager.getPrivateKey(server.id),
                sudoPassword = credentialManager.getSudoPassword(server.id)
            )
        }
    }
}

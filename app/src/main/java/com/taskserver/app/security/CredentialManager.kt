package com.taskserver.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "taskserver_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun credentialKey(serverId: Long, suffix: String): String =
        "server_${serverId}_$suffix"

    fun saveSshPassword(serverId: Long, password: String) {
        prefs.edit().putString(credentialKey(serverId, "password"), password).apply()
    }

    fun deleteSshPassword(serverId: Long) {
        prefs.edit().remove(credentialKey(serverId, "password")).apply()
    }

    fun getSshPassword(serverId: Long): String? =
        prefs.getString(credentialKey(serverId, "password"), null)

    fun savePrivateKey(serverId: Long, privateKey: String) {
        prefs.edit().putString(credentialKey(serverId, "key"), privateKey).apply()
    }

    fun deletePrivateKey(serverId: Long) {
        prefs.edit().remove(credentialKey(serverId, "key")).apply()
    }

    fun getPrivateKey(serverId: Long): String? =
        prefs.getString(credentialKey(serverId, "key"), null)

    fun saveSudoPassword(serverId: Long, password: String) {
        prefs.edit().putString(credentialKey(serverId, "sudo_password"), password).apply()
    }

    fun deleteSudoPassword(serverId: Long) {
        prefs.edit().remove(credentialKey(serverId, "sudo_password")).apply()
    }

    fun getSudoPassword(serverId: Long): String? =
        prefs.getString(credentialKey(serverId, "sudo_password"), null)

    fun deleteCredentials(serverId: Long) {
        prefs.edit()
            .remove(credentialKey(serverId, "password"))
            .remove(credentialKey(serverId, "key"))
            .remove(credentialKey(serverId, "sudo_password"))
            .apply()
    }
}

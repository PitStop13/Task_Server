package com.taskserver.app.data.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("taskserver_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_SSH_ENCRYPTION = "ssh_encryption"
        private const val KEY_BIOMETRIC_AUTH = "biometric_auth"
    }

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)

    fun setDarkMode(enabled: Boolean) = prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()

    fun isSshEncryptionEnabled(): Boolean = prefs.getBoolean(KEY_SSH_ENCRYPTION, true)

    fun setSshEncryptionEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SSH_ENCRYPTION, enabled).apply()

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_AUTH, false)

    fun setBiometricEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_BIOMETRIC_AUTH, enabled).apply()
}

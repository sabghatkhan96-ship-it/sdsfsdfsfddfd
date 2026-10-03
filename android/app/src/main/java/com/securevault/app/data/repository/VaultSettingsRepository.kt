package com.securevault.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VaultSettingsRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_vault_settings"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_AUTOLOCK_SECONDS = "autolock_seconds"
        private const val KEY_CLIPBOARD_CLEAR_SECONDS = "clipboard_clear_seconds"
        private const val KEY_FLAG_SECURE_ENABLED = "flag_secure_enabled"
        private const val KEY_AUTOFILL_ENABLED = "autofill_enabled"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _biometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true))
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _autoLockSeconds = MutableStateFlow(prefs.getInt(KEY_AUTOLOCK_SECONDS, 60)) // default 1 min
    val autoLockSeconds: StateFlow<Int> = _autoLockSeconds.asStateFlow()

    private val _clipboardClearSeconds = MutableStateFlow(prefs.getInt(KEY_CLIPBOARD_CLEAR_SECONDS, 30)) // default 30s
    val clipboardClearSeconds: StateFlow<Int> = _clipboardClearSeconds.asStateFlow()

    private val _flagSecureEnabled = MutableStateFlow(prefs.getBoolean(KEY_FLAG_SECURE_ENABLED, true))
    val flagSecureEnabled: StateFlow<Boolean> = _flagSecureEnabled.asStateFlow()

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _biometricEnabled.value = enabled
    }

    fun setAutoLockSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_AUTOLOCK_SECONDS, seconds).apply()
        _autoLockSeconds.value = seconds
    }

    fun setClipboardClearSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_CLIPBOARD_CLEAR_SECONDS, seconds).apply()
        _clipboardClearSeconds.value = seconds
    }

    fun setFlagSecureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLAG_SECURE_ENABLED, enabled).apply()
        _flagSecureEnabled.value = enabled
    }
}

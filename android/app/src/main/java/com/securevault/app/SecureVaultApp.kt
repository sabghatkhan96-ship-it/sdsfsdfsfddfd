package com.securevault.app

import android.app.Application
import com.securevault.app.data.crypto.CryptoManager
import com.securevault.app.data.crypto.MasterKeyManager
import com.securevault.app.data.db.VaultDatabase
import com.securevault.app.data.repository.VaultRepository
import com.securevault.app.data.repository.VaultSettingsRepository
import com.securevault.app.security.BiometricAuthManager
import com.securevault.app.security.ClipboardSecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecureVaultApp : Application() {

    lateinit var database: VaultDatabase
        private set

    lateinit var cryptoManager: CryptoManager
        private set

    lateinit var masterKeyManager: MasterKeyManager
        private set

    lateinit var vaultRepository: VaultRepository
        private set

    lateinit var vaultSettingsRepository: VaultSettingsRepository
        private set

    lateinit var biometricAuthManager: BiometricAuthManager
        private set

    lateinit var clipboardSecurityManager: ClipboardSecurityManager
        private set

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private var lastBackgroundTimestamp: Long = 0L

    override fun onCreate() {
        super.onCreate()

        database = VaultDatabase.getInstance(this)
        cryptoManager = CryptoManager()
        masterKeyManager = MasterKeyManager(this)
        vaultRepository = VaultRepository(database.credentialDao(), cryptoManager)
        vaultSettingsRepository = VaultSettingsRepository(this)
        biometricAuthManager = BiometricAuthManager(this)
        clipboardSecurityManager = ClipboardSecurityManager(this)
    }

    fun unlockVault() {
        _isVaultUnlocked.value = true
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun onAppForegrounded() {
        val timeoutSeconds = vaultSettingsRepository.autoLockSeconds.value
        if (timeoutSeconds == 0) {
            // Immediate lock on background
            lockVault()
        } else if (lastBackgroundTimestamp > 0) {
            val elapsedSeconds = (System.currentTimeMillis() - lastBackgroundTimestamp) / 1000
            if (elapsedSeconds >= timeoutSeconds) {
                lockVault()
            }
        }
    }

    fun onAppBackgrounded() {
        lastBackgroundTimestamp = System.currentTimeMillis()
    }
}

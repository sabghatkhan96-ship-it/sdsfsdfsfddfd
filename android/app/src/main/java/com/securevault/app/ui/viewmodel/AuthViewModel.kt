package com.securevault.app.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.app.SecureVaultApp
import com.securevault.app.security.BiometricAuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isSetup: Boolean = false,
    val isUnlocked: Boolean = false,
    val enteredPin: String = "",
    val confirmPin: String = "",
    val isConfirmingPin: Boolean = false,
    val errorMessage: String? = null,
    val isBiometricAvailable: Boolean = false
)

class AuthViewModel(private val app: SecureVaultApp) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        val isSetup = app.masterKeyManager.isMasterKeySet()
        val isBioAvail = app.biometricAuthManager.isBiometricAvailable()
        _uiState.value = _uiState.value.copy(
            isSetup = isSetup,
            isUnlocked = app.isVaultUnlocked.value,
            isBiometricAvailable = isBioAvail
        )

        viewModelScope.launch {
            app.isVaultUnlocked.collect { unlocked ->
                _uiState.value = _uiState.value.copy(isUnlocked = unlocked)
            }
        }
    }

    fun onPinDigit(digit: String) {
        val current = _uiState.value.enteredPin
        if (current.length < 6) {
            val updated = current + digit
            _uiState.value = _uiState.value.copy(enteredPin = updated, errorMessage = null)

            // Auto-check on 4-6 digits if unlocked or setup
            if (updated.length >= 4) {
                if (!_uiState.value.isSetup) {
                    // During Setup
                    if (_uiState.value.isConfirmingPin && updated.length == _uiState.value.confirmPin.length) {
                        confirmSetup(updated)
                    }
                } else if (updated.length == 6 || (updated.length >= 4 && app.masterKeyManager.verifyMasterPin(updated))) {
                    verifyAndUnlock(updated)
                }
            }
        }
    }

    fun onPinBackspace() {
        val current = _uiState.value.enteredPin
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                enteredPin = current.dropLast(1),
                errorMessage = null
            )
        }
    }

    fun proceedToConfirmPin() {
        val pin = _uiState.value.enteredPin
        if (pin.length < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "PIN must be at least 4 digits")
            return
        }
        _uiState.value = _uiState.value.copy(
            confirmPin = pin,
            enteredPin = "",
            isConfirmingPin = true,
            errorMessage = null
        )
    }

    private fun confirmSetup(secondPin: String) {
        val firstPin = _uiState.value.confirmPin
        if (firstPin == secondPin) {
            app.masterKeyManager.setMasterPin(firstPin)
            app.unlockVault()
            _uiState.value = _uiState.value.copy(
                isSetup = true,
                isUnlocked = true,
                enteredPin = "",
                confirmPin = "",
                isConfirmingPin = false,
                errorMessage = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                enteredPin = "",
                errorMessage = "PINs do not match. Try again."
            )
        }
    }

    fun verifyAndUnlock(pin: String = _uiState.value.enteredPin) {
        if (app.masterKeyManager.verifyMasterPin(pin)) {
            app.unlockVault()
            _uiState.value = _uiState.value.copy(
                enteredPin = "",
                errorMessage = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                enteredPin = "",
                errorMessage = "Incorrect PIN. Try again."
            )
        }
    }

    fun triggerBiometricUnlock(activity: FragmentActivity) {
        if (!_uiState.value.isSetup) return
        if (!app.vaultSettingsRepository.biometricEnabled.value) return

        viewModelScope.launch {
            val result = app.biometricAuthManager.authenticate(
                activity = activity,
                title = "Unlock SecureVault",
                subtitle = "Authenticate to decrypt vault"
            )
            when (result) {
                is BiometricAuthResult.Success -> {
                    app.unlockVault()
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                }
                is BiometricAuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.errString.toString())
                }
                BiometricAuthResult.Failed -> {
                    _uiState.value = _uiState.value.copy(errorMessage = "Biometric authentication not recognized")
                }
                BiometricAuthResult.NotAvailable -> {
                    // Fall back to PIN
                }
            }
        }
    }

    fun lockVault() {
        app.lockVault()
        _uiState.value = _uiState.value.copy(enteredPin = "")
    }
}

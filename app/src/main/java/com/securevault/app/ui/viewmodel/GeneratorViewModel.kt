package com.securevault.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.securevault.app.SecureVaultApp
import com.securevault.app.security.GeneratorOptions
import com.securevault.app.security.PasswordGenerator
import com.securevault.app.security.PasswordStrength
import com.securevault.app.security.PasswordStrengthEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GeneratorUiState(
    val generatedValue: String = "",
    val options: GeneratorOptions = GeneratorOptions(),
    val strength: PasswordStrength = PasswordStrengthEvaluator.evaluate(""),
    val isPassphraseMode: Boolean = false,
    val passphraseWords: Int = 4,
    val toastMessage: String? = null
)

class GeneratorViewModel(private val app: SecureVaultApp) : ViewModel() {

    private val generator = PasswordGenerator()
    private val clipboardSecurityManager = app.clipboardSecurityManager
    private val settingsRepository = app.vaultSettingsRepository

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    init {
        generate()
    }

    fun generate() {
        val current = _uiState.value
        val result = if (current.isPassphraseMode) {
            generator.generatePassphrase(current.passphraseWords)
        } else {
            generator.generate(current.options)
        }
        val strength = PasswordStrengthEvaluator.evaluate(result)
        _uiState.value = current.copy(
            generatedValue = result,
            strength = strength
        )
    }

    fun setLength(length: Int) {
        val updatedOptions = _uiState.value.options.copy(length = length)
        _uiState.value = _uiState.value.copy(options = updatedOptions)
        generate()
    }

    fun toggleUppercase() {
        val current = _uiState.value.options
        val updated = current.copy(includeUppercase = !current.includeUppercase)
        _uiState.value = _uiState.value.copy(options = updated)
        generate()
    }

    fun toggleLowercase() {
        val current = _uiState.value.options
        val updated = current.copy(includeLowercase = !current.includeLowercase)
        _uiState.value = _uiState.value.copy(options = updated)
        generate()
    }

    fun toggleDigits() {
        val current = _uiState.value.options
        val updated = current.copy(includeDigits = !current.includeDigits)
        _uiState.value = _uiState.value.copy(options = updated)
        generate()
    }

    fun toggleSymbols() {
        val current = _uiState.value.options
        val updated = current.copy(includeSymbols = !current.includeSymbols)
        _uiState.value = _uiState.value.copy(options = updated)
        generate()
    }

    fun toggleExcludeAmbiguous() {
        val current = _uiState.value.options
        val updated = current.copy(excludeAmbiguous = !current.excludeAmbiguous)
        _uiState.value = _uiState.value.copy(options = updated)
        generate()
    }

    fun setPassphraseMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isPassphraseMode = enabled)
        generate()
    }

    fun setPassphraseWords(count: Int) {
        _uiState.value = _uiState.value.copy(passphraseWords = count)
        generate()
    }

    fun copyToClipboard() {
        val text = _uiState.value.generatedValue
        val clearSeconds = settingsRepository.clipboardClearSeconds.value
        clipboardSecurityManager.copySensitiveText("Generated Password", text, clearSeconds)
        val msg = if (clearSeconds > 0) "Password copied (clears in ${clearSeconds}s)" else "Password copied"
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }

    fun clearToastMessage() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}

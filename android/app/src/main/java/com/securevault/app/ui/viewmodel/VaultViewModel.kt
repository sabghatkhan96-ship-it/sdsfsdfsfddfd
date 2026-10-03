package com.securevault.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.app.SecureVaultApp
import com.securevault.app.data.model.Category
import com.securevault.app.data.model.Credential
import com.securevault.app.data.model.VaultHealth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VaultUiState(
    val searchQuery: String = "",
    val selectedCategory: Category = Category.ALL,
    val selectedCredential: Credential? = null,
    val vaultHealth: VaultHealth? = null,
    val toastMessage: String? = null
)

class VaultViewModel(private val app: SecureVaultApp) : ViewModel() {

    private val repository = app.vaultRepository
    private val clipboardSecurityManager = app.clipboardSecurityManager
    private val settingsRepository = app.vaultSettingsRepository

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    // Observable filtered credentials flow
    val credentials: StateFlow<List<Credential>> = combine(
        repository.getAllCredentials(),
        _uiState
    ) { allCreds, state ->
        allCreds.filter { cred ->
            val matchesCategory = (state.selectedCategory == Category.ALL || cred.category == state.selectedCategory)
            val matchesSearch = if (state.searchQuery.isBlank()) true else {
                val q = state.searchQuery.lowercase()
                cred.title.lowercase().contains(q) ||
                        cred.domainOrPackage.lowercase().contains(q) ||
                        cred.username.lowercase().contains(q)
            }
            matchesCategory && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshVaultHealth()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSelectedCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun selectCredential(credential: Credential?) {
        _uiState.value = _uiState.value.copy(selectedCredential = credential)
    }

    fun saveCredential(credential: Credential, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveCredential(credential)
            refreshVaultHealth()
            _uiState.value = _uiState.value.copy(toastMessage = "Saved '${credential.title}' securely")
            onComplete()
        }
    }

    fun deleteCredential(credential: Credential, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCredential(credential.id)
            refreshVaultHealth()
            _uiState.value = _uiState.value.copy(
                selectedCredential = null,
                toastMessage = "Deleted '${credential.title}'"
            )
            onComplete()
        }
    }

    fun copyUsername(username: String) {
        val clearSeconds = settingsRepository.clipboardClearSeconds.value
        clipboardSecurityManager.copySensitiveText("Username", username, clearSeconds)
        val msg = if (clearSeconds > 0) "Username copied (clears in ${clearSeconds}s)" else "Username copied"
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }

    fun copyPassword(password: String) {
        val clearSeconds = settingsRepository.clipboardClearSeconds.value
        clipboardSecurityManager.copySensitiveText("Password", password, clearSeconds)
        val msg = if (clearSeconds > 0) "Password copied (clears in ${clearSeconds}s)" else "Password copied"
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }

    fun clearToastMessage() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun refreshVaultHealth() {
        viewModelScope.launch {
            val health = repository.computeVaultHealth()
            _uiState.value = _uiState.value.copy(vaultHealth = health)
        }
    }
}

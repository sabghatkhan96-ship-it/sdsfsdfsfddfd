package com.securevault.app.data.repository

import com.securevault.app.data.crypto.CryptoManager
import com.securevault.app.data.db.CredentialDao
import com.securevault.app.data.db.CredentialEntity
import com.securevault.app.data.model.Category
import com.securevault.app.data.model.Credential
import com.securevault.app.data.model.VaultHealth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VaultRepository(
    private val credentialDao: CredentialDao,
    private val cryptoManager: CryptoManager
) {

    fun getAllCredentials(): Flow<List<Credential>> {
        return credentialDao.getAllCredentials().map { entities ->
            entities.map { decryptEntity(it) }
        }
    }

    suspend fun getAllCredentialsSync(): List<Credential> {
        return credentialDao.getAllCredentialsSync().map { decryptEntity(it) }
    }

    suspend fun getCredentialById(id: Long): Credential? {
        val entity = credentialDao.getCredentialById(id) ?: return null
        return decryptEntity(entity)
    }

    fun getCredentialsByCategory(category: Category): Flow<List<Credential>> {
        return if (category == Category.ALL) {
            getAllCredentials()
        } else {
            credentialDao.getCredentialsByCategory(category.name).map { entities ->
                entities.map { decryptEntity(it) }
            }
        }
    }

    fun searchCredentials(query: String): Flow<List<Credential>> {
        return credentialDao.searchCredentials(query).map { entities ->
            entities.map { decryptEntity(it) }
        }
    }

    suspend fun saveCredential(credential: Credential): Long {
        val entity = encryptModel(credential)
        return if (credential.id == 0L) {
            credentialDao.insertCredential(entity)
        } else {
            credentialDao.updateCredential(entity)
            credential.id
        }
    }

    suspend fun deleteCredential(id: Long) {
        credentialDao.deleteCredentialById(id)
    }

    suspend fun findMatchingCredentialsForTarget(target: String): List<Credential> {
        val all = getAllCredentialsSync()
        return all.filter { it.matchesTarget(target) }
    }

    suspend fun computeVaultHealth(): VaultHealth {
        val credentials = getAllCredentialsSync()
        if (credentials.isEmpty()) {
            return VaultHealth(0, 100, 0, 0, 0, emptyList(), emptyList())
        }

        val weak = mutableListOf<Credential>()
        val passwordCountMap = mutableMapOf<String, Int>()

        for (cred in credentials) {
            val pass = cred.password
            passwordCountMap[pass] = (passwordCountMap[pass] ?: 0) + 1
            if (isPasswordWeak(pass)) {
                weak.add(cred)
            }
        }

        val reused = credentials.filter { (passwordCountMap[it.password] ?: 0) > 1 }
        val strongCount = credentials.size - weak.size - (reused.size - passwordCountMap.filter { it.value > 1 }.size)

        var score = 100
        score -= (weak.size * 15)
        score -= (reused.size * 10)
        score = score.coerceIn(0, 100)

        return VaultHealth(
            totalCredentials = credentials.size,
            securityScore = score,
            weakCount = weak.size,
            reusedCount = reused.size,
            strongCount = maxOf(0, strongCount),
            weakCredentials = weak,
            reusedCredentials = reused
        )
    }

    private fun isPasswordWeak(password: String): Boolean {
        if (password.length < 10) return true
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        val varieties = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }
        return varieties < 3
    }

    private fun encryptModel(credential: Credential): CredentialEntity {
        // Password and sensitive fields are encrypted with hardware-backed AES-256-GCM
        val encPass = cryptoManager.encrypt(credential.password)
        val encUser = cryptoManager.encrypt(credential.username)
        val encNotes = cryptoManager.encrypt(credential.notes)

        return CredentialEntity(
            id = credential.id,
            title = credential.title,
            domainOrPackage = credential.domainOrPackage,
            usernameEncrypted = encUser.ciphertextBase64,
            passwordEncrypted = encPass.ciphertextBase64,
            notesEncrypted = encNotes.ciphertextBase64,
            iv = encPass.ivBase64, // Primary IV
            category = credential.category.name,
            isFavorite = credential.isFavorite,
            requiresBiometricForAutofill = credential.requiresBiometricForAutofill,
            createdAt = credential.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun decryptEntity(entity: CredentialEntity): Credential {
        // Separate decryption with fallback in case of format variations
        val userEnc = cryptoManager.decrypt(entity.usernameEncrypted, entity.iv)
        val passEnc = cryptoManager.decrypt(entity.passwordEncrypted, entity.iv)
        val notesEnc = cryptoManager.decrypt(entity.notesEncrypted, entity.iv)

        return Credential(
            id = entity.id,
            title = entity.title,
            domainOrPackage = entity.domainOrPackage,
            username = userEnc,
            password = passEnc,
            notes = notesEnc,
            category = Category.fromString(entity.category),
            isFavorite = entity.isFavorite,
            requiresBiometricForAutofill = entity.requiresBiometricForAutofill,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

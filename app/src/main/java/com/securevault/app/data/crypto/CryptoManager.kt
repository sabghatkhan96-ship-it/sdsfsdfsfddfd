package com.securevault.app.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class EncryptedPayload(
    val ciphertextBase64: String,
    val ivBase64: String
)

class CryptoManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SecureVault_Master_AES_Key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val KEY_SIZE = 256
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    init {
        ensureKeyExists()
    }

    private fun ensureKeyExists() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(parameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    /**
     * Encrypts plaintext string using AES-256-GCM.
     * Returns the ciphertext and unique random IV encoded in Base64.
     */
    fun encrypt(plaintext: String): EncryptedPayload {
        if (plaintext.isEmpty()) {
            return EncryptedPayload("", "")
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val plaintextBytes = plaintext.toByteArray(StandardCharsets.UTF_8)
        val ciphertext = cipher.doFinal(plaintextBytes)
        val iv = cipher.iv

        SecureMemory.wipe(plaintextBytes)

        val ciphertextBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

        return EncryptedPayload(ciphertextBase64, ivBase64)
    }

    /**
     * Decrypts ciphertext Base64 using AES-256-GCM with the provided IV Base64.
     */
    fun decrypt(ciphertextBase64: String, ivBase64: String): String {
        if (ciphertextBase64.isEmpty() || ivBase64.isEmpty()) {
            return ""
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val ciphertextBytes = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(ciphertextBytes)
        val result = String(decryptedBytes, StandardCharsets.UTF_8)

        SecureMemory.wipe(decryptedBytes)
        return result
    }

    /**
     * Provides an uninitialized Cipher suitable for BiometricPrompt.CryptoObject binding.
     */
    fun getCipherForBiometricAuth(): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        return cipher
    }
}

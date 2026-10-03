package com.securevault.app.data.crypto

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class MasterKeyManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_vault_master_auth"
        private const val KEY_PIN_HASH = "master_pin_hash"
        private const val KEY_PIN_SALT = "master_pin_salt"
        private const val KEY_IS_SETUP = "master_is_setup"
        private const val ITERATIONS = 100_000
        private const val KEY_LENGTH = 256
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val SALT_BYTES = 16
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isMasterKeySet(): Boolean {
        return prefs.getBoolean(KEY_IS_SETUP, false)
    }

    fun setMasterPin(pin: String): Boolean {
        if (pin.length < 4) return false

        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)

        val hash = hashPin(pin, salt)

        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)

        prefs.edit()
            .putString(KEY_PIN_SALT, saltBase64)
            .putString(KEY_PIN_HASH, hashBase64)
            .putBoolean(KEY_IS_SETUP, true)
            .apply()

        SecureMemory.wipe(salt)
        SecureMemory.wipe(hash)
        return true
    }

    fun verifyMasterPin(pin: String): Boolean {
        if (!isMasterKeySet()) return false

        val saltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val storedHashBase64 = prefs.getString(KEY_PIN_HASH, null) ?: return false

        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val computedHash = hashPin(pin, salt)
        val computedHashBase64 = Base64.encodeToString(computedHash, Base64.NO_WRAP)

        SecureMemory.wipe(salt)
        SecureMemory.wipe(computedHash)

        // Constant-time equals to protect against timing attacks
        return slowEquals(storedHashBase64, computedHashBase64)
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val pinChars = pin.toCharArray()
        val spec = PBEKeySpec(pinChars, salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded

        SecureMemory.wipe(pinChars)
        spec.clearPassword()
        return hash
    }

    private fun slowEquals(a: String, b: String): Boolean {
        var diff = a.length xor b.length
        val len = minOf(a.length, b.length)
        for (i in 0 until len) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}

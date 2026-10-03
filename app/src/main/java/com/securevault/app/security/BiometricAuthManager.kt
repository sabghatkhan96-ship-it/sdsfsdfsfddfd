package com.securevault.app.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class BiometricAuthResult {
    data object Success : BiometricAuthResult()
    data class Error(val errorCode: Int, val errString: CharSequence) : BiometricAuthResult()
    data object Failed : BiometricAuthResult()
    data object NotAvailable : BiometricAuthResult()
}

class BiometricAuthManager(private val context: Context) {

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock SecureVault",
        subtitle: String = "Verify biometric or device credential",
        description: String = "Confirm your identity to access credentials"
    ): BiometricAuthResult = suspendCancellableCoroutine { continuation ->
        if (!isBiometricAvailable()) {
            continuation.resume(BiometricAuthResult.NotAvailable)
            return@suspendCancellableCoroutine
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (continuation.isActive) {
                        continuation.resume(BiometricAuthResult.Success)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (continuation.isActive) {
                        continuation.resume(BiometricAuthResult.Error(errorCode, errString))
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Android BiometricPrompt stays open on soft failure (e.g. unrecognised finger),
                    // so we don't necessarily cancel continuation immediately unless desired.
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        continuation.invokeOnCancellation {
            prompt.cancelAuthentication()
        }

        prompt.authenticate(promptInfo)
    }
}

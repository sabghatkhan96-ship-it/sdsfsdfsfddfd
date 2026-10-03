package com.securevault.app.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import android.service.autofill.Dataset
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.securevault.app.R
import com.securevault.app.SecureVaultApp
import com.securevault.app.security.BiometricAuthResult
import kotlinx.coroutines.launch

class AutofillAuthActivity : FragmentActivity() {

    companion object {
        const val EXTRA_CREDENTIAL_ID = "extra_credential_id"
        const val EXTRA_USERNAME_ID = "extra_username_id"
        const val EXTRA_PASSWORD_ID = "extra_password_id"

        fun createIntentSender(
            context: Context,
            credentialId: Long,
            usernameId: AutofillId?,
            passwordId: AutofillId?
        ): IntentSender {
            val intent = Intent(context, AutofillAuthActivity::class.java).apply {
                putExtra(EXTRA_CREDENTIAL_ID, credentialId)
                putExtra(EXTRA_USERNAME_ID, usernameId)
                putExtra(EXTRA_PASSWORD_ID, passwordId)
            }
            val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            return PendingIntent.getActivity(context, credentialId.toInt(), intent, flags).intentSender
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val credentialId = intent.getLongExtra(EXTRA_CREDENTIAL_ID, -1L)
        val usernameId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_USERNAME_ID, AutofillId::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_USERNAME_ID)
        }
        val passwordId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_PASSWORD_ID, AutofillId::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_PASSWORD_ID)
        }

        if (credentialId == -1L || (usernameId == null && passwordId == null)) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        requestBiometricAuth(credentialId, usernameId, passwordId)
    }

    private fun requestBiometricAuth(
        credentialId: Long,
        usernameId: AutofillId?,
        passwordId: AutofillId?
    ) {
        val app = application as SecureVaultApp
        lifecycleScope.launch {
            val result = app.biometricAuthManager.authenticate(
                activity = this@AutofillAuthActivity,
                title = getString(R.string.biometric_prompt_title),
                subtitle = "Biometric required to autofill login",
                description = "Verify identity to fill credentials securely"
            )

            when (result) {
                is BiometricAuthResult.Success -> {
                    onAuthSuccess(credentialId, usernameId, passwordId)
                }
                is BiometricAuthResult.Error -> {
                    Toast.makeText(this@AutofillAuthActivity, result.errString, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_CANCELED)
                    finish()
                }
                BiometricAuthResult.Failed, BiometricAuthResult.NotAvailable -> {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    private suspend fun onAuthSuccess(
        credentialId: Long,
        usernameId: AutofillId?,
        passwordId: AutofillId?
    ) {
        val app = application as SecureVaultApp
        val credential = app.vaultRepository.getCredentialById(credentialId)
        if (credential == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val presentation = RemoteViews(packageName, R.layout.autofill_dataset_item).apply {
            setTextViewText(R.id.autofill_title, credential.title)
            setTextViewText(R.id.autofill_subtitle, credential.username)
        }

        val datasetBuilder = Dataset.Builder()
        if (usernameId != null) {
            datasetBuilder.setValue(usernameId, AutofillValue.forText(credential.username), presentation)
        }
        if (passwordId != null) {
            datasetBuilder.setValue(passwordId, AutofillValue.forText(credential.password), presentation)
        }

        val dataset = datasetBuilder.build()

        val replyIntent = Intent().apply {
            putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
        }
        setResult(RESULT_OK, replyIntent)
        finish()
    }
}

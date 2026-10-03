package com.securevault.app.service

import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveInfo
import android.service.autofill.SaveRequest
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.securevault.app.R
import com.securevault.app.SecureVaultApp
import com.securevault.app.data.model.Category
import com.securevault.app.data.model.Credential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class VaultAutofillService : AutofillService() {

    companion object {
        private const val TAG = "VaultAutofillService"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val fillContexts = request.fillContexts
        if (fillContexts.isEmpty()) {
            callback.onSuccess(null)
            return
        }

        val latestStructure = fillContexts.last().structure
        val parsed = AutofillStructureParser.parse(latestStructure)

        val target = parsed.webDomain ?: parsed.packageName
        if (target.isBlank() || (parsed.usernameId == null && parsed.passwordId == null)) {
            callback.onSuccess(null)
            return
        }

        serviceScope.launch {
            try {
                val app = application as SecureVaultApp
                val matches = app.vaultRepository.findMatchingCredentialsForTarget(target)

                if (matches.isEmpty()) {
                    callback.onSuccess(null)
                    return@launch
                }

                val responseBuilder = FillResponse.Builder()

                for (credential in matches) {
                    val presentation = RemoteViews(packageName, R.layout.autofill_dataset_item).apply {
                        setTextViewText(R.id.autofill_title, credential.title)
                        setTextViewText(R.id.autofill_subtitle, credential.username)
                        if (credential.requiresBiometricForAutofill) {
                            setTextViewText(R.id.autofill_badge, "🔒 Verify ID")
                        } else {
                            setTextViewText(R.id.autofill_badge, "🛡️ Autofill")
                        }
                    }

                    val datasetBuilder = Dataset.Builder()

                    if (credential.requiresBiometricForAutofill) {
                        // Requires Biometric authentication before autofill is completed
                        val authIntentSender = AutofillAuthActivity.createIntentSender(
                            context = this@VaultAutofillService,
                            credentialId = credential.id,
                            usernameId = parsed.usernameId,
                            passwordId = parsed.passwordId
                        )
                        datasetBuilder.setAuthentication(authIntentSender)
                    }

                    if (parsed.usernameId != null) {
                        val userValue = if (credential.requiresBiometricForAutofill) null
                        else AutofillValue.forText(credential.username)
                        datasetBuilder.setValue(parsed.usernameId, userValue, presentation)
                    }

                    if (parsed.passwordId != null) {
                        val passValue = if (credential.requiresBiometricForAutofill) null
                        else AutofillValue.forText(credential.password)
                        datasetBuilder.setValue(parsed.passwordId, passValue, presentation)
                    }

                    responseBuilder.addDataset(datasetBuilder.build())
                }

                // Configure SaveInfo so Android can offer to save or update passwords
                val saveType = SaveInfo.SAVE_DATA_TYPE_PASSWORD or SaveInfo.SAVE_DATA_TYPE_USERNAME
                val requiredIds = mutableListOf<AutofillId>()
                parsed.usernameId?.let { requiredIds.add(it) }
                parsed.passwordId?.let { requiredIds.add(it) }

                if (requiredIds.isNotEmpty()) {
                    val saveInfoBuilder = SaveInfo.Builder(saveType, requiredIds.toTypedArray())
                    responseBuilder.setSaveInfo(saveInfoBuilder.build())
                }

                callback.onSuccess(responseBuilder.build())
            } catch (e: Exception) {
                Log.e(TAG, "Error processing onFillRequest", e)
                callback.onFailure(e.localizedMessage)
            }
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val fillContexts = request.fillContexts
        if (fillContexts.isEmpty()) {
            callback.onSuccess()
            return
        }

        val structure = fillContexts.last().structure
        val parsed = AutofillStructureParser.parse(structure)

        val target = parsed.webDomain ?: parsed.packageName
        val userVal = parsed.usernameValue
        val passVal = parsed.passwordValue

        // Only save when the user explicitly entered valid credentials
        if (!userVal.isNullOrBlank() && !passVal.isNullOrBlank()) {
            serviceScope.launch {
                try {
                    val app = application as SecureVaultApp
                    val existing = app.vaultRepository.findMatchingCredentialsForTarget(target)
                    val title = parsed.webDomain?.takeIf { it.isNotBlank() }
                        ?: parsed.packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }

                    val credentialToSave = existing.firstOrNull { it.username == userVal }?.copy(
                        password = passVal,
                        updatedAt = System.currentTimeMillis()
                    ) ?: Credential(
                        title = title,
                        domainOrPackage = target,
                        username = userVal,
                        password = passVal,
                        category = Category.LOGINS,
                        requiresBiometricForAutofill = true
                    )

                    app.vaultRepository.saveCredential(credentialToSave)
                    callback.onSuccess()
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing onSaveRequest", e)
                    callback.onFailure(e.localizedMessage)
                }
            }
        } else {
            callback.onSuccess()
        }
    }
}

package com.securevault.app.service

import android.app.assist.AssistStructure
import android.text.InputType
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue

data class ParsedAutofillStructure(
    val packageName: String,
    val webDomain: String?,
    val usernameId: AutofillId?,
    val passwordId: AutofillId?,
    val newPasswordId: AutofillId?,
    val usernameValue: String? = null,
    val passwordValue: String? = null
)

object AutofillStructureParser {

    fun parse(structure: AssistStructure): ParsedAutofillStructure {
        val packageName = structure.activityComponent.packageName ?: ""
        var detectedDomain: String? = null
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null
        var newPasswordId: AutofillId? = null
        var usernameValue: String? = null
        var passwordValue: String? = null

        val nodeQueue = ArrayDeque<AssistStructure.ViewNode>()

        val windowCount = structure.windowNodeCount
        for (i in 0 until windowCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val root = windowNode.rootViewNode
            if (root != null) {
                nodeQueue.add(root)
            }
        }

        while (nodeQueue.isNotEmpty()) {
            val node = nodeQueue.removeFirst()

            // 1. Check for Web Domain (Chrome, Firefox, WebView)
            if (node.webDomain != null && node.webDomain!!.isNotBlank()) {
                detectedDomain = node.webDomain
            }

            // 2. Identify fields by official autofill hints or heuristics
            val autofillHints = node.autofillHints
            val autofillId = node.autofillId
            val inputType = node.inputType
            val idEntry = node.idEntry?.lowercase() ?: ""
            val hintText = (node.hint ?: "").lowercase()

            val isPasswordType = (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT &&
                    ((inputType and InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                            (inputType and InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)

            val isEmailType = (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT &&
                    (inputType and InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            // Match Password Field
            if (passwordId == null && autofillId != null) {
                val hasPasswordHint = autofillHints?.any {
                    it.equals(View.AUTOFILL_HINT_PASSWORD, ignoreCase = true)
                } ?: false

                if (hasPasswordHint || isPasswordType || idEntry.contains("password") || idEntry.contains("pass") || hintText.contains("password")) {
                    passwordId = autofillId
                    val valText = getAutofillText(node.autofillValue) ?: node.text?.toString()
                    if (!valText.isNullOrBlank()) {
                        passwordValue = valText
                    }
                }
            }

            // Match Username / Email Field
            if (usernameId == null && autofillId != null && autofillId != passwordId) {
                val hasUserHint = autofillHints?.any {
                    it.equals(View.AUTOFILL_HINT_USERNAME, ignoreCase = true) ||
                            it.equals(View.AUTOFILL_HINT_EMAIL_ADDRESS, ignoreCase = true)
                } ?: false

                if (hasUserHint || isEmailType ||
                    idEntry.contains("username") || idEntry.contains("email") || idEntry.contains("login") ||
                    hintText.contains("username") || hintText.contains("email") || hintText.contains("login")
                ) {
                    usernameId = autofillId
                    val valText = getAutofillText(node.autofillValue) ?: node.text?.toString()
                    if (!valText.isNullOrBlank()) {
                        usernameValue = valText
                    }
                }
            }

            // Queue children for BFS traversal
            for (j in 0 until node.childCount) {
                node.getChildAt(j)?.let { nodeQueue.add(it) }
            }
        }

        return ParsedAutofillStructure(
            packageName = packageName,
            webDomain = detectedDomain,
            usernameId = usernameId,
            passwordId = passwordId,
            newPasswordId = newPasswordId,
            usernameValue = usernameValue,
            passwordValue = passwordValue
        )
    }

    private fun getAutofillText(value: AutofillValue?): String? {
        return if (value != null && value.isText) {
            value.textValue.toString()
        } else null
    }
}

package com.securevault.app.security

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle

class ClipboardSecurityManager(private val context: Context) {

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val handler = Handler(Looper.getMainLooper())
    private var clearRunnable: Runnable? = null
    private var lastCopiedText: String? = null

    /**
     * Copies sensitive text to clipboard and schedules auto-clear.
     * @param label A user-friendly label (e.g., "Password", "Username")
     * @param text The sensitive text to copy
     * @param timeoutSeconds Duration before clearing (0 to disable)
     */
    fun copySensitiveText(label: String, text: String, timeoutSeconds: Int = 30) {
        val clip = ClipData.newPlainText(label, text)

        // Android 13+ (API 33) provides EXTRA_IS_SENSITIVE to prevent previewing in notifications/keyboard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }

        clipboard.setPrimaryClip(clip)
        lastCopiedText = text

        // Cancel previous timer
        clearRunnable?.let { handler.removeCallbacks(it) }

        if (timeoutSeconds > 0) {
            clearRunnable = Runnable {
                clearClipboardIfMatching(text)
            }
            handler.postDelayed(clearRunnable!!, timeoutSeconds * 1000L)
        }
    }

    private fun clearClipboardIfMatching(expectedText: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboard.clearPrimaryClip()
            } else {
                val clip = ClipData.newPlainText("", "")
                clipboard.setPrimaryClip(clip)
            }
        } catch (_: Exception) {
            // Ignore security exception if app is in background on strict devices
        }
        if (lastCopiedText == expectedText) {
            lastCopiedText = null
        }
    }
}

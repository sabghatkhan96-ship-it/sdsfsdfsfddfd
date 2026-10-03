package com.securevault.app.data.crypto

import java.util.Arrays

object SecureMemory {
    /**
     * Overwrites the byte array with zeros to prevent memory scraping.
     */
    fun wipe(bytes: ByteArray?) {
        if (bytes != null) {
            Arrays.fill(bytes, 0.toByte())
        }
    }

    /**
     * Overwrites the char array with zeros to prevent memory scraping.
     */
    fun wipe(chars: CharArray?) {
        if (chars != null) {
            Arrays.fill(chars, '\u0000')
        }
    }
}

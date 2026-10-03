package com.securevault.app.security

import java.security.SecureRandom

data class GeneratorOptions(
    val length: Int = 16,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeDigits: Boolean = true,
    val includeSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = true // excludes l, 1, I, O, 0
)

class PasswordGenerator {

    companion object {
        private const val UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ" // minus ambiguous if enabled
        private const val UPPERCASE_ALL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val LOWERCASE = "abcdefghijkmnopqrstuvwxyz" // minus ambiguous if enabled
        private const val LOWERCASE_ALL = "abcdefghijklmnopqrstuvwxyz"
        private const val DIGITS = "23456789" // minus 0, 1
        private const val DIGITS_ALL = "0123456789"
        private const val SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?"
    }

    private val random = SecureRandom()

    fun generate(options: GeneratorOptions = GeneratorOptions()): String {
        val uppercaseSet = if (options.excludeAmbiguous) UPPERCASE else UPPERCASE_ALL
        val lowercaseSet = if (options.excludeAmbiguous) LOWERCASE else LOWERCASE_ALL
        val digitsSet = if (options.excludeAmbiguous) DIGITS else DIGITS_ALL
        val symbolsSet = SYMBOLS

        val charPool = StringBuilder()
        val guaranteedChars = mutableListOf<Char>()

        if (options.includeUppercase) {
            charPool.append(uppercaseSet)
            guaranteedChars.add(uppercaseSet[random.nextInt(uppercaseSet.length)])
        }
        if (options.includeLowercase) {
            charPool.append(lowercaseSet)
            guaranteedChars.add(lowercaseSet[random.nextInt(lowercaseSet.length)])
        }
        if (options.includeDigits) {
            charPool.append(digitsSet)
            guaranteedChars.add(digitsSet[random.nextInt(digitsSet.length)])
        }
        if (options.includeSymbols) {
            charPool.append(symbolsSet)
            guaranteedChars.add(symbolsSet[random.nextInt(symbolsSet.length)])
        }

        if (charPool.isEmpty()) {
            charPool.append(lowercaseSet)
            guaranteedChars.add(lowercaseSet[random.nextInt(lowercaseSet.length)])
        }

        val poolString = charPool.toString()
        val targetLength = options.length.coerceIn(8, 64)
        val remainingLength = targetLength - guaranteedChars.size

        val passwordChars = ArrayList<Char>(guaranteedChars)
        for (i in 0 until remainingLength) {
            passwordChars.add(poolString[random.nextInt(poolString.length)])
        }

        // Shuffle cryptographically using Fisher-Yates
        for (i in passwordChars.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = passwordChars[i]
            passwordChars[i] = passwordChars[j]
            passwordChars[j] = temp
        }

        return passwordChars.joinToString("")
    }

    /**
     * Generates a memorable Diceware-style passphrase.
     */
    fun generatePassphrase(wordsCount: Int = 4, separator: String = "-"): String {
        val wordList = listOf(
            "solar", "nebula", "quantum", "cipher", "beacon", "falcon",
            "glacier", "harbor", "horizon", "matrix", "shield", "summit",
            "thunder", "vector", "zenith", "aurora", "cobalt", "dynasty",
            "echo", "frost", "gravity", "haven", "ignite", "jasper",
            "krypton", "lunar", "mirage", "nexus", "orbit", "phoenix"
        )
        val selected = (0 until wordsCount).map {
            wordList[random.nextInt(wordList.size)]
        }
        return selected.joinToString(separator)
    }
}

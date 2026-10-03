package com.securevault.app.security

import kotlin.math.ln
import kotlin.math.roundToInt

enum class StrengthLevel(val label: String, val colorHex: Long) {
    VERY_WEAK("Very Weak", 0xFFEF4444),
    WEAK("Weak", 0xFFF97316),
    FAIR("Fair", 0xFFFBBF24),
    STRONG("Strong", 0xFF10B981),
    EXCELLENT("Excellent", 0xFF06B6D4)
}

data class PasswordStrength(
    val score: Int, // 0 to 100
    val level: StrengthLevel,
    val entropyBits: Int,
    val suggestions: List<String>
)

object PasswordStrengthEvaluator {

    fun evaluate(password: String): PasswordStrength {
        if (password.isEmpty()) {
            return PasswordStrength(0, StrengthLevel.VERY_WEAK, 0, listOf("Enter a password"))
        }

        var poolSize = 0
        var hasUpper = false
        var hasLower = false
        var hasDigit = false
        var hasSpecial = false

        for (c in password) {
            when {
                c.isUpperCase() -> hasUpper = true
                c.isLowerCase() -> hasLower = true
                c.isDigit() -> hasDigit = true
                else -> hasSpecial = true
            }
        }

        if (hasUpper) poolSize += 26
        if (hasLower) poolSize += 26
        if (hasDigit) poolSize += 10
        if (hasSpecial) poolSize += 32

        // Entropy in bits = length * log2(poolSize)
        val entropy = if (poolSize > 0) {
            (password.length * (ln(poolSize.toDouble()) / ln(2.0))).roundToInt()
        } else 0

        val suggestions = mutableListOf<String>()
        if (password.length < 12) suggestions.add("Use at least 12 characters")
        if (!hasUpper) suggestions.add("Add uppercase letters (A-Z)")
        if (!hasLower) suggestions.add("Add lowercase letters (a-z)")
        if (!hasDigit) suggestions.add("Add numbers (0-9)")
        if (!hasSpecial) suggestions.add("Add special symbols (!@#$)")

        // Scoring 0..100
        var score = (entropy * 1.2).toInt().coerceIn(0, 100)
        if (password.length < 8) score = score.coerceAtMost(25)

        val level = when {
            score >= 80 -> StrengthLevel.EXCELLENT
            score >= 60 -> StrengthLevel.STRONG
            score >= 40 -> StrengthLevel.FAIR
            score >= 25 -> StrengthLevel.WEAK
            else -> StrengthLevel.VERY_WEAK
        }

        return PasswordStrength(
            score = score,
            level = level,
            entropyBits = entropy,
            suggestions = suggestions
        )
    }
}

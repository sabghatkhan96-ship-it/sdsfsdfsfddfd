package com.securevault.app.data.model

data class Credential(
    val id: Long = 0,
    val title: String,
    val domainOrPackage: String,
    val username: String,
    val password: String,
    val notes: String = "",
    val category: Category = Category.LOGINS,
    val isFavorite: Boolean = false,
    val requiresBiometricForAutofill: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Checks if this credential matches a given target app package or web domain.
     * Handles clean domains (e.g. "github.com" matches "https://github.com/login" or "github.com")
     */
    fun matchesTarget(target: String): Boolean {
        if (target.isBlank() || domainOrPackage.isBlank()) return false
        val cleanDomain = domainOrPackage.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/").first()

        val cleanTarget = target.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/").first()

        return cleanDomain == cleanTarget ||
                cleanTarget.endsWith(".$cleanDomain") ||
                cleanDomain.endsWith(".$cleanTarget") ||
                domainOrPackage.equals(target, ignoreCase = true)
    }
}

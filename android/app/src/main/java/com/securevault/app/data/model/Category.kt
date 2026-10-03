package com.securevault.app.data.model

enum class Category(val displayName: String, val iconEmoji: String) {
    ALL("All", "📁"),
    LOGINS("Logins", "🔑"),
    SOCIAL("Social", "💬"),
    FINANCE("Finance", "💳"),
    WORK("Work", "💼"),
    PERSONAL("Personal", "👤");

    companion object {
        fun fromString(value: String): Category {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: LOGINS
        }
    }
}

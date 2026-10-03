package com.securevault.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credentials")
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val domainOrPackage: String,
    val usernameEncrypted: String,
    val passwordEncrypted: String,
    val notesEncrypted: String,
    val iv: String,
    val category: String,
    val isFavorite: Boolean,
    val requiresBiometricForAutofill: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

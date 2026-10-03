package com.securevault.app.data.model

data class VaultHealth(
    val totalCredentials: Int,
    val securityScore: Int, // 0 - 100
    val weakCount: Int,
    val reusedCount: Int,
    val strongCount: Int,
    val weakCredentials: List<Credential>,
    val reusedCredentials: List<Credential>
)

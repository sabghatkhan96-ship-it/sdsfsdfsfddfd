package com.securevault.app.ui.screens.audit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securevault.app.data.model.Credential
import com.securevault.app.ui.theme.CardBorder
import com.securevault.app.ui.theme.SecurityGreen
import com.securevault.app.ui.theme.SecurityOrange
import com.securevault.app.ui.theme.SecurityRed
import com.securevault.app.ui.theme.SecurityYellow
import com.securevault.app.ui.theme.VaultBackground
import com.securevault.app.ui.theme.VaultPrimary
import com.securevault.app.ui.theme.VaultSurface
import com.securevault.app.ui.theme.VaultSurfaceHighlight
import com.securevault.app.ui.theme.VaultTextMuted
import com.securevault.app.ui.theme.VaultTextPrimary
import com.securevault.app.ui.theme.VaultTextSecondary
import com.securevault.app.ui.viewmodel.VaultViewModel

@Composable
fun SecurityAuditScreen(
    vaultViewModel: VaultViewModel,
    onNavigateToCredential: (Credential) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by vaultViewModel.uiState.collectAsState()
    val health = uiState.vaultHealth

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VaultBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Security Audit",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VaultTextPrimary
                    )
                )
                Text(
                    text = "Evaluate credential health and vulnerability exposure",
                    style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary)
                )
            }

            // Health Score Card
            val score = health?.securityScore ?: 100
            val scoreColor = when {
                score >= 80 -> SecurityGreen
                score >= 50 -> SecurityYellow
                else -> SecurityRed
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(VaultSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vault Health Score", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                score >= 80 -> "Excellent protection! All credentials meet strong security standards."
                                score >= 50 -> "Fair protection. Some credentials are weak or reused."
                                else -> "Critical attention needed. Weak or reused passwords detected."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary)
                        )
                    }

                    // Score Circle
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(scoreColor.copy(alpha = 0.15f))
                            .border(2.dp, scoreColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$score%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = scoreColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                }
            }

            // Statistics Grid (Total, Weak, Reused, Strong)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AuditStatCard(
                    title = "Total",
                    count = health?.totalCredentials ?: 0,
                    color = VaultPrimary,
                    modifier = Modifier.weight(1f)
                )
                AuditStatCard(
                    title = "Strong",
                    count = health?.strongCount ?: 0,
                    color = SecurityGreen,
                    modifier = Modifier.weight(1f)
                )
                AuditStatCard(
                    title = "Weak",
                    count = health?.weakCount ?: 0,
                    color = SecurityOrange,
                    modifier = Modifier.weight(1f)
                )
                AuditStatCard(
                    title = "Reused",
                    count = health?.reusedCount ?: 0,
                    color = SecurityRed,
                    modifier = Modifier.weight(1f)
                )
            }

            // Weak Passwords Section
            if (!health?.weakCredentials.isNullOrEmpty()) {
                Text(
                    text = "Weak Passwords (${health?.weakCount})",
                    style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontWeight = FontWeight.SemiBold)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    health!!.weakCredentials.forEach { cred ->
                        AuditIssueItem(
                            credential = cred,
                            issue = "Password too short or lacks complexity",
                            issueColor = SecurityOrange,
                            onClick = { onNavigateToCredential(cred) }
                        )
                    }
                }
            }

            // Reused Passwords Section
            if (!health?.reusedCredentials.isNullOrEmpty()) {
                Text(
                    text = "Reused Across Multiple Accounts (${health?.reusedCount})",
                    style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontWeight = FontWeight.SemiBold)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    health!!.reusedCredentials.forEach { cred ->
                        AuditIssueItem(
                            credential = cred,
                            issue = "Identical password used on another service",
                            issueColor = SecurityRed,
                            onClick = { onNavigateToCredential(cred) }
                        )
                    }
                }
            }

            // Recommendations Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VaultSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Security Recommendations", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontWeight = FontWeight.SemiBold))
                    }
                    Text("• Never reuse the same password across multiple platforms. If one site suffers a breach, credential stuffers can compromise all your linked accounts.", style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary))
                    Text("• Use 16+ characters with a mixture of numbers and symbols.", style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary))
                    Text("• Ensure Biometric Verification is enabled for system autofill to protect against unauthorized device access.", style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AuditStatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(VaultSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, style = MaterialTheme.typography.labelSmall.copy(color = VaultTextMuted))
        }
    }
}

@Composable
private fun AuditIssueItem(
    credential: Credential,
    issue: String,
    issueColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VaultSurface)
            .border(1.dp, issueColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(credential.title, style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(2.dp))
                Text(issue, style = MaterialTheme.typography.bodyMedium.copy(color = issueColor))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Fix", tint = VaultTextMuted)
        }
    }
}

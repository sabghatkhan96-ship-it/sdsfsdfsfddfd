package com.securevault.app.ui.screens.vault

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securevault.app.data.model.Credential
import com.securevault.app.security.PasswordStrengthEvaluator
import com.securevault.app.ui.theme.CardBorder
import com.securevault.app.ui.theme.SecurityRed
import com.securevault.app.ui.theme.SecurityYellow
import com.securevault.app.ui.theme.VaultBackground
import com.securevault.app.ui.theme.VaultPrimary
import com.securevault.app.ui.theme.VaultSurface
import com.securevault.app.ui.theme.VaultSurfaceHighlight
import com.securevault.app.ui.theme.VaultSurfaceVariant
import com.securevault.app.ui.theme.VaultTextMuted
import com.securevault.app.ui.theme.VaultTextPrimary
import com.securevault.app.ui.theme.VaultTextSecondary
import com.securevault.app.ui.viewmodel.VaultViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialDetailScreen(
    credential: Credential,
    vaultViewModel: VaultViewModel,
    onBack: () -> Unit,
    onEdit: (Credential) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by vaultViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentCredential by remember { mutableStateOf(credential) }

    val strength = remember(currentCredential.password) {
        PasswordStrengthEvaluator.evaluate(currentCredential.password)
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            vaultViewModel.clearToastMessage()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Credential?", color = VaultTextPrimary) },
            text = { Text("This will permanently remove '${currentCredential.title}' from your encrypted vault.", color = VaultTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        vaultViewModel.deleteCredential(currentCredential) {
                            onBack()
                        }
                    }
                ) {
                    Text("Delete", color = SecurityRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = VaultTextSecondary)
                }
            },
            containerColor = VaultSurface
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VaultBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Credential Details", color = VaultTextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VaultTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val updated = currentCredential.copy(isFavorite = !currentCredential.isFavorite)
                        currentCredential = updated
                        vaultViewModel.saveCredential(updated)
                    }) {
                        Icon(
                            imageVector = if (currentCredential.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (currentCredential.isFavorite) SecurityYellow else VaultTextSecondary
                        )
                    }
                    IconButton(onClick = { onEdit(currentCredential) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VaultPrimary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SecurityRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VaultBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VaultSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(VaultSurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentCredential.title.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = VaultPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = currentCredential.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = VaultTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(currentCredential.category.iconEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentCredential.category.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(color = VaultPrimary)
                            )
                        }
                    }
                }
            }

            // Domain / Package Card
            DetailFieldCard(
                label = "Website URL / App Package",
                value = currentCredential.domainOrPackage.ifBlank { "Not specified" },
                onCopy = if (currentCredential.domainOrPackage.isNotBlank()) {
                    { vaultViewModel.copyUsername(currentCredential.domainOrPackage) }
                } else null,
                actionIcon = if (currentCredential.domainOrPackage.contains(".")) Icons.Default.OpenInBrowser else null,
                onAction = if (currentCredential.domainOrPackage.contains(".")) {
                    {
                        val url = if (!currentCredential.domainOrPackage.startsWith("http"))
                            "https://${currentCredential.domainOrPackage}"
                        else currentCredential.domainOrPackage
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {}
                    }
                } else null
            )

            // Username Card
            DetailFieldCard(
                label = "Username / Email",
                value = currentCredential.username,
                onCopy = { vaultViewModel.copyUsername(currentCredential.username) }
            )

            // Password Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(VaultSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Password", style = MaterialTheme.typography.labelMedium.copy(color = VaultTextMuted))
                        Row {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = VaultTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { vaultViewModel.copyPassword(currentCredential.password) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Password",
                                    tint = VaultPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPasswordVisible) currentCredential.password else "•".repeat(currentCredential.password.length.coerceAtMost(24)),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = VaultTextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strength gauge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Strength: ${strength.level.label} (${strength.entropyBits} bits)",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(strength.level.colorHex))
                        )
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(VaultSurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (strength.score / 100f).coerceIn(0.1f, 1f))
                                    .height(6.dp)
                                    .background(Color(strength.level.colorHex))
                            )
                        }
                    }
                }
            }

            // Autofill Security Setting Switch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(VaultSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VaultPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Require Biometrics for Autofill",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = VaultTextPrimary,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                "Requires fingerprint/Face ID before autofilling in apps",
                                style = MaterialTheme.typography.labelSmall.copy(color = VaultTextMuted)
                            )
                        }
                    }

                    Switch(
                        checked = currentCredential.requiresBiometricForAutofill,
                        onCheckedChange = { checked ->
                            val updated = currentCredential.copy(requiresBiometricForAutofill = checked)
                            currentCredential = updated
                            vaultViewModel.saveCredential(updated)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VaultBackground,
                            checkedTrackColor = VaultPrimary,
                            uncheckedThumbColor = VaultTextMuted,
                            uncheckedTrackColor = VaultSurfaceVariant
                        )
                    )
                }
            }

            // Secure Notes Card
            if (currentCredential.notes.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VaultSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Encrypted Notes", style = MaterialTheme.typography.labelMedium.copy(color = VaultTextMuted))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(currentCredential.notes, style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextPrimary))
                    }
                }
            }

            // Timestamp Info
            val sdf = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = "Created: ${sdf.format(Date(currentCredential.createdAt))}",
                    style = MaterialTheme.typography.labelSmall.copy(color = VaultTextMuted)
                )
                Text(
                    text = "Last Modified: ${sdf.format(Date(currentCredential.updatedAt))}",
                    style = MaterialTheme.typography.labelSmall.copy(color = VaultTextMuted)
                )
            }
        }
    }
}

@Composable
private fun DetailFieldCard(
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VaultSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium.copy(color = VaultTextMuted))
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary))
            }

            Row {
                if (actionIcon != null && onAction != null) {
                    IconButton(onClick = onAction, modifier = Modifier.size(36.dp)) {
                        Icon(actionIcon, contentDescription = null, tint = VaultSecondary, modifier = Modifier.size(20.dp))
                    }
                }
                if (onCopy != null) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = VaultPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

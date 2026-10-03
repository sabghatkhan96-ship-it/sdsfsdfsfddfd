package com.securevault.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.autofill.AutofillManager
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securevault.app.data.repository.VaultSettingsRepository
import com.securevault.app.ui.theme.CardBorder
import com.securevault.app.ui.theme.SecurityGreen
import com.securevault.app.ui.theme.SecurityYellow
import com.securevault.app.ui.theme.VaultBackground
import com.securevault.app.ui.theme.VaultPrimary
import com.securevault.app.ui.theme.VaultSecondary
import com.securevault.app.ui.theme.VaultSurface
import com.securevault.app.ui.theme.VaultSurfaceHighlight
import com.securevault.app.ui.theme.VaultSurfaceVariant
import com.securevault.app.ui.theme.VaultTextMuted
import com.securevault.app.ui.theme.VaultTextPrimary
import com.securevault.app.ui.theme.VaultTextSecondary

@Composable
fun SettingsScreen(
    settingsRepository: VaultSettingsRepository,
    onLockVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val biometricEnabled by settingsRepository.biometricEnabled.collectAsState()
    val autoLockSeconds by settingsRepository.autoLockSeconds.collectAsState()
    val clipboardClearSeconds by settingsRepository.clipboardClearSeconds.collectAsState()
    val flagSecureEnabled by settingsRepository.flagSecureEnabled.collectAsState()

    var isAutofillActive by remember {
        val am = context.getSystemService(Context.AUTOFILL_MANAGER_SERVICE) as? AutofillManager
        mutableStateOf(am?.hasEnabledAutofillServices() == true)
    }

    var showAutoLockDropdown by remember { mutableStateOf(false) }
    var showClipboardDropdown by remember { mutableStateOf(false) }

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
                    text = "Security & Autofill",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VaultTextPrimary
                    )
                )
                Text(
                    text = "Autofill service, encryption keys, and privacy controls",
                    style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary)
                )
            }

            // Official Android Autofill Service Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VaultSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(VaultPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Android Autofill Service", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontWeight = FontWeight.Bold))
                                Text(
                                    if (isAutofillActive) "Enabled & Active" else "Not yet enabled",
                                    style = MaterialTheme.typography.labelSmall.copy(color = if (isAutofillActive) SecurityGreen else SecurityYellow)
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isAutofillActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isAutofillActive) SecurityGreen else SecurityYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "SecureVault connects to Android's official Autofill Framework (android.service.autofill). When you tap username or password fields in any supported app or browser, the system will offer suggestions directly from your encrypted vault.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary)
                    )

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isAutofillActive) VaultSurfaceHighlight else VaultPrimary)
                    ) {
                        Text(
                            text = if (isAutofillActive) "Manage System Autofill Settings" else "Enable SecureVault Autofill",
                            color = if (isAutofillActive) VaultTextPrimary else VaultBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Security Preferences Section
            Text("Vault Security", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontWeight = FontWeight.SemiBold))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VaultSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Biometric Unlock Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric Authentication", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontSize = 15.sp))
                            Text("Unlock vault with Face ID, Fingerprint, or Device PIN", style = MaterialTheme.typography.labelSmall.copy(color = VaultTextMuted))
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { settingsRepository.setBiometricEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VaultBackground,
                                checkedTrackColor = VaultPrimary,
                                uncheckedThumbColor = VaultTextMuted,
                                uncheckedTrackColor = VaultSurfaceVariant
                            )
                        )
                    }

                    // Auto-Lock Inactivity Setting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAutoLockDropdown = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Lock Vault", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontSize = 15.sp))
                            val label = when (autoLockSeconds) {
                                0 -> "Immediately on background"
                                60 -> "After 1 minute"
                                300 -> "After 5 minutes"
                                900 -> "After 15 minutes"
                                else -> "After ${autoLockSeconds}s"
                            }
                            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = VaultPrimary))
                        }

                        Box {
                            Text("Change", style = MaterialTheme.typography.labelMedium.copy(color = VaultSecondary, fontWeight = FontWeight.Bold))
                            DropdownMenu(
                                expanded = showAutoLockDropdown,
                                onDismissRequest = { showAutoLockDropdown = false }
                            ) {
                                DropdownMenuItem(text = { Text("Immediately on background") }, onClick = { settingsRepository.setAutoLockSeconds(0); showAutoLockDropdown = false })
                                DropdownMenuItem(text = { Text("After 1 minute") }, onClick = { settingsRepository.setAutoLockSeconds(60); showAutoLockDropdown = false })
                                DropdownMenuItem(text = { Text("After 5 minutes") }, onClick = { settingsRepository.setAutoLockSeconds(300); showAutoLockDropdown = false })
                                DropdownMenuItem(text = { Text("After 15 minutes") }, onClick = { settingsRepository.setAutoLockSeconds(900); showAutoLockDropdown = false })
                            }
                        }
                    }

                    // Clipboard Auto-Clear Setting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClipboardDropdown = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Clear Clipboard", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontSize = 15.sp))
                            val label = when (clipboardClearSeconds) {
                                0 -> "Disabled (Do not auto-clear)"
                                15 -> "After 15 seconds"
                                30 -> "After 30 seconds"
                                60 -> "After 60 seconds"
                                else -> "After ${clipboardClearSeconds}s"
                            }
                            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = VaultPrimary))
                        }

                        Box {
                            Text("Change", style = MaterialTheme.typography.labelMedium.copy(color = VaultSecondary, fontWeight = FontWeight.Bold))
                            DropdownMenu(
                                expanded = showClipboardDropdown,
                                onDismissRequest = { showClipboardDropdown = false }
                            ) {
                                DropdownMenuItem(text = { Text("After 15 seconds") }, onClick = { settingsRepository.setClipboardClearSeconds(15); showClipboardDropdown = false })
                                DropdownMenuItem(text = { Text("After 30 seconds") }, onClick = { settingsRepository.setClipboardClearSeconds(30); showClipboardDropdown = false })
                                DropdownMenuItem(text = { Text("After 60 seconds") }, onClick = { settingsRepository.setClipboardClearSeconds(60); showClipboardDropdown = false })
                                DropdownMenuItem(text = { Text("Disabled") }, onClick = { settingsRepository.setClipboardClearSeconds(0); showClipboardDropdown = false })
                            }
                        }
                    }

                    // FLAG_SECURE Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Screen Capture Protection", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontSize = 15.sp))
                            Text("Blocks screenshots and hide app preview in recent apps", style = MaterialTheme.typography.labelSmall.copy(color = VaultTextMuted))
                        }
                        Switch(
                            checked = flagSecureEnabled,
                            onCheckedChange = { settingsRepository.setFlagSecureEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VaultBackground,
                                checkedTrackColor = VaultPrimary,
                                uncheckedThumbColor = VaultTextMuted,
                                uncheckedTrackColor = VaultSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Zero-Knowledge & Privacy Manifesto Card
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
                        Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Privacy & Security Guarantee", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary, fontWeight = FontWeight.Bold))
                    }
                    Text("• 100% Offline: SecureVault does NOT request the internet permission (android.permission.INTERNET). Your passwords physically cannot be uploaded or sent anywhere.", style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary))
                    Text("• Zero Scraping: We never access, log, or extract passwords from other apps without your explicit action. The official Android Autofill Framework only triggers when you interact with an input field.", style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary))
                    Text("• AES-256-GCM Hardware Encryption: All credentials stored in Room DB are encrypted using Android Keystore keys generated in your device's Secure Element (TEE/StrongBox).", style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary))
                }
            }

            // Lock Vault Now Button
            Button(
                onClick = onLockVault,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VaultSurfaceVariant)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = VaultTextPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock Vault Now", color = VaultTextPrimary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

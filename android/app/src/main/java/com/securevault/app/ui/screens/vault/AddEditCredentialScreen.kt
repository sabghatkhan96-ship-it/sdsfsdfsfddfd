package com.securevault.app.ui.screens.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securevault.app.data.model.Category
import com.securevault.app.data.model.Credential
import com.securevault.app.security.GeneratorOptions
import com.securevault.app.security.PasswordGenerator
import com.securevault.app.security.PasswordStrengthEvaluator
import com.securevault.app.ui.theme.CardBorder
import com.securevault.app.ui.theme.SecurityRed
import com.securevault.app.ui.theme.VaultBackground
import com.securevault.app.ui.theme.VaultPrimary
import com.securevault.app.ui.theme.VaultSurface
import com.securevault.app.ui.theme.VaultSurfaceVariant
import com.securevault.app.ui.theme.VaultTextMuted
import com.securevault.app.ui.theme.VaultTextPrimary
import com.securevault.app.ui.theme.VaultTextSecondary
import com.securevault.app.ui.viewmodel.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCredentialScreen(
    existingCredential: Credential? = null,
    vaultViewModel: VaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(existingCredential?.title ?: "") }
    var domainOrPackage by remember { mutableStateOf(existingCredential?.domainOrPackage ?: "") }
    var username by remember { mutableStateOf(existingCredential?.username ?: "") }
    var password by remember { mutableStateOf(existingCredential?.password ?: "") }
    var notes by remember { mutableStateOf(existingCredential?.notes ?: "") }
    var category by remember { mutableStateOf(existingCredential?.category ?: Category.LOGINS) }
    var requiresBiometric by remember { mutableStateOf(existingCredential?.requiresBiometricForAutofill ?: true) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val passwordGenerator = remember { PasswordGenerator() }
    val strength = remember(password) { PasswordStrengthEvaluator.evaluate(password) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VaultBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingCredential == null) "New Credential" else "Edit Credential",
                        color = VaultTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VaultTextPrimary)
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Input
            VaultInputField(
                label = "Service / App Name *",
                value = title,
                onValueChange = { title = it; validationError = null },
                placeholder = "e.g. GitHub, Google, Netflix"
            )

            // Domain or Package Input
            VaultInputField(
                label = "Website Domain or App Package Name *",
                value = domainOrPackage,
                onValueChange = { domainOrPackage = it; validationError = null },
                placeholder = "e.g. github.com, accounts.google.com"
            )

            // Username Input
            VaultInputField(
                label = "Username / Email *",
                value = username,
                onValueChange = { username = it; validationError = null },
                placeholder = "e.g. user@example.com"
            )

            // Password Input with generator shortcut
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Password *",
                        style = MaterialTheme.typography.labelMedium.copy(color = VaultTextMuted)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(VaultPrimary.copy(alpha = 0.12f))
                            .clickable {
                                password = passwordGenerator.generate(GeneratorOptions(length = 18))
                                isPasswordVisible = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generate", style = MaterialTheme.typography.labelSmall.copy(color = VaultPrimary, fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; validationError = null },
                    placeholder = { Text("Enter or generate password", color = VaultTextMuted) },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = VaultTextMuted
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VaultSurface,
                        unfocusedContainerColor = VaultSurface,
                        focusedBorderColor = VaultPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = VaultTextPrimary,
                        unfocusedTextColor = VaultTextPrimary
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    singleLine = true
                )

                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${strength.level.label} (${strength.entropyBits} bits entropy)",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(strength.level.colorHex))
                        )
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(VaultSurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (strength.score / 100f).coerceIn(0.1f, 1f))
                                    .height(5.dp)
                                    .background(Color(strength.level.colorHex))
                            )
                        }
                    }
                }
            }

            // Category Picker
            Column {
                Text("Category", style = MaterialTheme.typography.labelMedium.copy(color = VaultTextMuted))
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    val selectableCategories = Category.entries.filter { it != Category.ALL }
                    items(selectableCategories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VaultPrimary.copy(alpha = 0.2f) else VaultSurface)
                                .border(1.dp, if (isSelected) VaultPrimary else CardBorder, RoundedCornerShape(12.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat.iconEmoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    cat.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) VaultPrimary else VaultTextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Autofill Security Requirement Switch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(VaultSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VaultPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Require Biometrics for Autofill",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, color = VaultTextPrimary)
                            )
                            Text(
                                "Must authenticate with Face/Fingerprint before filling",
                                style = MaterialTheme.typography.labelSmall.copy(color = VaultTextMuted)
                            )
                        }
                    }

                    Switch(
                        checked = requiresBiometric,
                        onCheckedChange = { requiresBiometric = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VaultBackground,
                            checkedTrackColor = VaultPrimary,
                            uncheckedThumbColor = VaultTextMuted,
                            uncheckedTrackColor = VaultSurfaceVariant
                        )
                    )
                }
            }

            // Notes Input
            Column {
                Text("Secure Notes (Optional)", style = MaterialTheme.typography.labelMedium.copy(color = VaultTextMuted))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add security questions, PINs, or recovery codes...", color = VaultTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VaultSurface,
                        unfocusedContainerColor = VaultSurface,
                        focusedBorderColor = VaultPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = VaultTextPrimary,
                        unfocusedTextColor = VaultTextPrimary
                    )
                )
            }

            // Error notice
            if (validationError != null) {
                Text(
                    text = validationError ?: "",
                    color = SecurityRed,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        validationError = "Please enter a title (e.g. GitHub)"
                        return@Button
                    }
                    if (domainOrPackage.isBlank()) {
                        validationError = "Please enter a domain or package name for autofill"
                        return@Button
                    }
                    if (username.isBlank()) {
                        validationError = "Please enter username or email"
                        return@Button
                    }
                    if (password.isBlank()) {
                        validationError = "Please enter or generate a password"
                        return@Button
                    }

                    val toSave = (existingCredential ?: Credential(
                        title = title.trim(),
                        domainOrPackage = domainOrPackage.trim(),
                        username = username.trim(),
                        password = password,
                        notes = notes.trim(),
                        category = category,
                        requiresBiometricForAutofill = requiresBiometric
                    )).copy(
                        title = title.trim(),
                        domainOrPackage = domainOrPackage.trim(),
                        username = username.trim(),
                        password = password,
                        notes = notes.trim(),
                        category = category,
                        requiresBiometricForAutofill = requiresBiometric,
                        updatedAt = System.currentTimeMillis()
                    )

                    vaultViewModel.saveCredential(toSave) {
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = VaultBackground, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingCredential == null) "Encrypt & Save to Vault" else "Update Encrypted Credential",
                    style = MaterialTheme.typography.titleMedium.copy(color = VaultBackground, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VaultInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(color = VaultTextMuted))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = VaultTextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VaultSurface,
                unfocusedContainerColor = VaultSurface,
                focusedBorderColor = VaultPrimary,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = VaultTextPrimary,
                unfocusedTextColor = VaultTextPrimary
            ),
            singleLine = true
        )
    }
}

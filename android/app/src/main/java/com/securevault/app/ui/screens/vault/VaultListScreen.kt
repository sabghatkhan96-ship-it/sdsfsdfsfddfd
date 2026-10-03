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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securevault.app.data.model.Category
import com.securevault.app.data.model.Credential
import com.securevault.app.ui.theme.CardBorder
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
import com.securevault.app.ui.viewmodel.VaultViewModel

@Composable
fun VaultListScreen(
    vaultViewModel: VaultViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Credential) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by vaultViewModel.uiState.collectAsState()
    val credentials by vaultViewModel.credentials.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            vaultViewModel.clearToastMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VaultBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = VaultPrimary,
                contentColor = VaultBackground,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Credential", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "My Passwords",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = VaultTextPrimary
                            )
                        )
                        Text(
                            text = "${credentials.size} items protected",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = VaultPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VaultSurfaceVariant)
                            .border(1.dp, CardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Encrypted",
                            tint = VaultPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search field
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { vaultViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search logins, apps, or websites...", color = VaultTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = VaultTextMuted)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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

            // Category Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Category.entries) { category ->
                    val isSelected = uiState.selectedCategory == category
                    CategoryChip(
                        category = category,
                        isSelected = isSelected,
                        onClick = { vaultViewModel.setSelectedCategory(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Credentials List or Empty State
            if (credentials.isEmpty()) {
                EmptyVaultState(
                    isSearching = uiState.searchQuery.isNotBlank(),
                    onAddClick = onNavigateToAdd
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 84.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(credentials, key = { it.id }) { credential ->
                        CredentialCard(
                            credential = credential,
                            onClick = { onNavigateToDetail(credential) },
                            onCopyUsername = { vaultViewModel.copyUsername(credential.username) },
                            onCopyPassword = { vaultViewModel.copyPassword(credential.password) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgBrush = if (isSelected) {
        Brush.horizontalGradient(listOf(VaultPrimary, VaultSecondary))
    } else {
        Brush.horizontalGradient(listOf(VaultSurface, VaultSurface))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgBrush)
            .border(
                1.dp,
                if (isSelected) VaultPrimary else CardBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(category.iconEmoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) VaultBackground else VaultTextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun CredentialCard(
    credential: Credential,
    onClick: () -> Unit,
    onCopyUsername: () -> Unit,
    onCopyPassword: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VaultSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon + Titles
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Avatar with Initial
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultSurfaceHighlight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = credential.title.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = VaultPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = credential.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = VaultTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (credential.isFavorite) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = SecurityYellow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = credential.username,
                        style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (credential.requiresBiometricForAutofill) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = "Biometric Required",
                                tint = VaultPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Biometric Autofill",
                                style = MaterialTheme.typography.labelSmall.copy(color = VaultPrimary)
                            )
                        } else {
                            Text(
                                text = credential.domainOrPackage,
                                style = MaterialTheme.typography.labelSmall.copy(color = VaultTextMuted),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Quick Copy Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Copy Username
                IconButton(
                    onClick = onCopyUsername,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Username",
                        tint = VaultTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Copy Password with lock accent
                IconButton(
                    onClick = onCopyPassword,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(VaultPrimary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Copy Password",
                        tint = VaultPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyVaultState(
    isSearching: Boolean,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(VaultSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = VaultPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isSearching) "No matching credentials" else "Your Vault is Empty",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = VaultTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearching) "Try a different search keyword or category"
            else "Save credentials to enable one-tap biometric autofill across your apps and websites.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = VaultTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        )

        if (!isSearching) {
            Spacer(modifier = Modifier.height(20.dp))
            androidx.compose.material3.Button(
                onClick = onAddClick,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Your First Login", color = VaultBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

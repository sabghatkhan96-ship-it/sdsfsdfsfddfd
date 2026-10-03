package com.securevault.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securevault.app.data.model.Credential
import com.securevault.app.ui.screens.audit.SecurityAuditScreen
import com.securevault.app.ui.screens.generator.PasswordGeneratorScreen
import com.securevault.app.ui.screens.lock.LockScreen
import com.securevault.app.ui.screens.settings.SettingsScreen
import com.securevault.app.ui.screens.vault.AddEditCredentialScreen
import com.securevault.app.ui.screens.vault.CredentialDetailScreen
import com.securevault.app.ui.screens.vault.VaultListScreen
import com.securevault.app.ui.theme.CardBorder
import com.securevault.app.ui.theme.VaultBackground
import com.securevault.app.ui.theme.VaultPrimary
import com.securevault.app.ui.theme.VaultSurface
import com.securevault.app.ui.theme.VaultTextMuted
import com.securevault.app.ui.theme.VaultTextPrimary
import com.securevault.app.ui.viewmodel.AuthViewModel
import com.securevault.app.ui.viewmodel.GeneratorViewModel
import com.securevault.app.ui.viewmodel.VaultViewModel

enum class NavItem(val label: String, val icon: ImageVector) {
    VAULT("Vault", Icons.Default.Lock),
    GENERATOR("Generator", Icons.Default.AutoAwesome),
    AUDIT("Audit", Icons.Default.HealthAndSafety),
    SETTINGS("Settings", Icons.Default.Settings)
}

sealed class Screen {
    data object MainTabs : Screen()
    data object AddCredential : Screen()
    data class EditCredential(val credential: Credential) : Screen()
    data class CredentialDetail(val credential: Credential) : Screen()
}

@Composable
fun VaultNavigation(
    authViewModel: AuthViewModel,
    vaultViewModel: VaultViewModel,
    generatorViewModel: GeneratorViewModel,
    settingsRepository: com.securevault.app.data.repository.VaultSettingsRepository,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.uiState.collectAsState()

    // Gate screen with Biometric / PIN Lock Screen if locked
    if (!authState.isUnlocked) {
        LockScreen(authViewModel = authViewModel, modifier = modifier)
        return
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainTabs) }
    var selectedTab by remember { mutableStateOf(NavItem.VAULT) }

    when (val screen = currentScreen) {
        is Screen.AddCredential -> {
            AddEditCredentialScreen(
                existingCredential = null,
                vaultViewModel = vaultViewModel,
                onBack = { currentScreen = Screen.MainTabs },
                modifier = modifier
            )
        }
        is Screen.EditCredential -> {
            AddEditCredentialScreen(
                existingCredential = screen.credential,
                vaultViewModel = vaultViewModel,
                onBack = { currentScreen = Screen.CredentialDetail(screen.credential) },
                modifier = modifier
            )
        }
        is Screen.CredentialDetail -> {
            CredentialDetailScreen(
                credential = screen.credential,
                vaultViewModel = vaultViewModel,
                onBack = { currentScreen = Screen.MainTabs },
                onEdit = { currentScreen = Screen.EditCredential(it) },
                modifier = modifier
            )
        }
        Screen.MainTabs -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = VaultBackground,
                bottomBar = {
                    NavigationBar(
                        containerColor = VaultSurface,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    ) {
                        NavItem.entries.forEach { item ->
                            val isSelected = selectedTab == item
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = item },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VaultBackground,
                                    selectedTextColor = VaultPrimary,
                                    indicatorColor = VaultPrimary,
                                    unselectedIconColor = VaultTextMuted,
                                    unselectedTextColor = VaultTextMuted
                                )
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (selectedTab) {
                        NavItem.VAULT -> {
                            VaultListScreen(
                                vaultViewModel = vaultViewModel,
                                onNavigateToAdd = { currentScreen = Screen.AddCredential },
                                onNavigateToDetail = { currentScreen = Screen.CredentialDetail(it) }
                            )
                        }
                        NavItem.GENERATOR -> {
                            PasswordGeneratorScreen(generatorViewModel = generatorViewModel)
                        }
                        NavItem.AUDIT -> {
                            SecurityAuditScreen(
                                vaultViewModel = vaultViewModel,
                                onNavigateToCredential = { currentScreen = Screen.CredentialDetail(it) }
                            )
                        }
                        NavItem.SETTINGS -> {
                            SettingsScreen(
                                settingsRepository = settingsRepository,
                                onLockVault = { authViewModel.lockVault() }
                            )
                        }
                    }
                }
            }
        }
    }
}

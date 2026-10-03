package com.securevault.app.ui.screens.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.securevault.app.ui.theme.SecurityRed
import com.securevault.app.ui.theme.VaultBackground
import com.securevault.app.ui.theme.VaultPrimary
import com.securevault.app.ui.theme.VaultSecondary
import com.securevault.app.ui.theme.VaultSurface
import com.securevault.app.ui.theme.VaultSurfaceVariant
import com.securevault.app.ui.theme.VaultTextMuted
import com.securevault.app.ui.theme.VaultTextPrimary
import com.securevault.app.ui.theme.VaultTextSecondary
import com.securevault.app.ui.viewmodel.AuthViewModel

@Composable
fun LockScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // Trigger biometric prompt automatically on launch if already setup
    LaunchedEffect(uiState.isSetup, uiState.isUnlocked) {
        if (uiState.isSetup && !uiState.isUnlocked && activity != null && uiState.isBiometricAvailable) {
            authViewModel.triggerBiometricUnlock(activity)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(VaultBackground, Color(0xFF070B12))
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(VaultPrimary.copy(alpha = 0.2f), VaultSecondary.copy(alpha = 0.1f))
                            )
                        )
                        .border(1.dp, VaultPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = VaultPrimary,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SecureVault",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = VaultTextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                val subtitleText = when {
                    !uiState.isSetup && !uiState.isConfirmingPin -> "Set up your 4-6 digit Master PIN"
                    !uiState.isSetup && uiState.isConfirmingPin -> "Confirm your Master PIN"
                    else -> "Enter Master PIN or use Biometrics"
                }

                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = VaultTextSecondary,
                        textAlign = TextAlign.Center
                    )
                )
            }

            // PIN Dots Indicator Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pinLength = uiState.enteredPin.length
                    for (i in 0 until 6) {
                        val isFilled = i < pinLength
                        val scale by animateFloatAsState(if (isFilled) 1.25f else 1.0f, label = "dotScale")

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) VaultPrimary else VaultSurfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isFilled) VaultPrimary else Color(0x33475569),
                                    CircleShape
                                )
                        )
                    }
                }

                // Error Message Banner
                AnimatedVisibility(visible = uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = SecurityRed,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                // First time setup confirmation button if 4+ digits
                if (!uiState.isSetup && !uiState.isConfirmingPin && uiState.enteredPin.length >= 4) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { authViewModel.proceedToConfirmPin() },
                        colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue", color = VaultBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Numeric Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (digit in row) {
                            KeypadButton(text = digit, onClick = { authViewModel.onPinDigit(digit) })
                        }
                    }
                }

                // Bottom row: Biometric / PIN button, 0, Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Biometric Trigger Button
                    Box(
                        modifier = Modifier.size(68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSetup && uiState.isBiometricAvailable && activity != null) {
                            IconButton(
                                onClick = { authViewModel.triggerBiometricUnlock(activity) },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(VaultPrimary.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Unlock",
                                    tint = VaultPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }

                    // Digit 0
                    KeypadButton(text = "0", onClick = { authViewModel.onPinDigit("0") })

                    // Backspace Button
                    Box(
                        modifier = Modifier.size(68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { authViewModel.onPinBackspace() },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Backspace",
                                tint = VaultTextMuted,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(VaultSurface)
            .border(1.dp, Color(0x22475569), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp,
                color = VaultTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

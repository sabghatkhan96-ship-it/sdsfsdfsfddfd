package com.securevault.app.ui.screens.generator

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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securevault.app.ui.theme.CardBorder
import com.securevault.app.ui.theme.VaultBackground
import com.securevault.app.ui.theme.VaultPrimary
import com.securevault.app.ui.theme.VaultSecondary
import com.securevault.app.ui.theme.VaultSurface
import com.securevault.app.ui.theme.VaultSurfaceHighlight
import com.securevault.app.ui.theme.VaultSurfaceVariant
import com.securevault.app.ui.theme.VaultTextMuted
import com.securevault.app.ui.theme.VaultTextPrimary
import com.securevault.app.ui.theme.VaultTextSecondary
import com.securevault.app.ui.viewmodel.GeneratorViewModel

@Composable
fun PasswordGeneratorScreen(
    generatorViewModel: GeneratorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by generatorViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            generatorViewModel.clearToastMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VaultBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            Column {
                Text(
                    text = "Password Generator",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VaultTextPrimary
                    )
                )
                Text(
                    text = "Generate cryptographically secure passwords & passphrases",
                    style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary)
                )
            }

            // Mode Tabs (Random vs Passphrase)
            TabRow(
                selectedTabIndex = if (uiState.isPassphraseMode) 1 else 0,
                containerColor = VaultSurface,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (uiState.isPassphraseMode) 1 else 0]),
                        color = VaultPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = !uiState.isPassphraseMode,
                    onClick = { generatorViewModel.setPassphraseMode(false) },
                    text = { Text("Password", color = if (!uiState.isPassphraseMode) VaultPrimary else VaultTextMuted, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.isPassphraseMode,
                    onClick = { generatorViewModel.setPassphraseMode(true) },
                    text = { Text("Passphrase", color = if (uiState.isPassphraseMode) VaultPrimary else VaultTextMuted, fontWeight = FontWeight.SemiBold) }
                )
            }

            // Result Display Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(VaultSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.generatedValue,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = VaultTextPrimary,
                            textAlign = TextAlign.Center,
                            fontSize = if (uiState.generatedValue.length > 24) 16.sp else 20.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strength Indicator Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${uiState.strength.level.label} • ${uiState.strength.entropyBits} bits entropy",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(uiState.strength.level.colorHex), fontWeight = FontWeight.SemiBold)
                        )
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(VaultSurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (uiState.strength.score / 100f).coerceIn(0.1f, 1f))
                                    .height(6.dp)
                                    .background(Color(uiState.strength.level.colorHex))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row: Refresh & Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { generatorViewModel.generate() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VaultSurfaceHighlight)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = VaultTextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate", color = VaultTextPrimary, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = { generatorViewModel.copyToClipboard() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = VaultBackground, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", color = VaultBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Controls Section
            if (!uiState.isPassphraseMode) {
                // Password Character Controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VaultSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Length Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Password Length", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary))
                                Text(
                                    "${uiState.options.length} characters",
                                    style = MaterialTheme.typography.titleMedium.copy(color = VaultPrimary, fontWeight = FontWeight.Bold)
                                )
                            }
                            Slider(
                                value = uiState.options.length.toFloat(),
                                onValueChange = { generatorViewModel.setLength(it.toInt()) },
                                valueRange = 8f..48f,
                                steps = 39,
                                colors = SliderDefaults.colors(
                                    thumbColor = VaultPrimary,
                                    activeTrackColor = VaultPrimary,
                                    inactiveTrackColor = VaultSurfaceVariant
                                )
                            )
                        }

                        GeneratorToggleRow(
                            label = "Uppercase Letters (A-Z)",
                            checked = uiState.options.includeUppercase,
                            onCheckedChange = { generatorViewModel.toggleUppercase() }
                        )

                        GeneratorToggleRow(
                            label = "Lowercase Letters (a-z)",
                            checked = uiState.options.includeLowercase,
                            onCheckedChange = { generatorViewModel.toggleLowercase() }
                        )

                        GeneratorToggleRow(
                            label = "Numbers (0-9)",
                            checked = uiState.options.includeDigits,
                            onCheckedChange = { generatorViewModel.toggleDigits() }
                        )

                        GeneratorToggleRow(
                            label = "Special Symbols (!@#$)",
                            checked = uiState.options.includeSymbols,
                            onCheckedChange = { generatorViewModel.toggleSymbols() }
                        )

                        GeneratorToggleRow(
                            label = "Exclude Ambiguous (l, 1, O, 0)",
                            checked = uiState.options.excludeAmbiguous,
                            onCheckedChange = { generatorViewModel.toggleExcludeAmbiguous() }
                        )
                    }
                }
            } else {
                // Passphrase Controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VaultSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Word Count", style = MaterialTheme.typography.titleMedium.copy(color = VaultTextPrimary))
                            Text(
                                "${uiState.passphraseWords} words",
                                style = MaterialTheme.typography.titleMedium.copy(color = VaultPrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                        Slider(
                            value = uiState.passphraseWords.toFloat(),
                            onValueChange = { generatorViewModel.setPassphraseWords(it.toInt()) },
                            valueRange = 3f..8f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = VaultPrimary,
                                activeTrackColor = VaultPrimary,
                                inactiveTrackColor = VaultSurfaceVariant
                            )
                        )
                        Text(
                            text = "Passphrases use easy-to-remember words combined with hyphens. Highly secure against brute-force attacks and simple to type on mobile devices.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = VaultTextSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GeneratorToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(color = VaultTextPrimary))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VaultBackground,
                checkedTrackColor = VaultPrimary,
                uncheckedThumbColor = VaultTextMuted,
                uncheckedTrackColor = VaultSurfaceVariant
            )
        )
    }
}

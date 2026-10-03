package com.securevault.app.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.securevault.app.SecureVaultApp
import com.securevault.app.ui.navigation.VaultNavigation
import com.securevault.app.ui.theme.SecureVaultTheme
import com.securevault.app.ui.viewmodel.AuthViewModel
import com.securevault.app.ui.viewmodel.GeneratorViewModel
import com.securevault.app.ui.viewmodel.VaultViewModel
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private lateinit var app: SecureVaultApp
    private lateinit var authViewModel: AuthViewModel
    private lateinit var vaultViewModel: VaultViewModel
    private lateinit var generatorViewModel: GeneratorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as SecureVaultApp

        // Listen for FLAG_SECURE setting to prevent screen capture & recents snapshot preview
        lifecycleScope.launch {
            app.vaultSettingsRepository.flagSecureEnabled.collect { isSecure ->
                if (isSecure) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        authViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(app) as T
            }
        })[AuthViewModel::class.java]

        vaultViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VaultViewModel(app) as T
            }
        })[VaultViewModel::class.java]

        generatorViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GeneratorViewModel(app) as T
            }
        })[GeneratorViewModel::class.java]

        enableEdgeToEdge()

        setContent {
            SecureVaultTheme {
                VaultNavigation(
                    authViewModel = authViewModel,
                    vaultViewModel = vaultViewModel,
                    generatorViewModel = generatorViewModel,
                    settingsRepository = app.vaultSettingsRepository
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        app.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        app.onAppBackgrounded()
    }
}

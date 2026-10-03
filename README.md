# SecureVault - Native Android Password Manager & Autofill Service 🛡️📱

A production-grade, zero-knowledge native Android application built with **Kotlin**, **Jetpack Compose**, **AndroidKeyStore (AES-256-GCM)**, and the official Android **`AutofillService`** framework.

---

## 🔒 Core Security & Architecture

### 1. Official Android Autofill Service Framework
- Inherits directly from `android.service.autofill.AutofillService`.
- Intercepts system-mediated autofill intents when you tap login fields in any app (Chrome, WhatsApp, banking apps, etc.).
- Traverses the `AssistStructure` to extract package names and web domains (`viewNode.webDomain`).
- Matches stored credentials and securely injects them into `AUTOFILL_HINT_USERNAME` and `AUTOFILL_HINT_PASSWORD` fields.
- Saves new credentials via `onSaveRequest` upon user submission with explicit consent.

### 2. Biometric Authentication & Device Credential Gate
- Integrated with `androidx.biometric.BiometricPrompt` supporting:
  - **Fingerprint**
  - **Face Unlock**
  - **Device PIN / Pattern fallback**
- Per-credential biometric autofill gate (`requiresBiometricForAutofill`). Tapping an autofill item prompts for biometric confirmation before credentials decrypt.
- Master PIN derived with **PBKDF2WithHmacSHA256** (100,000 iterations + 16-byte random salt).

### 3. Hardware-Backed AES-256-GCM Encryption
- Master encryption keys generated and stored inside the device's hardware **Secure Element / Trusted Execution Environment (TEE/StrongBox)** via `AndroidKeyStore`.
- Authenticated encryption prevents ciphertext tampering or bit-flipping.
- In-memory data sanitization (`SecureMemory.wipe`) zeroizes byte and char arrays after use to protect against RAM scraping.

### 4. Zero-Knowledge & Anti-Extraction Guarantee
- **No Internet Access**: `android.permission.INTERNET` is completely absent from `AndroidManifest.xml`. It is mathematically and operating-system-enforced impossible for passwords to leave your device.
- **Screen Capture Protection**: `FLAG_SECURE` prevents screenshots, screen recording, and task-switcher snapshots from capturing passwords.
- **Sensitive Clipboard Auto-Clear**: Automatically wipes copied passwords after 30 seconds and tags data with `ClipDescription.EXTRA_IS_SENSITIVE` on Android 13+.

---

## 📂 Project Structure

```
├── .github/workflows/
│   └── build-apk.yml                           # Automated GitHub Actions APK builder
├── gradle/
│   ├── libs.versions.toml                      # Version catalog
│   └── wrapper/gradle-wrapper.properties       # Gradle 8.7 wrapper
├── app/
│   ├── build.gradle.kts                        # Compose, Room, Biometric & Crypto deps
│   ├── proguard-rules.pro                      # Security & crypto obfuscation rules
│   └── src/main/
│       ├── AndroidManifest.xml                 # Zero-internet permissions & AutofillService
│       ├── res/
│       │   ├── layout/
│       │   │   ├── autofill_dataset_item.xml   # RemoteViews dropdown layout
│       │   │   └── autofill_auth_prompt.xml    # RemoteViews biometric unlock prompt
│       │   ├── xml/
│       │   │   ├── autofill_service_config.xml # System autofill metadata
│       │   │   ├── data_extraction_rules.xml   # Disallows unencrypted cloud backups
│       │   │   └── backup_rules.xml            # Disallows unencrypted ADB backups
│       │   └── values/                         # Colors, themes, strings
│       └── java/com/securevault/app/
│           ├── SecureVaultApp.kt               # Application lifecycle & lock state
│           ├── data/
│           │   ├── crypto/
│           │   │   ├── CryptoManager.kt        # AndroidKeyStore AES-256-GCM cipher
│           │   │   ├── MasterKeyManager.kt     # PBKDF2WithHmacSHA256 PIN derivation
│           │   │   └── SecureMemory.kt         # In-memory RAM zero-wiping
│           │   ├── db/
│           │   │   ├── CredentialDao.kt        # Room DAO queries & flows
│           │   │   ├── CredentialEntity.kt     # Room entity with encrypted fields
│           │   │   └── VaultDatabase.kt        # Room database
│           │   ├── model/                      # Credential, Category, VaultHealth
│           │   └── repository/                 # VaultRepository & Settings
│           ├── security/
│           │   ├── BiometricAuthManager.kt     # BiometricPrompt wrapper
│           │   ├── ClipboardSecurityManager.kt # Auto-clearing clipboard
│           │   ├── PasswordGenerator.kt        # Cryptographic password generator
│           │   └── PasswordStrengthEvaluator.kt# Entropy & strength evaluation
│           ├── service/
│           │   ├── AutofillStructureParser.kt  # AssistStructure traversal
│           │   ├── VaultAutofillService.kt     # Official AutofillService
│           │   └── AutofillAuthActivity.kt     # Biometric gate for autofill
│           └── ui/
│               ├── MainActivity.kt             # FLAG_SECURE & Compose host
│               ├── theme/                      # Obsidian & Emerald Material3 palette
│               ├── navigation/                 # Bottom bar routing
│               ├── screens/                    # LockScreen, VaultList, Detail, Generator, Audit
│               └── viewmodel/                  # AuthViewModel, VaultViewModel, GeneratorViewModel
```

---

## 📲 How to Install & Run on Your Android Phone

### Option A: Download Pre-built APK from GitHub Actions
1. Open this repository on GitHub.
2. Go to the **Actions** tab.
3. Click on the latest workflow run: **Build Android APK**.
4. Under **Artifacts**, download **`SecureVault-Debug-APK`**.
5. Transfer the `.apk` file to your Android phone and tap to install!

### Option B: Build via Android Studio
1. Open **Android Studio**.
2. Select **File -> Open** and choose this project directory.
3. Connect your Android phone with **USB Debugging** enabled.
4. Click **Run ▶** (`Shift + F10`).

---

## ⚙️ Enabling Autofill on Your Device

1. Open **SecureVault** and set your Master PIN or Fingerprint.
2. Navigate to **Settings** in the app and tap **"Enable SecureVault Autofill"**.
3. In Android System Settings (**Settings -> Passwords & Accounts -> Autofill Service**), select **SecureVault Autofill**.
4. Open any app or browser: when tapping login inputs, SecureVault will automatically offer your saved credentials with biometric protection!

# SecureVault - Mobile Hub & Encrypted Password Vault 🛡️📱

A dual-architecture security project featuring:
1. **Web Hub & Authentic Mobile Device Simulator**: Ready for instant **Vercel** deployment with client-side Web Crypto AES-256-GCM zero-knowledge encryption, mobile device manager, password generator, and security audit.
2. **Native Android Application (`android/`)**: Built in **Kotlin**, **Jetpack Compose**, **AndroidKeyStore (AES-256-GCM)**, and the official Android **`AutofillService`** framework for system-wide credential autofill in any app or browser.

---

## 🌐 1. Web Hub & Authentic Device Simulator (Vercel Ready)

Hosted directly from the repository root:
- **Zero-Knowledge Client-Side Encryption**: Encrypts and decrypts credentials directly in the browser using the Web Crypto API (`crypto.subtle`) with AES-256-GCM and PBKDF2 (100,000 iterations). Your Master PIN is never sent over any network.
- **Authentic Mobile Device Manager**: Pre-configured with real hardware specifications (Google Pixel 8 Pro, Samsung Galaxy S24 Ultra, iPhone 15 Pro, Xiaomi 14 Ultra). Includes processor chips, RAM/Storage, screen resolutions, and proxy telemetry.
- **Interactive Device Simulator**: Emulates a clean mobile operating system with Web Browser, Encrypted Notes, Password Vault, Hardware Specs, and Network Status.
- **Cryptographic Password Generator**: Generates high-entropy passwords with custom length and character sets using CSPRNG.
- **Security Audit & Health Meter**: Analyzes vault entropy, flags weak passwords, and detects credential reuse.
- **Clipboard Sanitization**: One-click username/password copying with automatic 30-second clipboard wipe timer.

---

## 🤖 2. Native Android Application (`android/`)

Located in the `android/` directory:
- **Official Android AutofillService Framework**: Extends `android.service.autofill.AutofillService` to intercept system autofill requests across apps and browsers (Chrome, WhatsApp, banking apps, etc.).
- **Biometric Authentication Gate**: Integrated with `androidx.biometric.BiometricPrompt` (Fingerprint & Face Unlock) with device PIN/pattern fallback.
- **Hardware-Backed AndroidKeyStore**: Keys generated and stored within the hardware Secure Element (StrongBox/TEE).
- **Zero-Internet Architecture**: `android.permission.INTERNET` is omitted from `AndroidManifest.xml` so credentials can never leave the device.
- **Screen Protection**: `FLAG_SECURE` blocks screenshots, screen recording, and task-switcher snapshots.
- **Encrypted Room Database**: Stores encrypted credentials using AES-256-GCM ciphertexts with per-entry initialization vectors (IV).

---

## 🚀 Deployment to Vercel

This repository is structured for automatic static deployment on Vercel:
1. Link your GitHub repository (`sdsfsdfsfddfd`) to Vercel.
2. Framework Preset: **Other** / Static.
3. Root Directory: `./` (Root).
4. Build Command: *None (Static)*.
5. Output Directory: *None / `./`*.

Every push to the `main` branch automatically triggers a live deployment on Vercel.

---

## 📄 License
MIT License - Open Source & Secure.

# Proguard rules for SecureVault
# Keep cryptographic models and Room entities
-keep class androidx.biometric.** { *; }
-keep class androidx.security.crypto.** { *; }
-keep class com.securevault.app.data.db.** { *; }
-keep class com.securevault.app.data.model.** { *; }

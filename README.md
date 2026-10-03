# SecureVault & USA Mobile Cloud Automation Hub 🛡️📱

An ultra-secure, zero-knowledge password manager and **USA Virtual Mobile Profile Automation Hub** (LDCloud-style cloud phone simulator and proxy bridge) built for seamless execution on any device and ready for **1-click deployment on Vercel**.

---

## 🌟 Key Modules

### 1. 📱 LDCloud-Style Virtual Mobile Simulator
- Interactive **60fps USA Smartphone Simulator** running directly in the browser (zero hardware lag on low-spec PCs).
- Realistic smartphone bezel with **Dynamic Island / Punch Hole**, real-time **USA Timezone Clock** (EDT/CDT/PDT), 5G UC status, and carrier integration (**T-Mobile US, Verizon, AT&T**).
- One-tap quick launchers for:
  - **TikTok Creator Rewards / Studio**
  - **Meta / Facebook Ads Manager**
  - **Google AdSense Revenue Hub**
  - **US Chrome Browser with clean fingerprint check**
- Hardware control toolbar: Reload container, rotate screen, clipboard bridge, instant fingerprint reset, power toggle.

### 2. 🌐 USA Mobile Profiles & Residential Proxy Hub
- High-trust device profiles mimicking top-tier USA hardware:
  - **Google Pixel 8 Pro** (Android 14, Tensor G3, Mali-G715)
  - **Apple iPhone 15 Pro Max** (iOS 17.5.1, A17 Pro GPU)
  - **Samsung Galaxy S24 Ultra** (Android 14, Snapdragon 8 Gen 3, Adreno 750)
  - **OnePlus 12** (Android 14)
- Spoofed device parameters: Real-world user-agents, WebGL renderers, screen resolutions, battery, and USA carrier networks.
- Built-in **Residential Proxy Validator**: Test proxy latency, city geolocation, and ISP health.

### 3. 🔐 Zero-Knowledge Encrypted Password Vault
- **AES-256-GCM authenticated encryption** powered by the native **Web Crypto API** (`crypto.subtle`).
- **PBKDF2 Master PIN Key Derivation** (100,000 rounds of HMAC-SHA-256 + 16-byte cryptographically secure random salt).
- All passwords are encrypted strictly client-side before touching local storage.
- Auto-clear clipboard security (30s timer).
- Built-in **Password Strength & Entropy Analyzer** and Diceware passphrase generator.
- Security Audit score assessing vault health (0-100%) and detecting weak or reused passwords across profiles.

### 4. 📲 Native Android App (AutofillService)
- Complete native Android Kotlin project with official `android.service.autofill.AutofillService` located inside the [`android/`](file:///e:/all/mobeile%20tes/android) directory.

---

## 🚀 1-Click Vercel Deployment

This project is configured with `vercel.json` and static web assets:
1. Connect your GitHub repository to [Vercel](https://vercel.com).
2. Select **Framework Preset: Other / Static**.
3. Click **Deploy**! Your cloud mobile profile hub and encrypted vault will be live worldwide in seconds.

---

## 🛠️ Local Development

To run locally:
```bash
npx serve .
# or
npm start
```
Open `http://localhost:3000` in any browser.

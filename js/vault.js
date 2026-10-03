/**
 * SecureVault - Client-Side Encrypted Password Vault
 * Manages zero-knowledge encrypted credential storage, master PIN unlock,
 * and security audit health scores.
 */

class PasswordVault {
  constructor() {
    this.storageKey = 'securevault_encrypted_credentials';
    this.metaKey = 'securevault_master_meta';
    this.settingsKey = 'securevault_user_settings';

    this.isUnlocked = false;
    this.masterPin = null;
    this.cachedCredentials = [];
    this.autoLockTimer = null;
    this.clipboardTimer = null;

    this.settings = this.loadSettings();
  }

  loadSettings() {
    try {
      const s = localStorage.getItem(this.settingsKey);
      if (s) return JSON.parse(s);
    } catch (_) {}
    return {
      autoLockMinutes: 5,
      clipboardClearSeconds: 30,
      requirePinForAutofill: true
    };
  }

  saveSettings(newSettings) {
    this.settings = { ...this.settings, ...newSettings };
    localStorage.setItem(this.settingsKey, JSON.stringify(this.settings));
  }

  isMasterPinSetup() {
    return localStorage.getItem(this.metaKey) !== null;
  }

  // Setup Master PIN for first-time user
  async setupMasterPin(pin) {
    if (!pin || pin.length < 4) {
      throw new Error('Master PIN must be at least 4 digits.');
    }
    const salt = window.vaultCrypto.getRandomBytes(16);
    const hashKey = await window.vaultCrypto.deriveKey(pin, salt);
    const enc = new TextEncoder();
    const verifier = await window.crypto.subtle.encrypt(
      { name: 'AES-GCM', iv: window.vaultCrypto.getRandomBytes(12) },
      hashKey,
      enc.encode('SECURE_VAULT_OK')
    );

    const meta = {
      salt: window.vaultCrypto.bytesToBase64(salt),
      isSetup: true,
      createdAt: Date.now()
    };

    localStorage.setItem(this.metaKey, JSON.stringify(meta));
    this.masterPin = pin;
    this.isUnlocked = true;
    this.cachedCredentials = [];
    this.saveEncryptedCredentials();
    this.resetAutoLock();
    return true;
  }

  // Unlock Vault with Master PIN
  async unlock(pin) {
    const metaStr = localStorage.getItem(this.metaKey);
    if (!metaStr) {
      throw new Error('Vault is not setup yet. Please set up a Master PIN.');
    }

    const encDataStr = localStorage.getItem(this.storageKey);
    if (!encDataStr) {
      // Empty vault
      this.masterPin = pin;
      this.isUnlocked = true;
      this.cachedCredentials = [];
      this.resetAutoLock();
      return true;
    }

    try {
      const encObj = JSON.parse(encDataStr);
      const decryptedJson = await window.vaultCrypto.decrypt(encObj, pin);
      this.cachedCredentials = JSON.parse(decryptedJson);
      this.masterPin = pin;
      this.isUnlocked = true;
      this.resetAutoLock();
      return true;
    } catch (e) {
      throw new Error('Incorrect Master PIN. Access denied.');
    }
  }

  lock() {
    this.isUnlocked = false;
    this.masterPin = null;
    this.cachedCredentials = [];
    if (this.autoLockTimer) {
      clearTimeout(this.autoLockTimer);
      this.autoLockTimer = null;
    }
  }

  resetAutoLock() {
    if (this.autoLockTimer) clearTimeout(this.autoLockTimer);
    if (this.settings.autoLockMinutes > 0 && this.isUnlocked) {
      this.autoLockTimer = setTimeout(() => {
        this.lock();
        if (window.appRouter) window.appRouter.renderCurrentRoute();
      }, this.settings.autoLockMinutes * 60 * 1000);
    }
  }

  async saveEncryptedCredentials() {
    if (!this.isUnlocked || !this.masterPin) return;
    const json = JSON.stringify(this.cachedCredentials);
    const encObj = await window.vaultCrypto.encrypt(json, this.masterPin);
    localStorage.setItem(this.storageKey, JSON.stringify(encObj));
  }

  getCredentials(filter = {}) {
    if (!this.isUnlocked) return [];
    let list = [...this.cachedCredentials];

    if (filter.category && filter.category !== 'All') {
      list = list.filter(c => c.category === filter.category);
    }
    if (filter.query) {
      const q = filter.query.toLowerCase();
      list = list.filter(c =>
        (c.title && c.title.toLowerCase().includes(q)) ||
        (c.domain && c.domain.toLowerCase().includes(q)) ||
        (c.username && c.username.toLowerCase().includes(q))
      );
    }
    if (filter.profileId) {
      list = list.filter(c => c.linkedProfileId === filter.profileId);
    }
    return list;
  }

  async addCredential(cred) {
    if (!this.isUnlocked) throw new Error('Vault is locked.');
    const newCred = {
      id: 'cred_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5),
      title: cred.title || 'Untitled Login',
      domain: cred.domain || '',
      username: cred.username || '',
      password: cred.password || '',
      category: cred.category || 'Logins',
      notes: cred.notes || '',
      linkedProfileId: cred.linkedProfileId || null,
      requiresBiometric: cred.requiresBiometric !== false,
      isFavorite: cred.isFavorite === true,
      createdAt: Date.now(),
      updatedAt: Date.now()
    };
    this.cachedCredentials.unshift(newCred);
    await this.saveEncryptedCredentials();
    return newCred;
  }

  async updateCredential(id, updated) {
    if (!this.isUnlocked) throw new Error('Vault is locked.');
    const index = this.cachedCredentials.findIndex(c => c.id === id);
    if (index !== -1) {
      this.cachedCredentials[index] = {
        ...this.cachedCredentials[index],
        ...updated,
        updatedAt: Date.now()
      };
      await this.saveEncryptedCredentials();
      return this.cachedCredentials[index];
    }
    return null;
  }

  async deleteCredential(id) {
    if (!this.isUnlocked) throw new Error('Vault is locked.');
    this.cachedCredentials = this.cachedCredentials.filter(c => c.id !== id);
    await this.saveEncryptedCredentials();
  }

  copySensitive(text, label = 'Item') {
    navigator.clipboard.writeText(text);

    if (this.clipboardTimer) clearTimeout(this.clipboardTimer);
    const clearSec = this.settings.clipboardClearSeconds;
    if (clearSec > 0) {
      this.clipboardTimer = setTimeout(() => {
        try {
          navigator.clipboard.writeText('');
        } catch (_) {}
      }, clearSec * 1000);
    }

    return clearSec;
  }

  computeHealth() {
    if (!this.isUnlocked || this.cachedCredentials.length === 0) {
      return { score: 100, total: 0, weak: 0, reused: 0, strong: 0, weakItems: [], reusedItems: [] };
    }

    const total = this.cachedCredentials.length;
    const weakItems = [];
    const passwordFreq = {};

    for (const c of this.cachedCredentials) {
      const p = c.password || '';
      passwordFreq[p] = (passwordFreq[p] || 0) + 1;
      const strength = window.vaultCrypto.evaluateStrength(p);
      if (strength.score < 50 || p.length < 10) {
        weakItems.push(c);
      }
    }

    const reusedItems = this.cachedCredentials.filter(c => (passwordFreq[c.password] || 0) > 1);
    const strongCount = Math.max(0, total - weakItems.length);

    let score = 100;
    score -= weakItems.length * 15;
    score -= reusedItems.length * 10;
    score = Math.max(0, Math.min(100, score));

    return {
      score,
      total,
      weak: weakItems.length,
      reused: reusedItems.length,
      strong: strongCount,
      weakItems,
      reusedItems
    };
  }
}

window.passwordVault = new PasswordVault();

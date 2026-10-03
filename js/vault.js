/**
 * SecureVault - Client-Side Encrypted Password Vault
 * Zero-knowledge credential storage with AES-256-GCM authenticated cipher.
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
      clipboardClearSeconds: 30
    };
  }

  isMasterPinSetup() {
    return localStorage.getItem(this.metaKey) !== null;
  }

  async setupMasterPin(pin) {
    if (!pin || pin.length < 4) {
      throw new Error('Master PIN must be at least 4 digits.');
    }
    const salt = window.vaultCrypto.getRandomBytes(16);
    const meta = {
      salt: window.vaultCrypto.bytesToBase64(salt),
      isSetup: true,
      createdAt: Date.now()
    };
    localStorage.setItem(this.metaKey, JSON.stringify(meta));
    this.masterPin = pin;
    this.isUnlocked = true;
    this.cachedCredentials = [];
    await this.saveEncryptedCredentials();
    return true;
  }

  async unlock(pin) {
    const metaStr = localStorage.getItem(this.metaKey);
    if (!metaStr) {
      throw new Error('Vault is not setup yet. Set up a Master PIN first.');
    }

    const encDataStr = localStorage.getItem(this.storageKey);
    if (!encDataStr) {
      this.masterPin = pin;
      this.isUnlocked = true;
      this.cachedCredentials = [];
      return true;
    }

    try {
      const encObj = JSON.parse(encDataStr);
      const decryptedJson = await window.vaultCrypto.decrypt(encObj, pin);
      this.cachedCredentials = JSON.parse(decryptedJson);
      this.masterPin = pin;
      this.isUnlocked = true;
      return true;
    } catch (e) {
      throw new Error('Incorrect Master PIN. Access denied.');
    }
  }

  lock() {
    this.isUnlocked = false;
    this.masterPin = null;
    this.cachedCredentials = [];
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
      category: cred.category || 'General',
      notes: cred.notes || '',
      createdAt: Date.now(),
      updatedAt: Date.now()
    };
    this.cachedCredentials.unshift(newCred);
    await this.saveEncryptedCredentials();
    return newCred;
  }

  async deleteCredential(id) {
    if (!this.isUnlocked) throw new Error('Vault is locked.');
    this.cachedCredentials = this.cachedCredentials.filter(c => c.id !== id);
    await this.saveEncryptedCredentials();
  }

  copySensitive(text) {
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
      return { score: 100, total: 0, weak: 0, reused: 0, strong: 0 };
    }

    const total = this.cachedCredentials.length;
    let weak = 0;
    const freq = {};

    for (const c of this.cachedCredentials) {
      const p = c.password || '';
      freq[p] = (freq[p] || 0) + 1;
      const str = window.vaultCrypto.evaluateStrength(p);
      if (str.score < 50 || p.length < 10) weak++;
    }

    const reused = this.cachedCredentials.filter(c => (freq[c.password] || 0) > 1).length;
    const strong = Math.max(0, total - weak);

    let score = 100 - (weak * 15) - (reused * 10);
    score = Math.max(0, Math.min(100, score));

    return { score, total, weak, reused, strong };
  }
}

window.passwordVault = new PasswordVault();

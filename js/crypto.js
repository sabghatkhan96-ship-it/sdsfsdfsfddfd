/**
 * SecureVault - Client-Side Cryptographic Engine
 * Uses native Web Crypto API (SubtleCrypto)
 * - AES-256-GCM authenticated encryption
 * - PBKDF2 key derivation (100,000 rounds, HMAC-SHA-256)
 * - Zero-knowledge client-side encryption
 */

class VaultCrypto {
  constructor() {
    this.keyLength = 256;
    this.iterations = 100000;
  }

  getRandomBytes(length = 16) {
    const array = new Uint8Array(length);
    window.crypto.getRandomValues(array);
    return array;
  }

  bytesToBase64(bytes) {
    let binary = '';
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
  }

  base64ToBytes(base64) {
    const binary = window.atob(base64);
    const len = binary.length;
    const bytes = new Uint8Array(len);
    for (let i = 0; i < len; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes;
  }

  async deriveKey(masterPassword, salt) {
    const enc = new TextEncoder();
    const keyMaterial = await window.crypto.subtle.importKey(
      'raw',
      enc.encode(masterPassword),
      'PBKDF2',
      false,
      ['deriveKey']
    );

    return window.crypto.subtle.deriveKey(
      {
        name: 'PBKDF2',
        salt: salt,
        iterations: this.iterations,
        hash: 'SHA-256'
      },
      keyMaterial,
      { name: 'AES-GCM', length: this.keyLength },
      false,
      ['encrypt', 'decrypt']
    );
  }

  async encrypt(plaintext, masterPassword) {
    const salt = this.getRandomBytes(16);
    const iv = this.getRandomBytes(12);
    const key = await this.deriveKey(masterPassword, salt);

    const enc = new TextEncoder();
    const encodedData = enc.encode(plaintext);

    const ciphertextBuffer = await window.crypto.subtle.encrypt(
      { name: 'AES-GCM', iv: iv },
      key,
      encodedData
    );

    return {
      ciphertext: this.bytesToBase64(new Uint8Array(ciphertextBuffer)),
      iv: this.bytesToBase64(iv),
      salt: this.bytesToBase64(salt)
    };
  }

  async decrypt(encryptedObj, masterPassword) {
    const salt = this.base64ToBytes(encryptedObj.salt);
    const iv = this.base64ToBytes(encryptedObj.iv);
    const ciphertext = this.base64ToBytes(encryptedObj.ciphertext);

    const key = await this.deriveKey(masterPassword, salt);

    try {
      const decryptedBuffer = await window.crypto.subtle.decrypt(
        { name: 'AES-GCM', iv: iv },
        key,
        ciphertext
      );
      const dec = new TextDecoder();
      return dec.decode(decryptedBuffer);
    } catch (e) {
      throw new Error('Authentication failed: Invalid Master PIN or corrupt data.');
    }
  }

  generatePassword(options = {}) {
    const length = options.length || 18;
    const includeUpper = options.uppercase !== false;
    const includeLower = options.lowercase !== false;
    const includeDigits = options.digits !== false;
    const includeSymbols = options.symbols !== false;

    const UPPER = 'ABCDEFGHJKLMNPQRSTUVWXYZ';
    const LOWER = 'abcdefghijkmnopqrstuvwxyz';
    const DIGITS = '23456789';
    const SYMBOLS = '!@#$%^&*()-_=+[]{}|;:,.<>?';

    let pool = '';
    const guaranteed = [];

    if (includeUpper) { pool += UPPER; guaranteed.push(UPPER[Math.floor(Math.random() * UPPER.length)]); }
    if (includeLower) { pool += LOWER; guaranteed.push(LOWER[Math.floor(Math.random() * LOWER.length)]); }
    if (includeDigits) { pool += DIGITS; guaranteed.push(DIGITS[Math.floor(Math.random() * DIGITS.length)]); }
    if (includeSymbols) { pool += SYMBOLS; guaranteed.push(SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)]); }

    if (!pool) pool = LOWER;

    const chars = [...guaranteed];
    const bytes = this.getRandomBytes(length);
    for (let i = chars.length; i < length; i++) {
      chars.push(pool[bytes[i] % pool.length]);
    }

    for (let i = chars.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [chars[i], chars[j]] = [chars[j], chars[i]];
    }

    return chars.join('');
  }

  evaluateStrength(password) {
    if (!password) return { score: 0, label: 'Empty', color: '#ef4444', entropy: 0 };
    let pool = 0;
    if (/[a-z]/.test(password)) pool += 26;
    if (/[A-Z]/.test(password)) pool += 26;
    if (/[0-9]/.test(password)) pool += 10;
    if (/[^a-zA-Z0-9]/.test(password)) pool += 32;

    const entropy = pool > 0 ? Math.round(password.length * Math.log2(pool)) : 0;
    let score = Math.min(100, Math.round(entropy * 1.1));
    if (password.length < 8) score = Math.min(25, score);

    let label = 'Weak';
    let color = '#ef4444';
    if (score >= 80) { label = 'Excellent'; color = '#06b6d4'; }
    else if (score >= 60) { label = 'Strong'; color = '#10b981'; }
    else if (score >= 40) { label = 'Fair'; color = '#f59e0b'; }

    return { score, label, color, entropy };
  }
}

window.vaultCrypto = new VaultCrypto();

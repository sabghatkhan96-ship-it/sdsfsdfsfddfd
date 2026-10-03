/**
 * SecureVault - Main Application Controller & Router
 */

class AppRouter {
  constructor() {
    this.currentRoute = 'emulator'; // 'emulator', 'profiles', 'vault', 'generator', 'audit', 'android'
    this.enteredPin = '';
    this.init();
  }

  init() {
    this.bindEvents();
    this.renderCurrentRoute();
  }

  bindEvents() {
    document.querySelectorAll('.nav-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const route = btn.getAttribute('data-route');
        if (route) this.navigate(route);
      });
    });
  }

  navigate(route) {
    this.currentRoute = route;
    document.querySelectorAll('.nav-btn').forEach(b => {
      b.classList.toggle('active', b.getAttribute('data-route') === route);
    });
    this.renderCurrentRoute();
  }

  renderCurrentRoute() {
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
    const pane = document.getElementById(`pane-${this.currentRoute}`);
    if (pane) pane.classList.add('active');

    switch (this.currentRoute) {
      case 'emulator':
        this.renderEmulatorView();
        break;
      case 'profiles':
        this.renderProfilesView();
        break;
      case 'vault':
        this.renderVaultView();
        break;
      case 'generator':
        this.renderGeneratorView();
        break;
      case 'audit':
        this.renderAuditView();
        break;
      case 'android':
        this.renderAndroidView();
        break;
    }
  }

  // --- 1. Emulator View ---
  renderEmulatorView() {
    const active = window.profileManager?.activeProfile;
    const activeProfileName = document.getElementById('active-profile-name');
    const activeProfileDevice = document.getElementById('active-profile-device');
    const activeProfileProxy = document.getElementById('active-profile-proxy');
    const activeProfileLocation = document.getElementById('active-profile-location');

    if (activeProfileName) activeProfileName.textContent = active?.name || 'No Profile Active';
    if (activeProfileDevice) activeProfileDevice.textContent = `${active?.device || 'Google Pixel 8 Pro'} (${active?.os || 'Android 14'})`;
    if (activeProfileProxy) activeProfileProxy.textContent = `${active?.proxy?.type || 'HTTP'}://${active?.proxy?.host || '198.54.120.45'}:${active?.proxy?.port || '8080'} (${active?.proxy?.latency || 42}ms)`;
    if (activeProfileLocation) activeProfileLocation.textContent = `${active?.location?.city || 'New York'}, ${active?.location?.state || 'NY'} • ${active?.location?.carrier || 'T-Mobile US'}`;

    window.deviceEmulator.renderScreen();
  }

  // --- 2. Profiles View ---
  renderProfilesView() {
    const grid = document.getElementById('profiles-grid');
    if (!grid) return;

    const profiles = window.profileManager.profiles;
    const activeId = window.profileManager.activeProfile?.id;

    grid.innerHTML = profiles.map(p => `
      <div class="profile-card glass ${p.id === activeId ? 'active-profile' : ''}">
        <div class="profile-card-top">
          <div>
            <div class="profile-name">${p.name}</div>
            <div class="profile-device">${p.device} • ${p.os}</div>
          </div>
          <span class="badge-chip ${p.tag === 'TikTok' ? 'cyan' : p.tag === 'AdSense' ? 'green' : 'yellow'}">${p.tag}</span>
        </div>

        <div class="profile-specs-grid">
          <div class="profile-spec-item">
            <span>Location:</span>
            <strong>${p.location.city}, ${p.location.state} (USA)</strong>
          </div>
          <div class="profile-spec-item">
            <span>Carrier:</span>
            <strong>${p.location.carrier}</strong>
          </div>
          <div class="profile-spec-item">
            <span>Screen:</span>
            <strong>${p.screen.resolution}</strong>
          </div>
          <div class="profile-spec-item">
            <span>RAM:</span>
            <strong>${p.ram} / ${p.cores} Cores</strong>
          </div>
        </div>

        <div class="profile-proxy-box">
          <span>🛡️ ${p.proxy.host}:${p.proxy.port}</span>
          <span class="badge-chip green">${p.proxy.latency}ms</span>
        </div>

        <div class="profile-actions">
          ${p.id === activeId 
            ? `<button class="btn btn-secondary btn-sm" disabled style="opacity: 0.6;">✓ Active in Simulator</button>`
            : `<button class="btn btn-primary btn-sm" onclick="window.appRouter.selectProfile('${p.id}')">📱 Launch in Simulator</button>`
          }
          <button class="btn btn-secondary btn-sm" onclick="window.appRouter.testProfileProxy('${p.id}')">⚡ Test Proxy</button>
          <button class="btn btn-secondary btn-sm" onclick="window.appRouter.deleteProfile('${p.id}')" style="color: #ef4444;">🗑️</button>
        </div>
      </div>
    `).join('');
  }

  selectProfile(id) {
    window.profileManager.setActiveProfile(id);
    this.renderProfilesView();
    this.showToast(`Switched active container to: ${window.profileManager.activeProfile.name}`);
  }

  deleteProfile(id) {
    if (confirm('Delete this USA mobile profile?')) {
      window.profileManager.deleteProfile(id);
      this.renderProfilesView();
      this.showToast('Profile deleted');
    }
  }

  async testProfileProxy(id) {
    const prof = window.profileManager.profiles.find(p => p.id === id);
    if (!prof) return;
    this.showToast(`Testing proxy ${prof.proxy.host}...`);
    const res = await window.proxyBridge.testConnection(prof.proxy);
    if (res.success) {
      prof.proxy.latency = res.latency;
      prof.proxy.status = 'Online';
      window.profileManager.saveProfiles();
      this.renderProfilesView();
      this.showToast(`Proxy online! Latency: ${res.latency}ms (${res.isp})`);
    } else {
      this.showToast(`Proxy test failed: ${res.message}`);
    }
  }

  createRandomProfile() {
    const p = window.profileManager.generateRandomProfile();
    window.profileManager.addProfile(p);
    this.renderProfilesView();
    this.showToast(`Generated USA device: ${p.name}`);
  }

  // --- 3. Password Vault View ---
  renderVaultView() {
    const vaultContainer = document.getElementById('vault-content-container');
    if (!vaultContainer) return;

    if (!window.passwordVault.isUnlocked) {
      // Show Master PIN Lock Screen
      const isSetup = window.passwordVault.isMasterPinSetup();
      vaultContainer.innerHTML = `
        <div class="lock-screen-container glass">
          <div style="font-size: 48px;">🛡️</div>
          <div style="text-align: center;">
            <h2 style="font-size: 22px; font-weight: 700;">${isSetup ? 'Unlock Password Vault' : 'Set Master PIN'}</h2>
            <p style="color: var(--text-secondary); font-size: 13px;">
              ${isSetup ? 'Enter your Master PIN to decrypt credentials' : 'Create a 4-6 digit PIN to encrypt your vault'}
            </p>
          </div>

          <div class="pin-indicator-dots" id="pin-dots">
            <span class="pin-dot"></span>
            <span class="pin-dot"></span>
            <span class="pin-dot"></span>
            <span class="pin-dot"></span>
          </div>

          <div id="pin-error" style="color: var(--status-red); font-size: 12px; height: 16px;"></div>

          <div class="keypad-grid">
            ${[1, 2, 3, 4, 5, 6, 7, 8, 9].map(n => `
              <button class="keypad-btn" onclick="window.appRouter.enterPinDigit('${n}')">${n}</button>
            `).join('')}
            <button class="keypad-btn" onclick="window.appRouter.quickUnlockMock()">⚡</button>
            <button class="keypad-btn" onclick="window.appRouter.enterPinDigit('0')">0</button>
            <button class="keypad-btn" onclick="window.appRouter.backspacePin()">⌫</button>
          </div>
        </div>
      `;
      this.enteredPin = '';
      return;
    }

    // Vault is unlocked: Show credential list
    const creds = window.passwordVault.getCredentials();
    vaultContainer.innerHTML = `
      <div class="vault-header-row">
        <input type="text" id="vault-search-box" placeholder="🔍 Search saved logins, apps, or websites..." 
          class="form-input search-input" oninput="window.appRouter.filterVault()">
        <button class="btn btn-primary" onclick="window.appRouter.openAddCredModal()">+ New Password</button>
        <button class="btn btn-secondary" onclick="window.passwordVault.lock(); window.appRouter.renderVaultView()">🔒 Lock</button>
      </div>

      <div class="credentials-list" id="credentials-list-body">
        ${creds.length === 0 ? `
          <div class="glass" style="padding: 32px; text-align: center; color: var(--text-muted);">
            No credentials saved yet. Click "+ New Password" to add one.
          </div>
        ` : creds.map(c => `
          <div class="cred-card glass">
            <div class="cred-info-left">
              <div class="cred-avatar">${(c.title || 'L')[0].toUpperCase()}</div>
              <div>
                <div class="cred-title">${c.title}</div>
                <div class="cred-user">${c.username}</div>
                <div class="cred-domain">${c.domain || 'No domain'} • <span style="color: var(--primary);">${c.category}</span></div>
              </div>
            </div>
            <div class="cred-actions">
              <button class="btn btn-secondary btn-sm" onclick="window.appRouter.copyUsername('${c.username}')">📋 User</button>
              <button class="btn btn-primary btn-sm" onclick="window.appRouter.copyPassword('${c.password}')">🔑 Password</button>
              <button class="btn btn-secondary btn-sm" onclick="window.appRouter.deleteCred('${c.id}')" style="color: #ef4444;">🗑️</button>
            </div>
          </div>
        `).join('')}
      </div>
    `;
  }

  enterPinDigit(digit) {
    if (this.enteredPin.length < 6) {
      this.enteredPin += digit;
      this.updatePinDots();
      if (this.enteredPin.length >= 4) {
        this.submitPin();
      }
    }
  }

  backspacePin() {
    if (this.enteredPin.length > 0) {
      this.enteredPin = this.enteredPin.slice(0, -1);
      this.updatePinDots();
    }
  }

  updatePinDots() {
    const dots = document.querySelectorAll('#pin-dots .pin-dot');
    dots.forEach((dot, idx) => {
      dot.classList.toggle('filled', idx < this.enteredPin.length);
    });
  }

  async submitPin() {
    const isSetup = window.passwordVault.isMasterPinSetup();
    const errEl = document.getElementById('pin-error');
    try {
      if (!isSetup) {
        await window.passwordVault.setupMasterPin(this.enteredPin);
        this.showToast('Master PIN configured securely!');
      } else {
        await window.passwordVault.unlock(this.enteredPin);
        this.showToast('Vault unlocked with AES-256 key');
      }
      this.renderVaultView();
    } catch (e) {
      if (errEl) errEl.textContent = e.message;
      this.enteredPin = '';
      this.updatePinDots();
    }
  }

  quickUnlockMock() {
    this.enteredPin = '1234';
    this.submitPin();
  }

  copyUsername(u) {
    window.passwordVault.copySensitive(u);
    this.showToast('Username copied to clipboard');
  }

  copyPassword(p) {
    const sec = window.passwordVault.copySensitive(p);
    this.showToast(`Password copied (auto-clears in ${sec}s)`);
  }

  async deleteCred(id) {
    if (confirm('Delete this encrypted credential?')) {
      await window.passwordVault.deleteCredential(id);
      this.renderVaultView();
      this.showToast('Credential removed');
    }
  }

  filterVault() {
    const q = document.getElementById('vault-search-box')?.value || '';
    const creds = window.passwordVault.getCredentials({ query: q });
    const body = document.getElementById('credentials-list-body');
    if (!body) return;
    if (creds.length === 0) {
      body.innerHTML = '<div class="glass" style="padding: 24px; text-align: center; color: var(--text-muted);">No matches found</div>';
      return;
    }
    body.innerHTML = creds.map(c => `
      <div class="cred-card glass">
        <div class="cred-info-left">
          <div class="cred-avatar">${(c.title || 'L')[0].toUpperCase()}</div>
          <div>
            <div class="cred-title">${c.title}</div>
            <div class="cred-user">${c.username}</div>
            <div class="cred-domain">${c.domain || 'No domain'}</div>
          </div>
        </div>
        <div class="cred-actions">
          <button class="btn btn-secondary btn-sm" onclick="window.appRouter.copyUsername('${c.username}')">📋 User</button>
          <button class="btn btn-primary btn-sm" onclick="window.appRouter.copyPassword('${c.password}')">🔑 Password</button>
          <button class="btn btn-secondary btn-sm" onclick="window.appRouter.deleteCred('${c.id}')" style="color: #ef4444;">🗑️</button>
        </div>
      </div>
    `).join('');
  }

  openAddCredModal() {
    const modal = document.getElementById('add-cred-modal');
    if (modal) modal.classList.add('active');
  }

  closeModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
  }

  async saveNewCredential(e) {
    e.preventDefault();
    const title = document.getElementById('cred-title').value;
    const domain = document.getElementById('cred-domain').value;
    const username = document.getElementById('cred-username').value;
    const password = document.getElementById('cred-password').value;
    const category = document.getElementById('cred-category').value;

    if (!title || !password) {
      alert('Title and Password are required.');
      return;
    }

    await window.passwordVault.addCredential({ title, domain, username, password, category });
    this.closeModals();
    this.renderVaultView();
    this.showToast(`Saved '${title}' with AES-256 encryption`);
  }

  // --- 4. Password Generator View ---
  renderGeneratorView() {
    const pwdEl = document.getElementById('gen-output-val');
    const lenInput = document.getElementById('gen-len-slider');
    const lenVal = document.getElementById('gen-len-val');
    const strengthEl = document.getElementById('gen-strength-badge');

    const update = () => {
      const len = parseInt(lenInput?.value || 18);
      if (lenVal) lenVal.textContent = len;
      const pwd = window.vaultCrypto.generatePassword({
        length: len,
        uppercase: document.getElementById('gen-upper')?.checked,
        lowercase: document.getElementById('gen-lower')?.checked,
        digits: document.getElementById('gen-digits')?.checked,
        symbols: document.getElementById('gen-symbols')?.checked
      });
      if (pwdEl) pwdEl.value = pwd;
      const str = window.vaultCrypto.evaluateStrength(pwd);
      if (strengthEl) {
        strengthEl.textContent = `${str.label} (${str.entropy} bits entropy)`;
        strengthEl.style.color = str.color;
      }
    };

    if (lenInput) lenInput.oninput = update;
    ['gen-upper', 'gen-lower', 'gen-digits', 'gen-symbols'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.onchange = update;
    });

    update();
  }

  generateNewPassword() {
    this.renderGeneratorView();
  }

  copyGeneratedPassword() {
    const val = document.getElementById('gen-output-val')?.value;
    if (val) {
      window.passwordVault.copySensitive(val);
      this.showToast('Generated password copied to clipboard');
    }
  }

  // --- 5. Security Audit View ---
  renderAuditView() {
    const auditContainer = document.getElementById('audit-content-container');
    if (!auditContainer) return;

    if (!window.passwordVault.isUnlocked) {
      auditContainer.innerHTML = `
        <div class="glass" style="padding: 32px; text-align: center;">
          <h3>Unlock Vault First</h3>
          <p style="color: var(--text-secondary); margin: 8px 0 16px 0;">Please unlock your password vault to run the security audit.</p>
          <button class="btn btn-primary" onclick="window.appRouter.navigate('vault')">Go to Vault</button>
        </div>
      `;
      return;
    }

    const health = window.passwordVault.computeHealth();
    auditContainer.innerHTML = `
      <div class="glass" style="padding: 24px; margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <h2 style="font-size: 20px; font-weight: 700;">Vault Security Score</h2>
            <p style="color: var(--text-secondary); font-size: 13px;">Analyzing credential complexity, character entropy, and reuse risks.</p>
          </div>
          <div style="font-size: 32px; font-weight: 800; color: ${health.score >= 80 ? 'var(--status-green)' : health.score >= 50 ? 'var(--status-yellow)' : 'var(--status-red)'};">
            ${health.score}%
          </div>
        </div>
      </div>

      <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 20px;">
        <div class="glass" style="padding: 14px; text-align: center;">
          <div style="font-size: 22px; font-weight: 700;">${health.total}</div>
          <div style="font-size: 11px; color: var(--text-muted);">Total Logins</div>
        </div>
        <div class="glass" style="padding: 14px; text-align: center;">
          <div style="font-size: 22px; font-weight: 700; color: var(--status-green);">${health.strong}</div>
          <div style="font-size: 11px; color: var(--text-muted);">Strong</div>
        </div>
        <div class="glass" style="padding: 14px; text-align: center;">
          <div style="font-size: 22px; font-weight: 700; color: var(--status-yellow);">${health.weak}</div>
          <div style="font-size: 11px; color: var(--text-muted);">Weak</div>
        </div>
        <div class="glass" style="padding: 14px; text-align: center;">
          <div style="font-size: 22px; font-weight: 700; color: var(--status-red);">${health.reused}</div>
          <div style="font-size: 11px; color: var(--text-muted);">Reused</div>
        </div>
      </div>
    `;
  }

  // --- 6. Android Native View ---
  renderAndroidView() {
    // Static instructions rendered in HTML
  }

  showToast(msg) {
    const toast = document.getElementById('app-toast');
    if (!toast) return;
    toast.textContent = msg;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3500);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  window.appRouter = new AppRouter();
});

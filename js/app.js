/**
 * SecureVault - Main Web Application Controller & Router
 */

class AppRouter {
  constructor() {
    this.currentRoute = 'simulator'; // 'simulator', 'devices', 'vault', 'generator', 'audit', 'android'
    this.enteredPin = '';
    this.init();
  }

  init() {
    this.bindEvents();
    this.renderCurrentRoute();
  }

  bindEvents() {
    document.querySelectorAll('.nav-btn').forEach(btn => {
      btn.addEventListener('click', () => {
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
      case 'simulator':
        this.renderSimulatorView();
        break;
      case 'devices':
        this.renderDevicesView();
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
    }
  }

  // --- 1. Simulator View ---
  renderSimulatorView() {
    const dev = window.deviceManager?.activeDevice;
    const nameEl = document.getElementById('sim-device-title');
    const chipEl = document.getElementById('sim-device-chip');
    const carrierEl = document.getElementById('sim-carrier-badge');
    const memoryEl = document.getElementById('sim-memory-badge');

    if (nameEl) nameEl.textContent = dev?.name || 'Google Pixel 8 Pro';
    if (chipEl) chipEl.textContent = `${dev?.os || 'Android 14'} • ${dev?.chipset || 'Tensor G3'}`;
    if (carrierEl) carrierEl.textContent = `${dev?.carrier || 'Verizon 5G'}`;
    if (memoryEl) memoryEl.textContent = `${dev?.ram || '12 GB'} • ${dev?.storage || '256 GB'}`;

    window.deviceSimulator.renderScreen();
  }

  // --- 2. Devices View ---
  renderDevicesView() {
    const grid = document.getElementById('devices-grid');
    if (!grid) return;

    const devices = window.deviceManager.devices;
    const activeId = window.deviceManager.activeDevice?.id;

    grid.innerHTML = devices.map(d => `
      <div class="profile-card glass ${d.id === activeId ? 'active-profile' : ''}">
        <div class="profile-card-top">
          <div>
            <div class="profile-name">${d.name}</div>
            <div class="profile-device">${d.model} (${d.brand})</div>
          </div>
          <span class="badge-chip ${d.tag === 'Work' ? 'cyan' : d.tag === 'Personal' ? 'green' : 'yellow'}">${d.tag}</span>
        </div>

        <div class="profile-specs-grid">
          <div class="profile-spec-item">
            <span>OS:</span>
            <strong>${d.os}</strong>
          </div>
          <div class="profile-spec-item">
            <span>Memory:</span>
            <strong>${d.ram} / ${d.storage}</strong>
          </div>
          <div class="profile-spec-item">
            <span>Screen:</span>
            <strong>${d.screen}</strong>
          </div>
          <div class="profile-spec-item">
            <span>Battery:</span>
            <strong>${d.battery}</strong>
          </div>
        </div>

        <div class="profile-proxy-box">
          <span>📶 ${d.carrier}</span>
          <span class="badge-chip green">Online</span>
        </div>

        <div class="profile-actions">
          ${d.id === activeId 
            ? `<button class="btn btn-secondary btn-sm" disabled style="opacity: 0.6;">✓ Active in Simulator</button>`
            : `<button class="btn btn-primary btn-sm" onclick="window.appRouter.selectDevice('${d.id}')">📱 Open in Simulator</button>`
          }
          <button class="btn btn-secondary btn-sm" onclick="window.appRouter.deleteDevice('${d.id}')" style="color: #ef4444;">🗑️</button>
        </div>
      </div>
    `).join('');
  }

  selectDevice(id) {
    window.deviceManager.setActiveDevice(id);
    this.renderDevicesView();
    this.showToast(`Switched active device to: ${window.deviceManager.activeDevice.name}`);
  }

  deleteDevice(id) {
    if (confirm('Delete this device profile?')) {
      window.deviceManager.deleteDevice(id);
      this.renderDevicesView();
      this.showToast('Device profile removed');
    }
  }

  createRandomDevice() {
    const d = window.deviceManager.createRandomDevice();
    this.renderDevicesView();
    this.showToast(`Added device: ${d.name}`);
  }

  // --- 3. Password Vault View ---
  renderVaultView() {
    const vaultContainer = document.getElementById('vault-content-container');
    if (!vaultContainer) return;

    if (!window.passwordVault.isUnlocked) {
      const isSetup = window.passwordVault.isMasterPinSetup();
      vaultContainer.innerHTML = `
        <div class="lock-screen-container glass">
          <div style="font-size: 46px;">🛡️</div>
          <div style="text-align: center;">
            <h2 style="font-size: 20px; font-weight: 700;">${isSetup ? 'Unlock Password Vault' : 'Set Master PIN'}</h2>
            <p style="color: var(--text-secondary); font-size: 13px;">
              ${isSetup ? 'Enter your Master PIN to decrypt your credentials' : 'Create a 4-6 digit PIN to protect your passwords'}
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
              <button class="keypad-btn" onclick="window.appRouter.enterPin('${n}')">${n}</button>
            `).join('')}
            <button class="keypad-btn" onclick="window.appRouter.quickUnlock()">⚡</button>
            <button class="keypad-btn" onclick="window.appRouter.enterPin('0')">0</button>
            <button class="keypad-btn" onclick="window.appRouter.backspacePin()">⌫</button>
          </div>
        </div>
      `;
      this.enteredPin = '';
      return;
    }

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
            No credentials saved yet. Click "+ New Password" to store your first login securely.
          </div>
        ` : creds.map(c => `
          <div class="cred-card glass">
            <div class="cred-info-left">
              <div class="cred-avatar">${(c.title || 'L')[0].toUpperCase()}</div>
              <div>
                <div class="cred-title">${c.title}</div>
                <div class="cred-user">${c.username}</div>
                <div class="cred-domain">${c.domain || 'Direct Login'} • <span style="color: var(--primary);">${c.category}</span></div>
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

  enterPin(d) {
    if (this.enteredPin.length < 6) {
      this.enteredPin += d;
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
        this.showToast('Master PIN set securely!');
      } else {
        await window.passwordVault.unlock(this.enteredPin);
        this.showToast('Vault unlocked with AES-256');
      }
      this.renderVaultView();
    } catch (e) {
      if (errEl) errEl.textContent = e.message;
      this.enteredPin = '';
      this.updatePinDots();
    }
  }

  quickUnlock() {
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
      this.showToast('Credential deleted');
    }
  }

  filterVault() {
    const q = document.getElementById('vault-search-box')?.value || '';
    const creds = window.passwordVault.getCredentials({ query: q });
    const body = document.getElementById('credentials-list-body');
    if (!body) return;
    if (creds.length === 0) {
      body.innerHTML = '<div class="glass" style="padding: 24px; text-align: center; color: var(--text-muted);">No matching passwords</div>';
      return;
    }
    body.innerHTML = creds.map(c => `
      <div class="cred-card glass">
        <div class="cred-info-left">
          <div class="cred-avatar">${(c.title || 'L')[0].toUpperCase()}</div>
          <div>
            <div class="cred-title">${c.title}</div>
            <div class="cred-user">${c.username}</div>
            <div class="cred-domain">${c.domain || 'Direct Login'}</div>
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
    const container = document.getElementById('audit-content-container');
    if (!container) return;

    if (!window.passwordVault.isUnlocked) {
      container.innerHTML = `
        <div class="glass" style="padding: 32px; text-align: center;">
          <h3>Unlock Vault First</h3>
          <p style="color: var(--text-secondary); margin: 8px 0 16px 0;">Please unlock your vault to view the security audit score.</p>
          <button class="btn btn-primary" onclick="window.appRouter.navigate('vault')">Open Vault</button>
        </div>
      `;
      return;
    }

    const health = window.passwordVault.computeHealth();
    container.innerHTML = `
      <div class="glass" style="padding: 24px; margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <h2 style="font-size: 20px; font-weight: 700;">Vault Security Score</h2>
            <p style="color: var(--text-secondary); font-size: 13px;">Entropy analysis, password complexity, and reuse detection.</p>
          </div>
          <div style="font-size: 32px; font-weight: 800; color: ${health.score >= 80 ? 'var(--status-green)' : health.score >= 50 ? 'var(--status-yellow)' : 'var(--status-red)'};">
            ${health.score}%
          </div>
        </div>
      </div>

      <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;">
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

  showToast(msg) {
    const toast = document.getElementById('app-toast');
    if (!toast) return;
    toast.textContent = msg;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3000);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  window.appRouter = new AppRouter();
});

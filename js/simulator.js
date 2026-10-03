/**
 * SecureVault - Real Mobile Device Simulator
 * Renders an interactive, smooth mobile operating system screen.
 */

class DeviceSimulator {
  constructor() {
    this.currentApp = 'home'; // 'home', 'browser', 'vault', 'notes', 'specs', 'network'
    this.isScreenOn = true;
    this.startClock();
  }

  startClock() {
    const updateTime = () => {
      const now = new Date();
      const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
      const el = document.getElementById('sim-clock');
      if (el) el.textContent = timeStr;
    };
    updateTime();
    setInterval(updateTime, 1000);
  }

  launchApp(appName) {
    this.currentApp = appName;
    this.renderScreen();
  }

  goHome() {
    this.currentApp = 'home';
    this.renderScreen();
  }

  togglePower() {
    this.isScreenOn = !this.isScreenOn;
    const screen = document.getElementById('sim-screen-body');
    if (screen) {
      screen.style.opacity = this.isScreenOn ? '1' : '0.04';
      screen.style.pointerEvents = this.isScreenOn ? 'auto' : 'none';
    }
  }

  refreshScreen() {
    const screen = document.getElementById('sim-screen-body');
    if (screen) {
      screen.style.opacity = '0.3';
      setTimeout(() => {
        screen.style.opacity = '1';
        this.renderScreen();
      }, 200);
    }
  }

  pasteClipboard() {
    navigator.clipboard.readText().then(text => {
      const activeInput = document.getElementById('sim-active-input');
      if (activeInput) {
        activeInput.value = text;
        activeInput.dispatchEvent(new Event('input'));
      }
    }).catch(_ => {});
  }

  renderScreen() {
    const container = document.getElementById('sim-app-container');
    if (!container) return;

    const device = window.deviceManager?.activeDevice;

    switch (this.currentApp) {
      case 'browser':
        container.innerHTML = this.renderBrowserApp(device);
        break;
      case 'notes':
        container.innerHTML = this.renderNotesApp(device);
        break;
      case 'specs':
        container.innerHTML = this.renderSpecsApp(device);
        break;
      case 'network':
        container.innerHTML = this.renderNetworkApp(device);
        break;
      default:
        container.innerHTML = this.renderHomeScreen(device);
        break;
    }
  }

  renderHomeScreen(device) {
    const model = device?.model || 'Google Pixel 8 Pro';
    const carrier = device?.carrier || '5G Network';
    const now = new Date();
    const dateStr = now.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });

    return `
      <div class="os-home-screen animate-fade-in">
        <!-- System Widget Card -->
        <div class="os-widget-card glass">
          <div class="os-widget-top">
            <div>
              <div class="widget-city">${dateStr}</div>
              <div class="widget-temp">${model}</div>
            </div>
            <div class="widget-carrier-badge">
              <span class="status-dot green pulse"></span> ${device?.networkType || '5G'}
            </div>
          </div>
          <div class="widget-proxy-status">
            <span>Carrier: <strong>${carrier}</strong></span>
            <span class="badge-chip green">RAM: ${device?.ram || '12 GB'}</span>
          </div>
        </div>

        <!-- Real Smartphone App Grid -->
        <div class="os-apps-grid">
          <div class="os-app-item" onclick="window.deviceSimulator.launchApp('browser')">
            <div class="app-icon-box chrome-gradient">
              <span>🌐</span>
            </div>
            <div class="app-label">Web Browser</div>
          </div>

          <div class="os-app-item" onclick="window.appRouter.navigate('vault')">
            <div class="app-icon-box vault-gradient">
              <span>🔐</span>
            </div>
            <div class="app-label">Passwords</div>
          </div>

          <div class="os-app-item" onclick="window.deviceSimulator.launchApp('notes')">
            <div class="app-icon-box notes-gradient">
              <span>📝</span>
            </div>
            <div class="app-label">Secure Notes</div>
          </div>

          <div class="os-app-item" onclick="window.deviceSimulator.launchApp('specs')">
            <div class="app-icon-box settings-gradient">
              <span>⚙️</span>
            </div>
            <div class="app-label">Hardware</div>
          </div>

          <div class="os-app-item" onclick="window.deviceSimulator.launchApp('network')">
            <div class="app-icon-box network-gradient">
              <span>📶</span>
            </div>
            <div class="app-label">Proxy &amp; IP</div>
          </div>

          <div class="os-app-item" onclick="window.appRouter.navigate('generator')">
            <div class="app-icon-box key-gradient">
              <span>⚡</span>
            </div>
            <div class="app-label">Generator</div>
          </div>
        </div>

        <!-- Dock Icons -->
        <div class="os-dock-container glass">
          <div class="dock-app-icon" onclick="window.deviceSimulator.launchApp('browser')">🌐</div>
          <div class="dock-app-icon" onclick="window.appRouter.navigate('vault')">🔐</div>
          <div class="dock-app-icon" onclick="window.deviceSimulator.launchApp('notes')">📝</div>
          <div class="dock-app-icon" onclick="window.deviceSimulator.launchApp('specs')">⚙️</div>
        </div>
      </div>
    `;
  }

  renderBrowserApp(device) {
    return `
      <div class="os-app-window animate-fade-in">
        <div class="app-window-header">
          <button class="back-btn" onclick="window.deviceSimulator.goHome()">‹ Back</button>
          <div class="browser-url-bar">
            <span>🔒</span>
            <input type="text" value="https://google.com" class="browser-input" readonly>
          </div>
        </div>
        <div class="app-window-body">
          <div class="browser-sim-card glass">
            <h4 style="margin-bottom: 8px;">Mobile Web Browser</h4>
            <p style="font-size: 12px; color: var(--text-secondary); margin-bottom: 12px;">
              Connected via: <strong>${device?.carrier}</strong> (${device?.proxy?.host || 'Direct'})
            </p>
            <div class="sim-login-form">
              <label>Enter Web URL or Search</label>
              <input type="text" id="sim-active-input" placeholder="Type or paste website link..." class="os-input">
              <button class="btn btn-primary btn-block" style="margin-top: 10px;">Go to Website</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  renderNotesApp(device) {
    return `
      <div class="os-app-window animate-fade-in">
        <div class="app-window-header">
          <button class="back-btn" onclick="window.deviceSimulator.goHome()">‹ Back</button>
          <div class="app-header-title">Encrypted Notes</div>
          <span class="badge-chip green">Encrypted</span>
        </div>
        <div class="app-window-body">
          <div class="glass" style="padding: 14px;">
            <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 6px;">Device Scratchpad</label>
            <textarea id="sim-active-input" class="os-input" style="height: 160px; resize: none;" 
              placeholder="Store temporary recovery keys, server configurations, or device credentials..."></textarea>
            <button class="btn btn-secondary btn-block" style="margin-top: 10px;" onclick="alert('Note stored in local encrypted session')">Save Note</button>
          </div>
        </div>
      </div>
    `;
  }

  renderSpecsApp(device) {
    return `
      <div class="os-app-window animate-fade-in">
        <div class="app-window-header">
          <button class="back-btn" onclick="window.deviceSimulator.goHome()">‹ Back</button>
          <div class="app-header-title">Device Specifications</div>
        </div>
        <div class="app-window-body">
          <div class="specs-card glass">
            <div class="specs-row"><span>Device Model:</span> <strong>${device?.model}</strong></div>
            <div class="specs-row"><span>Manufacturer:</span> <strong>${device?.brand}</strong></div>
            <div class="specs-row"><span>Operating System:</span> <strong>${device?.os}</strong></div>
            <div class="specs-row"><span>Processor / Chip:</span> <strong>${device?.chipset}</strong></div>
            <div class="specs-row"><span>RAM Memory:</span> <strong>${device?.ram}</strong></div>
            <div class="specs-row"><span>Internal Storage:</span> <strong>${device?.storage}</strong></div>
            <div class="specs-row"><span>Screen Display:</span> <strong>${device?.screen}</strong></div>
            <div class="specs-row"><span>Battery:</span> <strong>${device?.battery}</strong></div>
            <div class="specs-row"><span>Camera:</span> <strong>${device?.camera}</strong></div>
          </div>
        </div>
      </div>
    `;
  }

  renderNetworkApp(device) {
    return `
      <div class="os-app-window animate-fade-in">
        <div class="app-window-header">
          <button class="back-btn" onclick="window.deviceSimulator.goHome()">‹ Back</button>
          <div class="app-header-title">Network &amp; Proxy Status</div>
          <span class="badge-chip green">Connected</span>
        </div>
        <div class="app-window-body">
          <div class="specs-card glass">
            <div class="specs-row"><span>Carrier Network:</span> <strong>${device?.carrier}</strong></div>
            <div class="specs-row"><span>Cellular Generation:</span> <strong>${device?.networkType}</strong></div>
            <div class="specs-row"><span>Proxy Host:</span> <strong>${device?.proxy?.host}</strong></div>
            <div class="specs-row"><span>Port:</span> <strong>${device?.proxy?.port}</strong></div>
            <div class="specs-row"><span>Latency:</span> <strong style="color: var(--primary);">${device?.proxy?.latency || 32}ms</strong></div>
            <div class="specs-row"><span>Status:</span> <strong style="color: var(--primary);">Active Online</strong></div>
          </div>
        </div>
      </div>
    `;
  }
}

window.deviceSimulator = new DeviceSimulator();

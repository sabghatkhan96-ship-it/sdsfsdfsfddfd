/**
 * SecureVault - LDCloud-Style Virtual Mobile Simulator
 * Interactive, smooth 60fps USA mobile device container running directly in the browser.
 */

class DeviceEmulator {
  constructor() {
    this.currentApp = 'home'; // 'home', 'tiktok', 'facebook', 'adsense', 'browser', 'settings'
    this.isScreenOn = true;
    this.orientation = 'portrait';
    this.simulatedTime = '--:--';
    this.timeInterval = null;
    this.startClock();
  }

  startClock() {
    const updateTime = () => {
      const active = window.profileManager?.activeProfile;
      const tz = active?.location?.timezone || 'America/New_York';
      try {
        const now = new Date();
        this.simulatedTime = now.toLocaleTimeString('en-US', {
          timeZone: tz,
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        });
      } catch (_) {
        const d = new Date();
        this.simulatedTime = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
      }
      const el = document.getElementById('device-clock');
      if (el) el.textContent = this.simulatedTime;
    };
    updateTime();
    this.timeInterval = setInterval(updateTime, 1000);
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
    const screen = document.getElementById('device-screen-body');
    if (screen) {
      screen.style.opacity = this.isScreenOn ? '1' : '0.05';
      screen.style.pointerEvents = this.isScreenOn ? 'auto' : 'none';
    }
  }

  refreshDevice() {
    const screen = document.getElementById('device-screen-body');
    if (screen) {
      screen.style.opacity = '0.3';
      setTimeout(() => {
        screen.style.opacity = '1';
        this.renderScreen();
      }, 300);
    }
  }

  resetFingerprint() {
    const profile = window.profileManager?.activeProfile;
    if (!profile) return;
    const randomHex = Math.random().toString(16).substring(2, 10);
    profile.fingerprintSeed = randomHex;
    profile.proxy.latency = Math.floor(Math.random() * 35) + 30;
    window.profileManager.saveProfiles();
    this.refreshDevice();
  }

  pasteClipboardToDevice() {
    navigator.clipboard.readText().then(text => {
      const input = document.getElementById('device-active-input');
      if (input) {
        input.value = text;
        input.dispatchEvent(new Event('input'));
      }
    }).catch(_ => {});
  }

  renderScreen() {
    const container = document.getElementById('device-app-container');
    if (!container) return;

    const profile = window.profileManager?.activeProfile;
    const isApple = profile?.brand === 'Apple';

    switch (this.currentApp) {
      case 'tiktok':
        container.innerHTML = this.renderTikTokApp(profile);
        break;
      case 'facebook':
        container.innerHTML = this.renderFacebookApp(profile);
        break;
      case 'adsense':
        container.innerHTML = this.renderAdSenseApp(profile);
        break;
      case 'browser':
        container.innerHTML = this.renderBrowserApp(profile);
        break;
      case 'settings':
        container.innerHTML = this.renderSettingsApp(profile);
        break;
      default:
        container.innerHTML = this.renderHomeScreen(profile);
        break;
    }
  }

  renderHomeScreen(profile) {
    const carrier = profile?.location?.carrier || 'T-Mobile US';
    const city = profile?.location?.city || 'New York';
    const state = profile?.location?.state || 'NY';
    const deviceModel = profile?.device || 'Google Pixel 8 Pro';

    return `
      <div class="os-home-screen animate-fade-in">
        <!-- Weather & Widget Header -->
        <div class="os-widget-card glass">
          <div class="os-widget-top">
            <div>
              <div class="widget-city">📍 ${city}, ${state}</div>
              <div class="widget-temp">72°F <span class="widget-cond">Sunny</span></div>
            </div>
            <div class="widget-carrier-badge">
              <span class="status-dot green pulse"></span> 5G UC
            </div>
          </div>
          <div class="widget-proxy-status">
            <span>🛡️ ${profile?.proxy?.type || 'HTTP'} Proxy: ${profile?.proxy?.host || '198.54.120.45'}</span>
            <span class="badge-chip green">${profile?.proxy?.latency || 42}ms</span>
          </div>
        </div>

        <!-- App Grid -->
        <div class="os-apps-grid">
          <div class="os-app-item" onclick="window.deviceEmulator.launchApp('tiktok')">
            <div class="app-icon-box tiktok-gradient">
              <span>🎵</span>
            </div>
            <div class="app-label">TikTok Creator</div>
          </div>

          <div class="os-app-item" onclick="window.deviceEmulator.launchApp('facebook')">
            <div class="app-icon-box fb-gradient">
              <span>📱</span>
            </div>
            <div class="app-label">Meta Ads</div>
          </div>

          <div class="os-app-item" onclick="window.deviceEmulator.launchApp('adsense')">
            <div class="app-icon-box adsense-gradient">
              <span>💵</span>
            </div>
            <div class="app-label">AdSense Hub</div>
          </div>

          <div class="os-app-item" onclick="window.deviceEmulator.launchApp('browser')">
            <div class="app-icon-box chrome-gradient">
              <span>🌐</span>
            </div>
            <div class="app-label">US Chrome</div>
          </div>

          <div class="os-app-item" onclick="window.deviceEmulator.launchApp('settings')">
            <div class="app-icon-box settings-gradient">
              <span>⚙️</span>
            </div>
            <div class="app-label">Device Specs</div>
          </div>

          <div class="os-app-item" onclick="window.appRouter.navigate('vault')">
            <div class="app-icon-box vault-gradient">
              <span>🔐</span>
            </div>
            <div class="app-label">Passwords</div>
          </div>
        </div>

        <!-- Quick Launch Bar -->
        <div class="os-dock-container glass">
          <div class="dock-app-icon" onclick="window.deviceEmulator.launchApp('browser')">🌐</div>
          <div class="dock-app-icon" onclick="window.deviceEmulator.launchApp('tiktok')">🎵</div>
          <div class="dock-app-icon" onclick="window.deviceEmulator.launchApp('adsense')">💵</div>
          <div class="dock-app-icon" onclick="window.deviceEmulator.launchApp('settings')">⚙️</div>
        </div>
      </div>
    `;
  }

  renderTikTokApp(profile) {
    return `
      <div class="os-app-window animate-fade-in">
        <div class="app-window-header">
          <button class="back-btn" onclick="window.deviceEmulator.goHome()">‹ Back</button>
          <div class="app-header-title">TikTok Creator Studio (USA)</div>
          <span class="badge-chip green">US Verified</span>
        </div>
        <div class="app-window-body">
          <div class="sim-banner tiktok-banner">
            <h3>Creator Rewards Program</h3>
            <p>Region: United States (${profile?.location?.city}, ${profile?.location?.state})</p>
          </div>

          <div class="sim-stats-grid">
            <div class="sim-stat-card">
              <div class="stat-num">$1,428.50</div>
              <div class="stat-lbl">Estimated Rewards</div>
            </div>
            <div class="sim-stat-card">
              <div class="stat-num">2.4M</div>
              <div class="stat-lbl">Qualified Views</div>
            </div>
          </div>

          <!-- Autofill Credential Quick Injector -->
          <div class="autofill-injector-box glass">
            <div class="injector-title">🛡️ SecureVault One-Tap Autofill</div>
            <p>Click below to fill stored USA TikTok credentials into this session:</p>
            <button class="btn btn-primary btn-sm" onclick="window.deviceEmulator.autofillFor('TikTok')">
              🔑 Autofill TikTok Account
            </button>
          </div>

          <div class="sim-login-form">
            <label>Username / Email</label>
            <input type="text" id="device-active-input" placeholder="us.creator@gmail.com" class="os-input">
            <label>Password</label>
            <input type="password" placeholder="••••••••••••" class="os-input">
            <button class="btn btn-secondary btn-block">Sign In with US Fingerprint</button>
          </div>
        </div>
      </div>
    `;
  }

  renderFacebookApp(profile) {
    return `
      <div class="os-app-window animate-fade-in">
        <div class="app-window-header">
          <button class="back-btn" onclick="window.deviceEmulator.goHome()">‹ Back</button>
          <div class="app-header-title">Meta Ads Manager (USA)</div>
          <span class="badge-chip cyan">Active</span>
        </div>
        <div class="app-window-body">
          <div class="sim-banner fb-banner">
            <h3>US Business Portfolio</h3>
            <p>Proxy: ${profile?.proxy?.host} • ${profile?.location?.carrier}</p>
          </div>

          <div class="sim-stats-grid">
            <div class="sim-stat-card">
              <div class="stat-num">$452.10</div>
              <div class="stat-lbl">Spent Today</div>
            </div>
            <div class="sim-stat-card">
              <div class="stat-num">3.82x</div>
              <div class="stat-lbl">ROAS Average</div>
            </div>
          </div>

          <div class="autofill-injector-box glass">
            <div class="injector-title">🛡️ SecureVault Autofill</div>
            <button class="btn btn-primary btn-sm" onclick="window.deviceEmulator.autofillFor('Facebook')">
              🔑 Autofill Meta Ads Account
            </button>
          </div>
        </div>
      </div>
    `;
  }

  renderAdSenseApp(profile) {
    return `
      <div class="os-app-window animate-fade-in">
        <div class="app-window-header">
          <button class="back-btn" onclick="window.deviceEmulator.goHome()">‹ Back</button>
          <div class="app-header-title">Google AdSense Console</div>
          <span class="badge-chip green">Verified US</span>
        </div>
        <div class="app-window-body">
          <div class="sim-banner adsense-banner">
            <h3>US Publisher Earnings</h3>
            <p>Account Tax Status: W-8BEN / US Citizen Verified</p>
          </div>

          <div class="sim-stats-grid">
            <div class="sim-stat-card">
              <div class="stat-num">$3,842.19</div>
              <div class="stat-lbl">Balance (USD)</div>
            </div>
            <div class="sim-stat-card">
              <div class="stat-num">$128.40</div>
              <div class="stat-lbl">Today so far</div>
            </div>
          </div>

          <div class="autofill-injector-box glass">
            <div class="injector-title">🛡️ SecureVault Autofill</div>
            <button class="btn btn-primary btn-sm" onclick="window.deviceEmulator.autofillFor('AdSense')">
              🔑 Autofill AdSense Login
            </button>
          </div>
        </div>
      </div>
    `;
  }

  renderBrowserApp(profile) {
    return `
      <div class="os-app-window animate-fade-in">
        <div class="app-window-header">
          <button class="back-btn" onclick="window.deviceEmulator.goHome()">‹ Home</button>
          <div class="browser-url-bar">
            <span>🔒</span>
            <input type="text" value="${profile?.targetUrl || 'https://whoer.net'}" class="browser-input" readonly>
          </div>
        </div>
        <div class="app-window-body">
          <div class="browser-sim-card glass">
            <h4>IP &amp; Fingerprint Audit</h4>
            <div class="fingerprint-row"><span>IP Address:</span> <strong>${profile?.proxy?.host || '198.54.120.45'}</strong></div>
            <div class="fingerprint-row"><span>Country:</span> <strong>United States (US)</strong></div>
            <div class="fingerprint-row"><span>Region:</span> <strong>${profile?.location?.city}, ${profile?.location?.state}</strong></div>
            <div class="fingerprint-row"><span>Timezone:</span> <strong>${profile?.location?.timezone}</strong></div>
            <div class="fingerprint-row"><span>Browser:</span> <strong>Chrome 128 / ${profile?.os}</strong></div>
            <div class="fingerprint-row"><span>GPU:</span> <strong>${profile?.gpu}</strong></div>
            <div class="fingerprint-row"><span>Anonymity:</span> <strong style="color: #10b981;">100% Clean USA Residential</strong></div>
          </div>
        </div>
      </div>
    `;
  }

  renderSettingsApp(profile) {
    return `
      <div class="os-app-window animate-fade-in">
        <div class="app-window-header">
          <button class="back-btn" onclick="window.deviceEmulator.goHome()">‹ Back</button>
          <div class="app-header-title">About Phone (Specs)</div>
        </div>
        <div class="app-window-body">
          <div class="specs-card glass">
            <div class="specs-row"><span>Device Model:</span> <strong>${profile?.device}</strong></div>
            <div class="specs-row"><span>Brand:</span> <strong>${profile?.brand}</strong></div>
            <div class="specs-row"><span>Operating System:</span> <strong>${profile?.os}</strong></div>
            <div class="specs-row"><span>Screen Resolution:</span> <strong>${profile?.screen?.resolution}</strong></div>
            <div class="specs-row"><span>RAM:</span> <strong>${profile?.ram}</strong></div>
            <div class="specs-row"><span>CPU Cores:</span> <strong>${profile?.cores} Cores</strong></div>
            <div class="specs-row"><span>GPU Renderer:</span> <strong>${profile?.gpu}</strong></div>
            <div class="specs-row"><span>Carrier:</span> <strong>${profile?.location?.carrier}</strong></div>
            <div class="specs-row"><span>Language:</span> <strong>${profile?.location?.language}</strong></div>
          </div>

          <div style="margin-top: 14px;">
            <button class="btn btn-secondary btn-block" onclick="window.deviceEmulator.resetFingerprint()">
              ⚡ Regenerate Hardware Fingerprint
            </button>
          </div>
        </div>
      </div>
    `;
  }

  autofillFor(tag) {
    const creds = window.passwordVault.getCredentials();
    const match = creds.find(c => c.title.toLowerCase().includes(tag.toLowerCase()) || c.domain.toLowerCase().includes(tag.toLowerCase()));
    if (match) {
      const input = document.getElementById('device-active-input');
      if (input) input.value = match.username;
      alert(`Autofilled credentials for ${match.title} (${match.username})`);
    } else {
      alert(`No saved credentials found for ${tag}. Add one in the Passwords Vault.`);
    }
  }
}

window.deviceEmulator = new DeviceEmulator();

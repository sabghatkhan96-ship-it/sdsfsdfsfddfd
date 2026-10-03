/**
 * SecureVault - USA Mobile Profile & Device Automation Engine
 * Simulates high-trust USA mobile hardware fingerprints, carriers, and proxy bindings.
 */

const USA_CITIES = [
  { city: 'New York', state: 'NY', timezone: 'America/New_York', tzOffset: -4, carrier: 'T-Mobile US' },
  { city: 'Los Angeles', state: 'CA', timezone: 'America/Los_Angeles', tzOffset: -7, carrier: 'Verizon Wireless' },
  { city: 'Chicago', state: 'IL', timezone: 'America/Chicago', tzOffset: -5, carrier: 'AT&T Mobility' },
  { city: 'Dallas', state: 'TX', timezone: 'America/Chicago', tzOffset: -5, carrier: 'T-Mobile US' },
  { city: 'Miami', state: 'FL', timezone: 'America/New_York', tzOffset: -4, carrier: 'Verizon Wireless' },
  { city: 'Seattle', state: 'WA', timezone: 'America/Los_Angeles', tzOffset: -7, carrier: 'AT&T Mobility' }
];

const DEVICE_TEMPLATES = [
  {
    model: 'Google Pixel 8 Pro',
    brand: 'Google',
    os: 'Android 14',
    screen: { width: 412, height: 915, dpr: 3.5, resolution: '1080x2400' },
    gpu: 'Google Tensor G3 (Mali-G715)',
    ram: '12 GB',
    cores: 8,
    userAgent: 'Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro Build/UD1A.231105.004; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/128.0.6613.88 Mobile Safari/537.36'
  },
  {
    model: 'Apple iPhone 15 Pro Max',
    brand: 'Apple',
    os: 'iOS 17.5.1',
    screen: { width: 430, height: 932, dpr: 3.0, resolution: '1179x2556' },
    gpu: 'Apple A17 Pro GPU',
    ram: '8 GB',
    cores: 6,
    userAgent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1'
  },
  {
    model: 'Samsung Galaxy S24 Ultra',
    brand: 'Samsung',
    os: 'Android 14',
    screen: { width: 412, height: 915, dpr: 3.5, resolution: '1440x3120' },
    gpu: 'Qualcomm Adreno (TM) 750',
    ram: '12 GB',
    cores: 8,
    userAgent: 'Mozilla/5.0 (Linux; Android 14; SM-S928U Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/128.0.6613.88 Mobile Safari/537.36'
  },
  {
    model: 'OnePlus 12',
    brand: 'OnePlus',
    os: 'Android 14',
    screen: { width: 412, height: 919, dpr: 3.5, resolution: '1440x3168' },
    gpu: 'Qualcomm Adreno (TM) 750',
    ram: '16 GB',
    cores: 8,
    userAgent: 'Mozilla/5.0 (Linux; Android 14; CPH2583 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/128.0.6613.88 Mobile Safari/537.36'
  }
];

class ProfileManager {
  constructor() {
    this.storageKey = 'securevault_usa_profiles';
    this.activeProfileKey = 'securevault_active_profile_id';
    this.profiles = this.loadProfiles();
    this.activeProfile = this.getActiveProfile();
  }

  loadProfiles() {
    try {
      const data = localStorage.getItem(this.storageKey);
      if (data) {
        return JSON.parse(data);
      }
    } catch (e) {
      console.error('Failed to load profiles:', e);
    }
    return this.createDefaultProfiles();
  }

  saveProfiles() {
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(this.profiles));
    } catch (e) {
      console.error('Failed to save profiles:', e);
    }
  }

  createDefaultProfiles() {
    const defaults = [
      this.generateRandomProfile({
        name: 'TikTok US Creator #1',
        tag: 'TikTok',
        targetUrl: 'https://www.tiktok.com/creator-center',
        cityIndex: 0 // New York
      }),
      this.generateRandomProfile({
        name: 'Facebook Ads US Hub',
        tag: 'Facebook',
        targetUrl: 'https://adsmanager.facebook.com',
        cityIndex: 1 // Los Angeles
      }),
      this.generateRandomProfile({
        name: 'Google AdSense USA #1',
        tag: 'AdSense',
        targetUrl: 'https://adsense.google.com',
        cityIndex: 2 // Chicago
      })
    ];
    this.profiles = defaults;
    this.saveProfiles();
    return defaults;
  }

  generateRandomProfile(overrides = {}) {
    const template = DEVICE_TEMPLATES[Math.floor(Math.random() * DEVICE_TEMPLATES.length)];
    const location = USA_CITIES[overrides.cityIndex !== undefined ? overrides.cityIndex : Math.floor(Math.random() * USA_CITIES.length)];
    const randomHex = Math.random().toString(16).substring(2, 10);

    return {
      id: 'prof_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5),
      name: overrides.name || `USA Profile (${template.model.split(' ')[1] || 'Device'})`,
      tag: overrides.tag || 'General',
      targetUrl: overrides.targetUrl || 'https://google.com',
      device: template.model,
      brand: template.brand,
      os: template.os,
      userAgent: template.userAgent,
      screen: template.screen,
      gpu: template.gpu,
      ram: template.ram,
      cores: template.cores,
      location: {
        city: location.city,
        state: location.state,
        country: 'United States',
        countryCode: 'US',
        timezone: location.timezone,
        carrier: location.carrier,
        language: 'en-US,en;q=0.9'
      },
      proxy: overrides.proxy || {
        enabled: true,
        type: 'HTTP',
        host: '198.54.120.' + (Math.floor(Math.random() * 200) + 10),
        port: '8080',
        username: 'us_resi_' + randomHex,
        password: '••••••••',
        status: 'Online',
        latency: Math.floor(Math.random() * 45) + 32, // 32ms - 77ms
        isp: location.carrier + ' Broadband'
      },
      createdAt: Date.now(),
      status: 'Active'
    };
  }

  addProfile(profileData) {
    this.profiles.unshift(profileData);
    this.saveProfiles();
    if (!this.activeProfile) {
      this.setActiveProfile(profileData.id);
    }
    return profileData;
  }

  updateProfile(id, updatedFields) {
    const index = this.profiles.findIndex(p => p.id === id);
    if (index !== -1) {
      this.profiles[index] = { ...this.profiles[index], ...updatedFields, updatedAt: Date.now() };
      this.saveProfiles();
      if (this.activeProfile && this.activeProfile.id === id) {
        this.activeProfile = this.profiles[index];
      }
      return this.profiles[index];
    }
    return null;
  }

  deleteProfile(id) {
    this.profiles = this.profiles.filter(p => p.id !== id);
    this.saveProfiles();
    if (this.activeProfile && this.activeProfile.id === id) {
      this.activeProfile = this.profiles[0] || null;
      if (this.activeProfile) {
        localStorage.setItem(this.activeProfileKey, this.activeProfile.id);
      } else {
        localStorage.removeItem(this.activeProfileKey);
      }
    }
  }

  setActiveProfile(id) {
    const found = this.profiles.find(p => p.id === id);
    if (found) {
      this.activeProfile = found;
      localStorage.setItem(this.activeProfileKey, id);
      return found;
    }
    return null;
  }

  getActiveProfile() {
    const activeId = localStorage.getItem(this.activeProfileKey);
    if (activeId) {
      const found = this.profiles.find(p => p.id === activeId);
      if (found) return found;
    }
    const fallback = this.profiles[0] || null;
    if (fallback) {
      localStorage.setItem(this.activeProfileKey, fallback.id);
    }
    return fallback;
  }
}

window.profileManager = new ProfileManager();

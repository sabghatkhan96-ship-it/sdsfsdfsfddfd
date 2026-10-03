/**
 * SecureVault - Real Mobile Device Profile Manager
 * Manages clean, authentic mobile device profiles, specifications, and proxy bindings.
 */

const REAL_DEVICES = [
  {
    model: 'Google Pixel 8 Pro',
    brand: 'Google',
    os: 'Android 14 (UpsideDownCake)',
    chipset: 'Google Tensor G3 (Titan M2 Security)',
    ram: '12 GB LPDDR5X',
    storage: '256 GB UFS 4.0',
    screen: '6.7" OLED (1344 x 2992, 120Hz LTPO)',
    battery: '5050 mAh (98% Health)',
    camera: '50 MP Dual Pixel + 48 MP Quad PD',
    carrier: 'Verizon 5G Ultra Wideband',
    networkType: '5G UW / Wi-Fi 7',
    defaultTag: 'Main'
  },
  {
    model: 'Samsung Galaxy S24 Ultra',
    brand: 'Samsung',
    os: 'Android 14 (One UI 6.1)',
    chipset: 'Qualcomm Snapdragon 8 Gen 3 for Galaxy',
    ram: '12 GB LPDDR5X',
    storage: '512 GB UFS 4.0',
    screen: '6.8" Dynamic AMOLED 2X (1440 x 3120)',
    battery: '5000 mAh (100% Health)',
    camera: '200 MP ISOCELL HP2 + 50 MP Periscope',
    carrier: 'T-Mobile 5G UC',
    networkType: '5G UC / Wi-Fi 7',
    defaultTag: 'Work'
  },
  {
    model: 'Apple iPhone 15 Pro Max',
    brand: 'Apple',
    os: 'iOS 17.5.1',
    chipset: 'Apple A17 Pro (6-core CPU, 6-core GPU)',
    ram: '8 GB LPDDR5',
    storage: '256 GB NVMe',
    screen: '6.7" Super Retina XDR (1179 x 2556, ProMotion)',
    battery: '4422 mAh (99% Health)',
    camera: '48 MP Main + 12 MP 5x Telephoto',
    carrier: 'AT&T 5G+',
    networkType: '5G+ / Wi-Fi 6E',
    defaultTag: 'Personal'
  },
  {
    model: 'Xiaomi 14 Ultra',
    brand: 'Xiaomi',
    os: 'Xiaomi HyperOS (Android 14)',
    chipset: 'Snapdragon 8 Gen 3',
    ram: '16 GB LPDDR5X',
    storage: '512 GB UFS 4.0',
    screen: '6.73" LTPO AMOLED (1440 x 3200)',
    battery: '5000 mAh (97% Health)',
    camera: '50 MP 1-inch LYT-900 Quad Camera',
    carrier: 'Vodafone 5G Gigabit',
    networkType: '5G / Wi-Fi 7',
    defaultTag: 'Testing'
  }
];

class DeviceManager {
  constructor() {
    this.storageKey = 'securevault_real_devices';
    this.activeDeviceKey = 'securevault_active_device_id';
    this.devices = this.loadDevices();
    this.activeDevice = this.getActiveDevice();
  }

  loadDevices() {
    try {
      const data = localStorage.getItem(this.storageKey);
      if (data) return JSON.parse(data);
    } catch (_) {}
    return this.createDefaultDevices();
  }

  saveDevices() {
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(this.devices));
    } catch (_) {}
  }

  createDefaultDevices() {
    const defaults = REAL_DEVICES.map((d, index) => ({
      id: 'dev_' + Date.now() + '_' + index,
      name: `${d.brand} ${d.model.split(' ')[1] || 'Device'} (${d.defaultTag})`,
      tag: d.defaultTag,
      ...d,
      proxy: {
        enabled: true,
        type: 'HTTP',
        host: '104.28.19.' + (index * 25 + 40),
        port: '8080',
        username: 'user_auth_' + (index + 1),
        password: '••••••••',
        latency: Math.floor(Math.random() * 25) + 24, // 24ms - 49ms
        status: 'Online'
      },
      createdAt: Date.now()
    }));
    this.devices = defaults;
    this.saveDevices();
    return defaults;
  }

  createRandomDevice(overrides = {}) {
    const template = REAL_DEVICES[Math.floor(Math.random() * REAL_DEVICES.length)];
    const id = 'dev_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5);
    const newDev = {
      id,
      name: overrides.name || `${template.brand} ${template.model.split(' ')[1] || 'Device'} #` + Math.floor(Math.random() * 90 + 10),
      tag: overrides.tag || template.defaultTag,
      ...template,
      proxy: overrides.proxy || {
        enabled: true,
        type: 'HTTP',
        host: '142.250.' + Math.floor(Math.random() * 200) + '.' + Math.floor(Math.random() * 200),
        port: '8080',
        username: 'auth_' + Math.random().toString(36).substr(2, 6),
        password: '••••••••',
        latency: Math.floor(Math.random() * 30) + 28,
        status: 'Online'
      },
      createdAt: Date.now()
    };
    this.devices.unshift(newDev);
    this.saveDevices();
    return newDev;
  }

  deleteDevice(id) {
    this.devices = this.devices.filter(d => d.id !== id);
    this.saveDevices();
    if (this.activeDevice && this.activeDevice.id === id) {
      this.activeDevice = this.devices[0] || null;
      if (this.activeDevice) {
        localStorage.setItem(this.activeDeviceKey, this.activeDevice.id);
      } else {
        localStorage.removeItem(this.activeDeviceKey);
      }
    }
  }

  setActiveDevice(id) {
    const found = this.devices.find(d => d.id === id);
    if (found) {
      this.activeDevice = found;
      localStorage.setItem(this.activeDeviceKey, id);
      return found;
    }
    return null;
  }

  getActiveDevice() {
    const activeId = localStorage.getItem(this.activeDeviceKey);
    if (activeId) {
      const found = this.devices.find(d => d.id === activeId);
      if (found) return found;
    }
    const fallback = this.devices[0] || null;
    if (fallback) {
      localStorage.setItem(this.activeDeviceKey, fallback.id);
    }
    return fallback;
  }
}

window.deviceManager = new DeviceManager();

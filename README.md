# 📱 Task Server
> **Automate your SSH workflow. Control your servers from anywhere.**

Task Server is an Android app designed by **Pietro Olivero** for anyone who manages servers and wants to eliminate repetitive typing. Save your servers, define your most-used tasks, and execute them with a single tap — or drop into a full interactive SSH terminal with ANSI color and virtual keyboard support whenever you need complete control.

---

## ⚡ Quick Links

<p align="center">
  <a href="https://github.com/PitStop13/Task_Server/releases/latest">
    <img src="https://img.shields.io/badge/Download-Latest%20APK-00E676?style=for-the-badge&logo=android&logoColor=white&labelColor=263238" alt="Download APK" />
  </a>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <a href="https://github.com/PitStop13/Task_Server/releases">
    <img src="https://img.shields.io/github/v/release/PitStop13/Task_Server?style=for-the-badge&color=2196F3&label=Release" alt="Latest Release" />
  </a>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <a href="https://github.com/PitStop13">
    <img src="https://img.shields.io/badge/Developer-PitStop13-24292E?style=for-the-badge&logo=github&logoColor=white&labelColor=171515" alt="GitHub Profile" />
  </a>
</p>

<p align="center">
  <sub>💡 <b>Just want the app?</b> Click the green button to grab the latest <code>.apk</code> instantly from GitHub Releases.</sub>
</p>

---

## ✨ Features

- 🖥️ **Server Presets & Live Health Monitoring** — Save host, port, and credentials once. Real-time background connectivity polling continuously monitors server availability (Online, Offline, Checking).
- ⚡ **One-Tap Task Automation** — Define multi-command shell scripts and launch them in a single tap. Preserves working directory changes (`cd`) and supports fail-fast execution (`set -e`).
- 💻 **Interactive SSH Terminal (xterm.js)** — Full-featured interactive terminal with VT100/ANSI color support, dynamic resizing, and a dedicated mobile accessory keyboard bar (ESC, TAB, arrow navigation, Ctrl+C).
- 🔐 **Biometric Security & Encrypted Vault** — Secure your credentials with Fingerprint/Face Unlock via AndroidX Biometric. All SSH passwords and private keys are encrypted at rest with Android Keystore & EncryptedSharedPreferences.
- 🛡️ **Sudo Support** — Seamlessly execute privileged commands with automated sudo password injection.
- 📜 **Audit History & Activity Logs** — Track executed tasks with timestamps, exit codes, and detailed output logs.
- 📦 **Lightweight & Modern** — Fast Material 3 interface built with Jetpack Compose, featuring native dark and light mode themes.

---

## 🎯 Who is this for?
Anyone who manages servers from their phone:
- **Homelab enthusiasts** — restart services, check disk space, reboot machines
- **Developers** — deploy, restart daemons, tail logs on the go
- **NAS / home server owners** — quick maintenance without sitting at a desk
- **SysAdmins** — run common scripts without opening a laptop

---

## 📸 Screenshots

### 🛠️ Main Overview & Configuration
| Home | New Server | Edit/New Task | Settings | Settings |
| :---: | :---: | :---: | :---: | :---: |
| <img src="Screenshots/home.png" width="160" alt="Home" /> | <img src="Screenshots/newServer.png" width="160" alt="New Server" /> | <img src="Screenshots/editTask.png" width="160" alt="Edit Task" /> | <img src="Screenshots/settings1.png" width="160" alt="Settings 1" /> | <img src="Screenshots/settings2.png" width="160" alt="Settings 2" /> |

### 💻 Connection & Management
| Servers List | Tasks List | SSH Dashboard | SSH Terminal | Activity Logs |
| :---: | :---: | :---: | :---: | :---: |
| <img src="Screenshots/server.png" width="160" alt="Servers" /> | <img src="Screenshots/task.png" width="160" alt="Tasks" /> | <img src="Screenshots/sshDashboard.png" width="160" alt="SSH Dashboard" /> | <img src="Screenshots/sshTerminal.png" width="160" alt="SSH Terminal" /> | <img src="Screenshots/log.png" width="160" alt="Logs" /> |

---

## 🛠️ Built With

- **Language:** Kotlin 2.1
- **UI Toolkit:** Jetpack Compose & Material 3
- **Architecture:** MVVM + Clean Architecture with Kotlin Coroutines & StateFlow
- **Dependency Injection:** Dagger Hilt
- **Local Database:** Room
- **Security:** AndroidX Biometric & EncryptedSharedPreferences
- **SSH & Cryptography:** SSHJ & BouncyCastle
- **Terminal Core:** xterm.js inside WebView with bidirectional byte streaming
- **Minimum Requirements:** Android 8.0 (API Level 26) or higher

---

## 🚀 How to Install

1. **Download:** Click the [Latest APK](https://github.com/PitStop13/Task_Server/releases/latest) button at the top of this page.
2. **Permissions:** Enable *Install from unknown sources* in your Android settings if prompted.
3. **Launch:** Install the file and you're good to go — no account required.

---

## 🤝 Contributing
Found a bug? Have a feature idea? Open an [Issue](https://github.com/PitStop13/Task_Server/issues) or submit a Pull Request. All contributions are welcome!

---

## 📄 License
MIT License — Copyright (c) 2026 **Pietro Olivero** (Main Developer & Designer).
Free to use, modify and distribute.

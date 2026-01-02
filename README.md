# Chronos - Brutalist Alarm Clock for Android 14

<div align="center">

**An uncompromising alarm clock that prevents oversleeping through wake-up challenges and security hardening**

[![Android 14](https://img.shields.io/badge/Android-14%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-BOM%202024.02.01-orange.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 🎯 Overview

Chronos is a feature-rich alarm clock app built for Android 14+ with a brutalist design aesthetic and comprehensive anti-bypass security. Unlike standard alarm apps, Chronos requires you to complete wake-up challenges and includes protections against all common methods users might attempt to silence or disable alarms.

### Key Features

✅ **6 Wake-Up Challenge Types**
- 🧮 Math Problems (configurable difficulty)
- ⚡ Burst Tapping (rapid tap counter)
- 🧠 Memory Pattern (sequence matching)
- ⌨️ Typing Challenge (phrase replication)
- 🏃 Velocity Challenge (GPS speed tracking)
- 📡 Bluetooth Challenge (device connection)

✅ **Advanced Protection**
- 🔊 Volume override (prevents muting)
- 🔋 Battery optimization bypass
- 🔕 Do Not Disturb override
- 🔄 Reboot persistence
- 🛡️ Force-close resistance
- 📱 Notification protection

✅ **Smart Features**
- 👁️ Wake-up check (verify you're actually awake)
- 🚨 Emergency contacts (SMS/call if you don't wake)
- 🎵 Custom audio sources (generated, system, URL, file)
- ⏰ Multiple alarms with day scheduling
- 🌙 12h/24h time format support

✅ **Brutalist Design**
- Hard shadows and borders
- Space Grotesk + Inter typography
- Spring-based animations
- Haptic feedback on all interactions
- Dark/light theme support

---

## 📸 Screenshots

<!-- Add screenshots here when available -->
```
android-screenshot.png - Main alarm list
android-screenshot-2.png - Alarm creation
android-screenshot-3.png - Challenge configuration
android-screenshot-4.png - Active alarm screen
android-screenshot-5.png - Settings screen
android-screenshot-6.png - Security status
```

---

## 🏗️ Architecture

### Tech Stack

**Core**
- Kotlin 1.9.22
- Android 14 (API 34)
- Jetpack Compose (BOM 2024.02.01)
- Material 3 Design

**Data Layer**
- Room Database 2.6.1
- DataStore Preferences
- Kotlin Serialization

**Services**
- Foreground Services
- AlarmManager for exact scheduling
- WorkManager for background tasks

**Location & Connectivity**
- Google Play Services Location
- Bluetooth LE
- SMS & Phone APIs

**Audio**
- Media3 ExoPlayer
- SoundPool for feedback
- AudioManager for volume control

### Project Structure

```
com.chronos.alarm/
├── data/              # Room database, DAOs, repositories
├── domain/            # Models, use cases, scheduler
├── ui/
│   ├── screens/       # Home, Alarm, Settings screens
│   ├── components/    # Reusable UI components
│   └── theme/         # Typography, colors, animations
├── challenge/         # 6 challenge implementations
├── service/           # Foreground services, managers
├── protection/        # Security & permission handling
└── utils/             # Extensions, helpers
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Gradle 8.11.1

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/luch4ik/chronos.git
cd chronos
```

2. **Open in Android Studio**
```bash
studio .
```

3. **Build the project**
```bash
./gradlew assembleDebug
```

4. **Install on device**
```bash
adb install app/build/outputs/apk/debug/chronos-debug.apk
```

### CI/CD

GitHub Actions automatically builds APKs on every push:
- Navigate to **Actions** tab
- Select latest workflow run
- Download APK artifacts (debug or release)
- Artifacts retained for 30 days

---

## 🔒 Security & Permissions

Chronos requires several permissions for full functionality:

### Critical Permissions (Required)
- `SCHEDULE_EXACT_ALARM` - Precise alarm timing
- `POST_NOTIFICATIONS` - Show alarm notifications
- `WAKE_LOCK` - Keep device awake during alarm
- Battery optimization exemption - Survive doze mode

### High Priority (Recommended)
- Notification policy access - Bypass Do Not Disturb
- `RECEIVE_BOOT_COMPLETED` - Reschedule after reboot

### Optional (For Features)
- `ACCESS_FINE_LOCATION` - Velocity Challenge
- `BLUETOOTH_CONNECT` - Bluetooth Challenge
- `SEND_SMS` - Emergency Contact SMS
- `CALL_PHONE` - Emergency Contact calls

### Security Testing

Run comprehensive security audit:
```kotlin
val securityTester = SecurityTester(context)
val report = securityTester.runFullSecurityAudit()
println(report.toReadableReport())
```

See [SECURITY_TEST_REPORT.md](SECURITY_TEST_REPORT.md) for detailed security analysis.

---

## 📖 Usage

### Creating an Alarm

1. Tap **NEW ALARM** button
2. Select time using wheel picker
3. Choose days (once, daily, weekdays, etc.)
4. Add challenges (tap icons to add, configure parameters)
5. Configure audio source
6. Enable optional features:
   - Wake-up check (verify awake after N minutes)
   - Emergency contact (SMS/call if not dismissed)
7. Tap **SAVE ALARM**

### Challenge Configuration

**Math Challenge**
- Count: Number of problems (1-10)
- Difficulty: Normal (1-50) or Hard (10-100)

**Burst Challenge**
- Taps: Target tap count (10-200)

**Memory Challenge**
- Rounds: Number of sequences (1-10)
- Length: Sequence length (3-9)

**Typing Challenge**
- Phrases: Number to type (1-5)
- Mode: Simple words or famous quotes

**Velocity Challenge**
- Speed: Target speed in km/h (1-50)
- Requires GPS and movement

**Bluetooth Challenge**
- Device: Select paired Bluetooth device
- Requires device pairing first

### Settings

Access via gear icon:
- **Time Format**: 12h or 24h
- **Theme**: Light, Dark, or System
- **Protection**: Volume override, reboot protection, uninstall warning

---

## 🧪 Testing

### Automated Tests
```bash
./gradlew test
```

### Security Tests
Run the security test suite to verify all protections:
```kotlin
// In your test or Activity
val securityTester = SecurityTester(context)
launch {
    val report = securityTester.runFullSecurityAudit()
    // Score should be 85+/100
    assert(report.overallSecurityScore >= 85)
}
```

### Manual Testing Checklist
- [ ] Set alarm for 1 minute from now
- [ ] Try muting volume (should restore)
- [ ] Try force-closing app (should survive)
- [ ] Try swiping notification (should persist)
- [ ] Enable Do Not Disturb (alarm should still ring)
- [ ] Reboot device (alarms should reschedule)
- [ ] Complete each challenge type
- [ ] Test wake-up check feature
- [ ] Test emergency contact (with caution!)

---

## 🛠️ Development

### Branch Strategy

**Main Branches**
- `master` - Stable, production-ready code
- Feature branches: `claude/*-gAHq9`

**Active Pull Requests**
1. `claude/fix-timepicker-hours-gAHq9` - TimePicker fixes + CI/CD
2. `claude/phase2-design-polish-gAHq9` - Design system polish
3. `claude/implement-challenges-gAHq9` - Velocity + Bluetooth challenges
4. `claude/advanced-features-gAHq9` - WakeUpCheck, EmergencyContact, Audio
5. `claude/settings-polish-gAHq9` - Documentation updates
6. `claude/security-hardening-gAHq9` - Security protections

### Code Style

- Kotlin official style guide
- 4-space indentation
- Max line length: 120 characters
- Meaningful variable names
- Comments for complex logic

### Adding a New Challenge

1. Create file in `challenge/` package
2. Implement challenge UI with Compose
3. Call `onSuccess()` when completed
4. Add to `AlarmScreen.kt` when block
5. Add icon to `ChallengeConfigurator.kt`
6. Test thoroughly

---

## 📚 Documentation

- [CHANGELOG.md](CHANGELOG.md) - Complete project history
- [SECURITY_TEST_REPORT.md](SECURITY_TEST_REPORT.md) - Security audit report
- [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) - Original technical plan
- [AGENT_GUIDE.md](AGENT_GUIDE.md) - AI development workflow

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

### Areas for Contribution
- [ ] Cloud backup for alarms
- [ ] Widget support
- [ ] Wear OS companion app
- [ ] More challenge types
- [ ] Localization (i18n)
- [ ] Custom sound files
- [ ] Tablet/foldable optimization

---

## 📋 Roadmap

### Version 1.0 (Current)
- ✅ All 6 challenge types
- ✅ Security hardening
- ✅ Advanced features
- ✅ Brutalist design system

### Version 1.1 (Planned)
- [ ] Cloud backup via Google Drive
- [ ] Import/export alarms
- [ ] Alarm statistics and history
- [ ] Custom sound library
- [ ] More theme options

### Version 2.0 (Future)
- [ ] Wear OS integration
- [ ] Home screen widgets
- [ ] Bedtime routine features
- [ ] Sleep tracking
- [ ] Smart alarm (wake during light sleep)

---

## 🐛 Known Issues

- Building in Docker requires network access for Gradle downloads
- Battery optimization prompt may appear multiple times
- Some custom ROMs may bypass protections
- Safe mode boot disables app (system limitation)

See [GitHub Issues](https://github.com/luch4ik/chronos/issues) for full list.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- React version: [luch4ik/chronos](https://github.com/luch4ik/chronos)
- Material Design 3 guidelines
- Jetpack Compose documentation
- Android developer community

---

## 📞 Support

- 📧 Email: [Create issue](https://github.com/luch4ik/chronos/issues/new)
- 💬 Discussions: [GitHub Discussions](https://github.com/luch4ik/chronos/discussions)
- 🐛 Bug Reports: [GitHub Issues](https://github.com/luch4ik/chronos/issues)

---

<div align="center">

**Built with ❤️ using Kotlin & Jetpack Compose**

⭐ Star this repo if you find it useful!

</div>

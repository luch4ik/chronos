# Chronos Deployment Guide

**Version:** 1.0.0
**Target:** Android 14+ (API 34)
**Last Updated:** 2026-01-02

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Building the APK](#building-the-apk)
3. [Signing for Release](#signing-for-release)
4. [Testing Checklist](#testing-checklist)
5. [Google Play Store](#google-play-store)
6. [Distribution Channels](#distribution-channels)
7. [Post-Deployment](#post-deployment)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Tools
- ✅ Android Studio Hedgehog (2023.1.1) or later
- ✅ JDK 17
- ✅ Android SDK 34
- ✅ Gradle 8.11.1
- ✅ Git
- ✅ Physical Android device (for testing)

### Required Accounts
- ✅ Google Play Console account ($25 one-time fee)
- ✅ Signing keystore (create if not exists)

### Environment Setup
```bash
# Verify Java version
java -version  # Should show Java 17

# Verify Android SDK
echo $ANDROID_HOME  # Should point to SDK location

# Verify Gradle
./gradlew --version  # Should show 8.11.1
```

---

## Building the APK

### 1. Clean Build

```bash
# Clean previous builds
./gradlew clean

# Verify no old artifacts
rm -rf app/build/outputs/apk/
```

### 2. Build Debug APK (Testing)

```bash
# Build debug version
./gradlew assembleDebug

# Output location
# app/build/outputs/apk/debug/app-debug.apk
```

### 3. Build Release APK (Production)

```bash
# Build release version (unsigned)
./gradlew assembleRelease

# Output location
# app/build/outputs/apk/release/app-release-unsigned.apk
```

### 4. Build via GitHub Actions

**Automatic builds on every push:**
1. Push to `master` or `main` branch
2. Navigate to Actions tab
3. Wait for build to complete (~5-10 minutes)
4. Download APK artifact
5. Artifacts retained for 30 days

---

## Signing for Release

### 1. Generate Keystore (First Time Only)

```bash
keytool -genkey -v \
  -keystore chronos-release.keystore \
  -alias chronos-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Answer prompts:
# - Password: [SECURE PASSWORD - SAVE THIS!]
# - First/Last name: Your name
# - Organization: Your company/self
# - City, State, Country: Your location
```

**⚠️ CRITICAL: Backup this keystore file!**
- Store in password manager
- Upload to secure cloud storage
- Keep offline copy
- You cannot recover this if lost!

### 2. Configure Signing in Gradle

Create `keystore.properties` in project root:
```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=chronos-key
storeFile=/path/to/chronos-release.keystore
```

**⚠️ Add to .gitignore:**
```bash
echo "keystore.properties" >> .gitignore
echo "*.keystore" >> .gitignore
```

### 3. Update build.gradle.kts

Add to `app/build.gradle.kts`:
```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 4. Build Signed APK

```bash
./gradlew assembleRelease

# Output (signed):
# app/build/outputs/apk/release/app-release.apk
```

### 5. Verify Signature

```bash
# Verify APK is signed
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Should show: "jar verified."
```

---

## Testing Checklist

### Pre-Release Testing (CRITICAL)

#### Device Testing
- [ ] Test on Android 14 device
- [ ] Test on Android 13 device (if supporting)
- [ ] Test on different screen sizes (phone, tablet)
- [ ] Test on different manufacturers (Samsung, Google, Xiaomi)

#### Functional Testing
- [ ] Create alarm → rings at correct time
- [ ] Complete all 6 challenge types
- [ ] Test wake-up check feature
- [ ] Test emergency contact (USE TEST NUMBER!)
- [ ] Volume override works
- [ ] Force close → app survives
- [ ] Reboot → alarms reschedule
- [ ] Do Not Disturb → alarm still rings
- [ ] Battery saver → alarm still rings

#### Permission Testing
- [ ] Grant all permissions → verify works
- [ ] Deny each permission → verify graceful handling
- [ ] Revoke permissions → verify re-request flow
- [ ] Battery optimization → verify exemption request

#### Edge Cases
- [ ] No alarms set → empty state shows
- [ ] 10+ alarms set → scrolling works
- [ ] Very long alarm labels → text truncates
- [ ] Rapid alarm creation → no crashes
- [ ] Airplane mode → alarm works
- [ ] Low battery (5%) → alarm works
- [ ] Low storage → app functions

#### Security Testing
Run automated security audit:
```kotlin
val securityTester = SecurityTester(context)
val report = securityTester.runFullSecurityAudit()

// Must pass:
assert(report.overallSecurityScore >= 85)
assert(report.failed == 0 || report.failed <= 2) // Max 2 acceptable failures
```

#### Performance Testing
- [ ] App launches in < 2 seconds
- [ ] Alarm list scrolls at 60 FPS
- [ ] Challenge games run smoothly
- [ ] No memory leaks (use Profiler)
- [ ] Battery drain acceptable (< 5% overnight)

---

## Google Play Store

### 1. Prepare Store Listing

**Required Assets:**
- App icon (512x512 PNG)
- Feature graphic (1024x500 PNG)
- Screenshots (min 2, max 8):
  - Phone: 1080x1920 or higher
  - 7-inch tablet: 1536x2048
  - 10-inch tablet: 2048x1536
- App description (short: 80 chars, full: 4000 chars)
- Privacy policy URL

**Example Short Description:**
```
Uncompromising alarm with wake-up challenges and security hardening to prevent oversleeping
```

**Example Full Description:**
```
Chronos is a brutalist alarm clock that prevents oversleeping through:

⏰ 6 WAKE-UP CHALLENGES
• Math Problems - Solve equations to dismiss
• Burst Tapping - Rapid tap counter
• Memory Pattern - Sequence matching
• Typing - Replicate phrases
• Velocity - GPS speed tracking
• Bluetooth - Device connection

🔒 SECURITY HARDENING
• Volume override protection
• Force-close resistance
• Do Not Disturb bypass
• Battery optimization handling
• Reboot persistence

🎨 BRUTALIST DESIGN
• Clean, bold interface
• Hard shadows and borders
• Haptic feedback
• Dark/light themes

💡 SMART FEATURES
• Wake-up verification checks
• Emergency contacts
• Multiple alarms
• Custom audio sources
• Day scheduling

Perfect for heavy sleepers who need extra motivation to wake up!
```

### 2. Create Release Build

```bash
# Build AAB (Android App Bundle) for Play Store
./gradlew bundleRelease

# Output:
# app/build/outputs/bundle/release/app-release.aab
```

### 3. Upload to Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Create new application
3. Fill in store listing
4. Upload app bundle (.aab file)
5. Set content rating
6. Set pricing (free/paid)
7. Select countries
8. Submit for review

### 4. Review Checklist

Before submitting:
- [ ] App complies with Google Play policies
- [ ] Privacy policy published (required if using permissions)
- [ ] Content rating completed
- [ ] Store listing complete with screenshots
- [ ] Target audience set
- [ ] App category selected
- [ ] Contact information added
- [ ] Test on internal track first

### 5. Release Tracks

**Internal Testing** (up to 100 testers)
```bash
# Upload to internal track first
# Test for 1-2 weeks
```

**Closed Testing** (alpha/beta)
```bash
# Expand to 1000+ testers
# Gather feedback
# Fix critical bugs
```

**Open Testing** (public beta)
```bash
# Open to anyone who opts in
# Final testing before production
```

**Production**
```bash
# Full release to all users
# Staged rollout recommended (10% → 50% → 100%)
```

---

## Distribution Channels

### Google Play Store (Primary)
- ✅ Recommended for most users
- ✅ Automatic updates
- ✅ Trusted platform
- ❌ Takes 1-7 days for review
- ❌ 15% commission on paid apps

### Direct APK (Alternative)
- ✅ Immediate distribution
- ✅ No commission
- ✅ Full control
- ❌ Manual updates
- ❌ Security warnings
- ❌ Requires "Install from unknown sources"

**Hosting Options:**
- GitHub Releases
- Own website
- APKMirror (third-party)
- F-Droid (open source only)

### Enterprise Distribution
- Google Play Managed Play Store
- MDM solutions (Intune, AirWatch)
- Private app distribution

---

## Post-Deployment

### Monitoring

**Analytics Setup:**
```kotlin
// Add Firebase Analytics or similar
implementation("com.google.firebase:firebase-analytics:21.5.0")

// Track key events:
logEvent("alarm_created")
logEvent("alarm_dismissed")
logEvent("challenge_completed", mapOf("type" to challengeType))
logEvent("alarm_bypassed") // If security fails
```

**Crash Reporting:**
```kotlin
// Add Crashlytics
implementation("com.google.firebase:firebase-crashlytics:18.6.0")

// Automatic crash reporting
```

### User Feedback

Monitor:
- Google Play Console reviews
- Crash reports
- User emails
- GitHub issues

Respond to:
- Critical bugs within 24 hours
- Feature requests within 1 week
- Positive reviews with thanks

### Updates

**Versioning:**
```gradle
versionCode = 2  // Increment for each release
versionName = "1.0.1"  // Semantic versioning
```

**Release Cadence:**
- Bug fixes: As needed (hotfix)
- Minor updates: Every 2-4 weeks
- Major updates: Every 3-6 months

**Update Notes Template:**
```
🐛 Bug Fixes
- Fixed volume override on Samsung devices
- Resolved crash when creating alarm without challenges

✨ New Features
- Added Spanish translation
- New "Zen" audio theme

🔧 Improvements
- Faster alarm creation
- Reduced battery usage by 15%
```

---

## Troubleshooting

### Build Failures

**"Cannot resolve dependency"**
```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# Rebuild
./gradlew build --refresh-dependencies
```

**"Java version mismatch"**
```bash
# Set Java 17
export JAVA_HOME=/path/to/jdk17

# Verify
java -version
```

**"Keystore not found"**
```bash
# Check keystore.properties path
cat keystore.properties

# Ensure absolute path
storeFile=/absolute/path/to/chronos-release.keystore
```

### Installation Issues

**"App not installed"**
- Uninstall previous version first
- Check package name matches
- Verify signature if updating

**"Installation blocked"**
- Enable "Install unknown apps" in Settings
- Check storage space
- Try different install method (adb)

### Runtime Issues

**Alarm doesn't ring**
1. Check notification permissions granted
2. Check battery optimization disabled
3. Check Do Not Disturb override granted
4. Run security audit: `SecurityTester.runFullSecurityAudit()`

**Challenges don't work**
- Velocity: Check location permission
- Bluetooth: Check Bluetooth permission + device paired
- All: Check network connection if using URL audio

**App crashes**
```bash
# View crash logs
adb logcat | grep AndroidRuntime

# Enable debug logging
adb shell setprop log.tag.ChronosAlarm VERBOSE
```

---

## Rollback Plan

If critical bug in production:

1. **Immediate:**
```bash
# Pause rollout in Play Console
# Rollback to previous version if possible
```

2. **Fix:**
```bash
# Create hotfix branch
git checkout -b hotfix/critical-bug

# Fix bug
# Test thoroughly
# Build and release ASAP
```

3. **Communicate:**
```
# Post to Play Store listing:
"We are aware of [issue] and working on a fix.
Expected resolution within 24 hours."
```

---

## Checklist: Final Release

Before hitting "Release to Production":

- [ ] All tests pass (automated + manual)
- [ ] Security audit score >= 85
- [ ] Tested on >= 3 physical devices
- [ ] No critical bugs in internal testing
- [ ] Store listing complete and reviewed
- [ ] Privacy policy published
- [ ] Support email configured
- [ ] Keystore backed up securely
- [ ] Release notes written
- [ ] Changelog updated
- [ ] Version code incremented
- [ ] Screenshots current
- [ ] Team review complete
- [ ] Legal review (if required)
- [ ] Marketing materials ready
- [ ] Monitoring configured

---

## Success Metrics

**Week 1 Targets:**
- 100+ installs
- 4.0+ star rating
- < 1% crash rate
- < 5 critical bugs

**Month 1 Targets:**
- 1,000+ installs
- 4.2+ star rating
- < 0.5% crash rate
- 80%+ security audit pass rate

**Long-term:**
- 10,000+ installs
- 4.5+ star rating
- Featured in "Productivity" category
- < 100 unresolved issues

---

**Good luck with your deployment! 🚀**

For questions or issues, open a [GitHub Issue](https://github.com/luch4ik/chronos/issues).

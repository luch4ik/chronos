# Chronos TimePicker - CLI Test Plan & Bug Analysis

## Executive Summary

**Critical Bug Found**: TimePicker value updates may fail due to:
1. **ADB swipe gestures** don't properly trigger LazyColumn scroll completion
2. **Prop capture issue** in LaunchedEffect - using stale hour/minute values instead of current picker state
3. **Race condition** between hour and minute wheel updates

## Root Cause Analysis

### Code Review Findings (`TimePicker.kt`)

**Issue #1: Stale Prop Capture in Callbacks**
```kotlin
// Line 93 - Hour scroll complete
onTimeChanged(newHour, minute)  // BUG: `minute` is captured prop, may be stale

// Line 107 - Minute scroll complete
onTimeChanged(hour, newMinute)  // BUG: `hour` is captured prop, may be stale
```

**What happens:**
1. User scrolls hour from 7 → 8
2. Callback fires: `onTimeChanged(8, 0)` ✓ Correct
3. Parent updates state, but recomposition hasn't happened yet
4. User immediately scrolls minute 0 → 30
5. Callback fires: `onTimeChanged(7, 30)` ✗ WRONG! Uses stale `hour=7`
6. Final saved time: 07:30 instead of 08:30

**Issue #2: LazyColumn Scroll Detection with ADB**
- ADB `input swipe` may not trigger proper `isScrollInProgress` state transitions
- The LaunchedEffect only fires when `isScrollInProgress` becomes `false`
- If ADB swipe doesn't properly complete the scroll gesture, change detection never fires

**Issue #3: `firstVisibleItemIndex` vs Actual Selected Item**
- Code uses `firstVisibleItemIndex - paddingItems` to determine selected value
- With snap behavior, this should work, but rapid scrolling or programmatic scrolling may cause misalignment

---

## Comprehensive Test Plan

### Phase 1: Manual Device Testing (Baseline)

#### Test 1.1: Basic Hour Wheel Scroll
```bash
# Prerequisites: Device connected, app installed and open to alarm creation screen

# Test Steps:
1. Manually swipe hour wheel from 07 → 08
2. Observe selected value visually
3. Save alarm
4. Verify saved time in database

# Expected: Alarm saved with hour=08
# Command to verify:
adb shell "run-as com.chronos.alarm cat /data/data/com.chronos.alarm/databases/chronos_database" > db.db
sqlite3 db.db "SELECT id, time FROM alarms ORDER BY id DESC LIMIT 1;"
```

#### Test 1.2: Basic Minute Wheel Scroll
```bash
# Test Steps:
1. Leave hour at default 07
2. Manually swipe minute wheel from 00 → 30
3. Save alarm
4. Verify saved time

# Expected: Alarm saved as 07:30
```

#### Test 1.3: Sequential Hour → Minute Scroll
```bash
# Test Steps:
1. Swipe hour: 07 → 15
2. Wait 1 second (ensure state updates)
3. Swipe minute: 00 → 45
4. Save alarm

# Expected: Alarm saved as 15:45
# This tests if hour prop updates before minute scroll
```

#### Test 1.4: Rapid Sequential Scrolling
```bash
# Test Steps:
1. Quickly swipe hour: 07 → 15 (don't wait)
2. Immediately swipe minute: 00 → 45
3. Save alarm

# Expected: Should save 15:45
# Reality: May save 07:45 due to prop capture bug
```

---

### Phase 2: ADB Automation Tests

#### Test 2.1: ADB Swipe - Hour Wheel
```bash
# Get screen dimensions first
adb shell wm size
# Output example: Physical size: 1080x2400

# Calculate hour wheel position (left side, center vertically)
# Assuming hour wheel is roughly at x=270 (left quarter)
# Center is at y=1200

# Swipe UP on hour wheel (increase hour value)
adb shell input swipe 270 1400 270 1000 300

# Wait for scroll to settle
sleep 2

# Take screenshot to verify
adb shell screencap -p /sdcard/timepicker_hour_test.png
adb pull /sdcard/timepicker_hour_test.png .

# Tap Save button (need to find coordinates first)
adb shell input tap 540 2100

# Verify saved alarm time
sleep 1
adb shell "run-as com.chronos.alarm cat /data/data/com.chronos.alarm/databases/chronos_database" > test_db.db
sqlite3 test_db.db "SELECT time FROM alarms ORDER BY id DESC LIMIT 1;"
```

**Expected Result**: Time should change from 07:XX
**Gemini's Result**: Stayed at 07:00 (FAILED)

#### Test 2.2: ADB Swipe - Minute Wheel
```bash
# Minute wheel position (right side, same vertical)
# Assuming minute wheel at x=810 (right quarter)

# Swipe UP on minute wheel (increase value)
adb shell input swipe 810 1400 810 1000 300
sleep 2
adb shell screencap -p /sdcard/timepicker_minute_test.png
adb pull /sdcard/timepicker_minute_test.png .
```

#### Test 2.3: Multiple Small Swipes (Workaround Test)
```bash
# Instead of one long swipe, try multiple short swipes
# This may better trigger scroll completion events

for i in {1..5}; do
    adb shell input swipe 270 1300 270 1200 100
    sleep 0.5
done

sleep 2
# Verify hour changed
```

#### Test 2.4: UI Automator Scroll (Alternative)
```bash
# UI Automator can directly scroll Scrollable elements
# This is more reliable than input swipe

# First, install test APK with UI Automator script
# Script would use:
# device.findObject(By.scrollable(true)).scrollForward()

# For CLI testing, we can use monkey tool with specific events:
adb shell monkey --throttle 500 \
    --pct-motion 100 \
    --pct-trackball 0 \
    -p com.chronos.alarm 50
```

---

### Phase 3: Playwright/Appium Automated Tests

#### Test 3.1: Playwright Test Setup
```bash
# Install dependencies
npm install -D @playwright/test
npm install -D appium
npm install -D appium-uiautomator2-driver

# Start Appium server
appium --base-path /wd/hub
```

#### Test 3.2: Playwright Test Script
```javascript
// tests/timepicker.spec.ts
import { test, expect, _android } from '@playwright/test';

test.describe('TimePicker Tests', () => {
  let device;

  test.beforeAll(async () => {
    device = await _android.connect({
      serialNumber: process.env.ANDROID_SERIAL || 'emulator-5554'
    });
    await device.shell('am force-stop com.chronos.alarm');
    await device.shell('am start -n com.chronos.alarm/.MainActivity');
    await device.shell('sleep 2');
  });

  test('should update hour when scrolling hour wheel', async () => {
    // Navigate to add alarm
    await device.tap('selector_for_add_alarm_button');

    // Get hour wheel element
    const hourWheel = await device.locator('hour_wheel_selector');

    // Perform scroll gesture
    await hourWheel.scroll({ direction: 'up', percent: 30 });

    // Wait for scroll to settle
    await device.shell('sleep 1');

    // Take screenshot
    await device.screenshot({ path: 'hour_scrolled.png' });

    // Tap save
    await device.tap('save_button_selector');

    // Verify database
    const result = await device.shell(
      'run-as com.chronos.alarm cat databases/chronos_database'
    );
    // Parse and assert
  });

  test('should handle rapid sequential scrolling', async () => {
    // Scroll hour quickly
    await hourWheel.scroll({ direction: 'up', percent: 50 });

    // Immediately scroll minute (no wait)
    await minuteWheel.scroll({ direction: 'up', percent: 30 });

    // Save and verify both changed
  });
});
```

---

### Phase 4: Root Cause Debugging Tests

#### Test 4.1: Add Debug Logging
```kotlin
// Modify TimePicker.kt to add logs
LaunchedEffect(hourListState.firstVisibleItemIndex, hourListState.isScrollInProgress) {
    android.util.Log.d("TimePicker", "Hour scroll: index=${hourListState.firstVisibleItemIndex}, scrolling=${hourListState.isScrollInProgress}")

    if (!hourListState.isScrollInProgress) {
        val selectedIndex = hourListState.firstVisibleItemIndex - paddingItems
        val newHour = selectedIndex.coerceIn(0, 23)
        android.util.Log.d("TimePicker", "Hour changed: $newHour (prop hour=$hour, lastReported=$lastReportedHour)")

        if (newHour != lastReportedHour) {
            lastReportedHour = newHour
            onTimeChanged(newHour, minute)
            android.util.Log.d("TimePicker", "Callback fired: onTimeChanged($newHour, $minute)")
        }
    }
}
```

#### Test 4.2: Monitor Logs During ADB Swipe
```bash
# Clear logs
adb logcat -c

# Start log monitoring
adb logcat -s TimePicker:D &

# Perform ADB swipe
adb shell input swipe 270 1400 270 1000 300

# Wait and observe logs
sleep 3

# Check if LaunchedEffect fired
# Expected logs:
# TimePicker: Hour scroll: index=X, scrolling=true
# TimePicker: Hour scroll: index=Y, scrolling=false
# TimePicker: Hour changed: 8 (prop hour=7, lastReported=7)
# TimePicker: Callback fired: onTimeChanged(8, 0)

# If logs show scrolling never became false, that's the bug!
```

#### Test 4.3: Test Scroll Completion Detection
```bash
# Create a test that repeatedly checks scroll state

adb shell input swipe 270 1400 270 1000 300 &

# Poll scroll state (would need instrumentation test for this)
# Check if isScrollInProgress ever transitions to false
```

---

### Phase 5: Fix Validation Tests

#### After implementing fix, run these validation tests:

**Test 5.1: Manual Touch - All Scenarios**
- [ ] Hour scroll only
- [ ] Minute scroll only
- [ ] Sequential: hour then minute
- [ ] Sequential: minute then hour
- [ ] Rapid: both wheels quickly
- [ ] Edge cases: 00:00, 23:59, 12:30

**Test 5.2: ADB Automation - Retry Original Failures**
```bash
# Re-run Test 2.1 (ADB hour swipe)
# Should now work correctly

# Re-run Test 2.2 (ADB minute swipe)
# Should now work correctly

# Re-run Test 1.4 (rapid scrolling)
# Should save correct time with both values
```

**Test 5.3: Edge Case Testing**
```bash
# Test boundary values
adb shell input swipe 270 1600 270 800 500  # Scroll to hour 23
adb shell input swipe 810 1600 810 800 500  # Scroll to minute 59
# Verify saves as 23:59

# Test wrap-around (if supported)
# Scroll past 23 to 00
# Scroll past 59 to 00
```

---

## Recommended Fix Strategies

### Strategy 1: Use lastReported Values Instead of Props
```kotlin
// Instead of:
onTimeChanged(newHour, minute)  // minute is stale prop

// Use:
onTimeChanged(newHour, lastReportedMinute)  // current picker state
```

### Strategy 2: Track Both Wheels' State Internally
```kotlin
var internalHour by remember { mutableStateOf(hour) }
var internalMinute by remember { mutableStateOf(minute) }

// Update internal state immediately on scroll
LaunchedEffect(hourListState.firstVisibleItemIndex) {
    val newHour = (hourListState.firstVisibleItemIndex - paddingItems).coerceIn(0, 23)
    internalHour = newHour
}

// Use internal state in callbacks
onTimeChanged(internalHour, internalMinute)
```

### Strategy 3: Debounce and Batch Updates
```kotlin
// Wait for both wheels to settle before calling callback
// Use a shared debounce timer
```

### Strategy 4: Replace LazyColumn Scroll Detection
```kotlin
// Instead of relying on isScrollInProgress
// Use snapshotFlow to detect actual value changes

LaunchedEffect(Unit) {
    snapshotFlow { hourListState.firstVisibleItemIndex }
        .distinctUntilChanged()
        .debounce(150)  // Wait for scroll to settle
        .collect { index ->
            val newHour = (index - paddingItems).coerceIn(0, 23)
            // Update logic here
        }
}
```

---

## Test Environment Setup

### Prerequisites
```bash
# Install Android SDK tools
sudo apt-get install android-sdk-platform-tools

# Install ADB
sudo apt-get install adb

# Connect device
adb devices

# Install app
adb install -r chronos-debug.apk

# Grant permissions
adb shell pm grant com.chronos.alarm android.permission.POST_NOTIFICATIONS
adb shell pm grant com.chronos.alarm android.permission.SCHEDULE_EXACT_ALARM
adb shell pm grant com.chronos.alarm android.permission.VIBRATE
```

### Screen Coordinate Discovery
```bash
# Find exact wheel positions by taking screenshot and analyzing
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png .

# Use image viewer to find coordinates
# Or use:
adb shell getevent  # Then touch the screen to see coordinates
```

### Database Access Commands
```bash
# Pull database
adb shell "run-as com.chronos.alarm cat /data/data/com.chronos.alarm/databases/chronos_database" > chronos.db

# Query alarms
sqlite3 chronos.db "SELECT * FROM alarms;"

# Check specific alarm
sqlite3 chronos.db "SELECT id, time, is_active FROM alarms WHERE id='YOUR_UUID';"

# Clear all alarms (for fresh tests)
adb shell "run-as com.chronos.alarm rm /data/data/com.chronos.alarm/databases/chronos_database*"
adb shell am force-stop com.chronos.alarm
adb shell am start -n com.chronos.alarm/.MainActivity
```

---

## Test Execution Checklist

### Pre-Test Setup
- [ ] Device connected and authorized (`adb devices`)
- [ ] App installed (`adb install chronos-debug.apk`)
- [ ] Permissions granted
- [ ] Database cleared for fresh state
- [ ] Logcat ready (`adb logcat -c`)

### Manual Tests (10 minutes)
- [ ] Run Tests 1.1 - 1.4
- [ ] Document results with screenshots
- [ ] Note any unexpected behavior

### ADB Automation Tests (15 minutes)
- [ ] Run Tests 2.1 - 2.4
- [ ] Capture screenshots at each step
- [ ] Verify with database queries
- [ ] Save logcat output

### Root Cause Investigation (20 minutes)
- [ ] Add debug logging to code
- [ ] Rebuild and install
- [ ] Run Test 4.2 with monitoring
- [ ] Analyze log output
- [ ] Identify exact failure point

### Fix Implementation (variable)
- [ ] Choose fix strategy
- [ ] Implement changes
- [ ] Build and install
- [ ] Run validation tests
- [ ] Verify all scenarios pass

---

## Success Criteria

✅ **Passing Tests:**
1. Manual touch scrolling updates values correctly
2. ADB swipe gestures trigger value changes
3. Rapid sequential scrolling saves correct final time
4. Database shows accurate hour:minute values
5. No regression in other TimePicker functionality
6. Haptic feedback still works
7. Visual selected item matches saved value

❌ **Current Failures (Based on Gemini's Report):**
1. ADB swipe doesn't change values (stays 07:00)
2. Likely root cause: `isScrollInProgress` not transitioning correctly
3. Secondary issue: Prop capture in LaunchedEffect callbacks

---

## Next Steps

### Immediate Actions:
1. **Run Phase 4 tests** (debugging with logs) to confirm root cause
2. **Implement Strategy 2** (internal state tracking) as primary fix
3. **Test with ADB swipes** to verify fix works with automation
4. **Run full validation suite** (Phase 5)

### Long-term Improvements:
1. Add instrumentation tests for TimePicker
2. Create UI test suite with Maestro or Appium
3. Add visual regression tests for wheel alignment
4. Implement integration tests for alarm creation flow

---

**Version**: 1.0
**Date**: 2026-01-02
**Status**: Ready for Execution
**Estimated Time**: 1-2 hours for full test suite

#!/bin/bash
# Screenshot helper with notification and sound

echo "📸 Preparing to take screenshot in 10 seconds..."

# Send notification to device
adb shell "cmd notification post -S bigtext -t 'Screenshot in 10s' 'Tag' 'OpenCode will capture your screen in 10 seconds'"

# Play notification sound
adb shell "cmd media volume --stream 5 --set 7 && cmd media dispatch play"

# Wait 10 seconds
echo "⏳ Waiting 10 seconds..."
sleep 10

# Take screenshot
echo "📸 Taking screenshot now!"
adb exec-out screencap -p > ~/chronos/android-screenshot.png

echo "✅ Screenshot saved to ~/chronos/android-screenshot.png"

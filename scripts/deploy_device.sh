#!/usr/bin/env bash
set -e

echo "============================================================"
echo "  Qwenton Apex Android: Automated Device Deployment Script"
echo "============================================================"
echo ""

# Check ADB
if ! command -v adb &> /dev/null; then
    echo "[ERROR] 'adb' command not found. Please install Android Platform Tools."
    exit 1
fi

echo "[1/4] Checking connected Android devices..."
adb devices
echo ""

# Build APK
echo "[2/4] Building Android debug APK with Gradle..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/.."

cd "$ROOT_DIR/android-client"
chmod +x gradlew
./gradlew assembleDebug

APK_PATH="$ROOT_DIR/android-client/app/build/outputs/apk/debug/app-debug.apk"

# Install APK
echo "[3/4] Installing $APK_PATH to device..."
adb install -r "$APK_PATH"

# Push server payload
echo "[4/4] Pushing Termux server files to device storage (/sdcard/qwenton-apex-server)..."
adb shell mkdir -p /sdcard/qwenton-apex-server
adb push "$ROOT_DIR/termux-server/." /sdcard/qwenton-apex-server/

echo ""
echo "============================================================"
echo "  Deployment Complete!"
echo "============================================================"
echo "To complete setup inside Termux on your Android device:"
echo ""
echo "  1. Open Termux app"
echo "  2. Grant storage permission: termux-setup-storage"
echo "  3. Copy server files to your home directory:"
echo "       cp -r /sdcard/qwenton-apex-server ~/"
echo "  4. Run the startup script:"
echo "       cd ~/qwenton-apex-server"
echo "       chmod +x start.sh"
echo "       ./start.sh"
echo "  5. Open the Qwenton Apex app and test connection!"
echo "============================================================"

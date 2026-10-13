@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   Qwenton Apex Android: Automated Device Deployment Script
echo ============================================================
echo.

rem Step 1: Check ADB
where adb >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] ADB was not found in your PATH.
    echo Please install Android Platform Tools and ensure 'adb' is accessible.
    pause
    exit /b 1
)

echo [1/4] Checking connected Android devices...
adb devices
echo.

rem Step 2: Build Android Client APK
echo [2/4] Building Android debug APK with Gradle...
cd "%~dp0..\android-client"
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo [ERROR] Gradle build failed!
    pause
    exit /b 1
)
cd "%~dp0.."

set APK_PATH=android-client\app\build\outputs\apk\debug\app-debug.apk

rem Step 3: Install APK to device
echo [3/4] Installing %APK_PATH% to Android device...
adb install -r "%APK_PATH%"
if %errorlevel% neq 0 (
    echo [WARNING] APK installation failed. Please verify device authorization and USB debugging.
) else (
    echo [SUCCESS] Qwenton Apex Android client installed successfully!
)

rem Step 4: Push Termux Server payload
echo.
echo [4/4] Pushing Termux server files to device storage (/sdcard/qwenton-apex-server)...
adb shell mkdir -p /sdcard/qwenton-apex-server
adb push termux-server/. /sdcard/qwenton-apex-server/

echo.
echo ============================================================
echo   Deployment Complete!
echo ============================================================
echo To complete setup inside Termux on your Android device:
echo.
echo   1. Open Termux app
echo   2. Grant storage permission: termux-setup-storage
echo   3. Copy files to your home directory:
echo        cp -r /sdcard/qwenton-apex-server ~/
echo   4. Run the server startup script:
echo        cd ~/qwenton-apex-server
echo        chmod +x start.sh
echo        ./start.sh
echo   5. Open the Qwenton Apex app and test connection!
echo ============================================================
pause

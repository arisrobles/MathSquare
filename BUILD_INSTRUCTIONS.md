# How to Build and Run MathSquare App

## Running in Android Studio

### Prerequisites
- Android Studio installed
- Android SDK (API 24+)
- Java 11 or higher

### Steps to Run:
1. **Open Project**
   - Open Android Studio
   - File → Open → Select `MathSquare` folder
   - Wait for Gradle sync to complete

2. **Connect Device/Emulator**
   - **Physical Device**: Enable USB Debugging and connect via USB
   - **Emulator**: Tools → Device Manager → Create/Start Virtual Device

3. **Run the App**
   - Click the green ▶️ Run button (or press `Shift + F10`)
   - Select your device/emulator
   - App will install and launch automatically

## Generating APK Files

### Debug APK (For Testing)

**Method 1: Android Studio GUI**
1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Wait for build to complete
3. Click "locate" in notification
4. APK location: `app\build\outputs\apk\debug\app-debug.apk`

**Method 2: Command Line**
```bash
# Windows (PowerShell)
.\gradlew.bat assembleDebug

# The APK will be at:
# app\build\outputs\apk\debug\app-debug.apk
```

### Release APK (For Distribution)

**Important**: Release APK requires signing configuration. For now, you can build an unsigned release APK:

**Method 1: Android Studio GUI**
1. Build → Generate Signed Bundle / APK
2. Select APK
3. Create new keystore (or use existing)
4. Fill in keystore details
5. Select release build variant
6. Finish

**Method 2: Command Line (Unsigned - for testing only)**
```bash
# Windows (PowerShell)
.\gradlew.bat assembleRelease

# Note: This creates an unsigned APK which cannot be installed
# For production, you need to sign it first
```

### Quick Debug APK Build Command
```bash
# Navigate to project root, then run:
.\gradlew.bat assembleDebug
```

## APK File Locations

After building, APK files are located at:
- **Debug APK**: `app\build\outputs\apk\debug\app-debug.apk`
- **Release APK**: `app\build\outputs\apk\release\app-release.apk` (if signed)

## Installing APK on Device

1. Transfer the APK file to your Android device
2. On device: Settings → Security → Enable "Install from Unknown Sources"
3. Open the APK file using a file manager
4. Tap "Install"

## Troubleshooting

### Build Errors

**Debug Keystore Error (KeytoolException)**
If you get: `Failed to read key AndroidDebugKey from store`
1. **Close Android Studio completely**
2. Open PowerShell/Command Prompt as Administrator
3. Delete the old keystore:
   ```powershell
   Remove-Item "$env:USERPROFILE\.android\debug.keystore" -Force
   ```
4. Regenerate it:
   ```powershell
   keytool -genkey -v -keystore "$env:USERPROFILE\.android\debug.keystore" -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
   ```
5. Reopen Android Studio and try building again

**Other Common Issues:**
- **Gradle Sync Failed**: File → Invalidate Caches → Restart
- **SDK Missing**: Tools → SDK Manager → Install required SDKs
- **Dependencies**: File → Sync Project with Gradle Files

### Run Errors
- **Device Not Detected**: Check USB Debugging is enabled
- **Emulator Issues**: Tools → Device Manager → Cold Boot Now
- **App Crashes**: Check Logcat for error messages

## Current App Info
- **Package Name**: `com.happym.mathsquare`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 33 (Android 13)
- **Version**: alpha-v0.0.1 (versionCode: 1)


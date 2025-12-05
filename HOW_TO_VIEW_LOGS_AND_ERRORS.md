# How to Access Console Errors in Android Studio (WiFi Debugging)

## 📱 **Viewing Logcat (Console/Error Logs)**

### **Method 1: Using Logcat Window (Easiest)**

1. **Open Logcat**:
   - At the bottom of Android Studio, click the **"Logcat"** tab
   - If you don't see it, go to: `View` → `Tool Windows` → `Logcat`
   - Or press: `Alt + 6` (Windows) / `Cmd + 6` (Mac)

2. **Select Your Device**:
   - At the top of Logcat, you'll see a dropdown for devices
   - Select your WiFi-connected device (e.g., "Pixel 5 API 33")
   - If device doesn't appear, see troubleshooting below

3. **Select Your App**:
   - Next to device dropdown, there's a package filter dropdown
   - Select your app: `com.happym.mathsquare`
   - Or type: `package:com.happym.mathsquare` in the search box

4. **Filter by Log Level**:
   - Use the dropdown to filter:
     - **Verbose** (all logs)
     - **Debug** (debug info)
     - **Info** (general info)
     - **Warn** (warnings)
     - **Error** (errors only) ⚠️ **Use this to see only errors!**

5. **Search for Errors**:
   - In the search box, type: `error` or `exception` or `crash`
   - Or use regex: `.*Error.*|.*Exception.*`

---

## 🔍 **Common Logcat Filters**

### **View Only Errors**:
```
package:com.happym.mathsquare level:error
```

### **View Errors and Warnings**:
```
package:com.happym.mathsquare level:error,level:warn
```

### **View Firebase Errors**:
```
package:com.happym.mathsquare Firebase
```

### **View Crashes**:
```
package:com.happym.mathsquare FATAL
```

### **View Specific Tag**:
```
tag:STUDENT_SIGNUP
```

---

## 🔧 **Method 2: Using ADB Commands (Terminal)**

### **Check if Device is Connected**:
```bash
adb devices
```
You should see your device listed with "device" status

### **View All Logs**:
```bash
adb logcat
```

### **View Only Errors**:
```bash
adb logcat *:E
```

### **View Logs for Your App Only**:
```bash
adb logcat | grep "com.happym.mathsquare"
```

### **Clear Logs**:
```bash
adb logcat -c
```

### **Save Logs to File**:
```bash
adb logcat > logcat_output.txt
```

---

## 📲 **WiFi Debugging Setup**

### **If Device Doesn't Appear in Logcat**:

1. **Enable WiFi Debugging** (Android 11+):
   - On your device: `Settings` → `Developer Options` → `Wireless debugging`
   - Enable it
   - Tap "Pair device with pairing code"
   - Note the IP address and port (e.g., `192.168.1.100:12345`)

2. **Connect via ADB**:
   ```bash
   adb pair 192.168.1.100:12345
   ```
   Enter the pairing code when prompted

3. **Connect to Device**:
   ```bash
   adb connect 192.168.1.100:XXXXX
   ```
   (Use the port shown in "Wireless debugging" settings)

4. **Verify Connection**:
   ```bash
   adb devices
   ```
   Should show: `192.168.1.100:XXXXX    device`

---

## 🐛 **Finding Specific Errors**

### **1. App Crashes**:
- Look for lines with `FATAL EXCEPTION`
- Check the stack trace below it
- Common tags: `AndroidRuntime`, `crash`

### **2. Firebase Errors**:
- Search for: `FirebaseException`, `FirestoreException`
- Look for: `PERMISSION_DENIED`, `NOT_FOUND`, etc.

### **3. Network Errors**:
- Search for: `IOException`, `SocketException`, `NetworkException`

### **4. Null Pointer Exceptions**:
- Search for: `NullPointerException`
- Check which line number caused it

---

## 📋 **Logcat Tips**

### **Useful Keyboard Shortcuts**:
- **Clear Logcat**: Click the trash icon or `Ctrl + L`
- **Scroll to Bottom**: Automatically scrolls to latest logs
- **Pause Logcat**: Click pause button to freeze logs

### **Save Important Logs**:
1. Select the log lines you want
2. Right-click → `Copy` or `Save to File`
3. Or use: `File` → `Save Logcat to File`

### **Color Coding**:
- **Red**: Errors
- **Orange**: Warnings
- **Blue**: Debug
- **Green**: Info
- **Gray**: Verbose

---

## 🔍 **Example: Finding Login Error**

1. Open Logcat
2. Filter: `package:com.happym.mathsquare level:error`
3. Try to log in
4. Look for errors like:
   ```
   E/STUDENT_SIGNUP: Failed to save student data: ...
   E/Firestore: Error accessing account: ...
   ```

---

## ⚠️ **Troubleshooting**

### **Problem**: Device not showing in Logcat
- **Solution**: 
  - Check `adb devices` in terminal
  - Restart ADB: `adb kill-server` then `adb start-server`
  - Reconnect WiFi debugging

### **Problem**: No logs appearing
- **Solution**:
  - Make sure app is running
  - Check if device is selected in Logcat dropdown
  - Try clearing filter: remove all text from search box

### **Problem**: Too many logs
- **Solution**:
  - Use package filter: `package:com.happym.mathsquare`
  - Filter by log level: `level:error`
  - Use specific tag: `tag:YourTagName`

### **Problem**: WiFi connection lost
- **Solution**:
  - Reconnect: `adb connect IP:PORT`
  - Or reconnect via USB temporarily

---

## 🎯 **Quick Reference**

| Action | Command/Location |
|--------|------------------|
| Open Logcat | `Alt + 6` or `View` → `Tool Windows` → `Logcat` |
| Filter by App | `package:com.happym.mathsquare` |
| View Errors Only | `level:error` |
| Clear Logs | Click trash icon or `Ctrl + L` |
| Check Device | `adb devices` |
| Connect WiFi | `adb connect IP:PORT` |

---

## 📝 **Adding Custom Logs in Your Code**

To see custom logs, use:
```java
Log.d("TAG_NAME", "Your message here");
Log.e("TAG_NAME", "Error message here");
Log.w("TAG_NAME", "Warning message here");
```

Then filter in Logcat: `tag:TAG_NAME`

---

**Status**: ✅ Complete guide for viewing logs and errors!


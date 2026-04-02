# Project README

## Overview

This Android application was developed to compare different eye tracking methods for guitarists.
It is built using **Kotlin** and integrates the **Eyedid SDK** for eye-tracking functionality. 
As the Eyedid licence key expires after one month, this guide explains how to obtain a new key and update the project.

---

## Getting a Licence Key

1. Go to the [Eyedid website](https://eyedid.ai) to create an account.
2. Once registered, you will be able to sign up for a free trial.
3. Each key is valid for one month from the date it is created.

---

## Updating the Licence Key

1. Open the project in Android Studio.
2. Go to this file:

   ```
   app/src/main/java/com.example.test/MainActivity.kt
   ```

3. The old licence key will be stored in this variable:

   ```kotlin
   val licenceKey = "my_licence_key"
   ```

4. Replace the the existing key with the new one:

   ```kotlin
   val licenceKey = "YOUR_NEW_KEY_HERE"
   ```

5. Save the file and rebuild the project.

6. Generate the APK.

---

## Running on an Android Device

To run the application on your Android phone:

### 1. Enable Developer Mode
1. On your Android phone, go to **Settings > About Phone**
2. Tap **Build Number** seven times until you see "You are now a developer"
3. Go back to **Settings > Developer Options**
4. Enable **USB Debugging**

### 2. Connect Your Phone
1. Connect your phone to your computer via a USB-C cable
2. On your phone, when prompted, select **"Allow USB Debugging"** and tap OK
3. If asked for the connection type, select **"File Transfer"** or **"MTP"**

### 3. Run the Application
1. Open the project in Android Studio
2. Wait for Gradle to finish syncing
3. In the toolbar at the top, you should see your device appear in the 
   device dropdown 
4. Press the **Run** button (green play icon)
5. The app will build and install automatically on your phone

### Troubleshooting
- If your device does not appear, try unplugging and replugging the cable
- Make sure USB Debugging is enabled
- Some phones require you to unlock the screen before the device is detected

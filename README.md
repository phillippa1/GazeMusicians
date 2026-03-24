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

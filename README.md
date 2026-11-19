# ANNU — Android Floating Companion

A native Android starter app for a user-controlled floating anime companion.

## Current prototype
- Requests Android **Display over other apps** permission.
- Starts a foreground service after the user taps Start.
- Displays a movable bubble over other apps.
- Voice input through Android speech recognition, with spoken replies using Android TextToSpeech.
- Starter allowlist: open YouTube, open Chrome, tell the time, greet, and stop the floating service.

## Build
GitHub Actions builds a debug APK. Open **Actions → Build ANNU APK**, run the workflow, then download the **ANNU-debug-apk** artifact from the completed run.

## Important limitations
This is an early prototype, not a general-purpose phone-control AI. It does not execute arbitrary spoken commands, read private screen content, send messages, or make calls. Sensitive actions should require explicit confirmation. Android may stop background work due to battery management, and overlays cannot cover every protected/system screen.

The 3D VRM model, richer expressions, conversational AI, persistent preferences, and additional user-approved app actions are not integrated yet. Add a licensed model at `app/src/main/assets/annu.vrm` in a later 3D integration step.

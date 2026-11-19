# ANNU — Android Floating Companion

A native Android app prototype for a user-controlled floating anime companion overlay.

## Current prototype
- Requests Android's **Display over other apps** permission.
- Starts a foreground service from an explicit user action.
- Displays a movable floating companion window over other apps.
- Includes a WebView scene that will load the user's VRM model when supplied.

## Build
The GitHub Actions workflow builds a debug APK. Install it on your Android device, open ANNU, grant overlay permission, then tap **Start ANNU**.

## Notes
Android may stop background work because of battery management, and overlays cannot cover every protected/system screen. A persistent notification is shown while the companion is running. The VRM asset is not included yet; add your own licensed model at `app/src/main/assets/annu.vrm`.

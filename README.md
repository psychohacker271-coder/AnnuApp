# ANNU — Android Floating 3D Companion

ANNU is an early Android-native prototype for a user-controlled floating VRM companion.

## Implemented in this repository
- Android app shell with a local WebView-based VRM renderer.
- Bundled VRM model copied into private app storage on first launch.
- Import/replace a VRM file and import audio files into private app storage.
- User-started floating overlay foreground service.
- Drag the overlay; its last position is saved and restored.
- App controls to make the overlay smaller/larger and reset its size/position.
- One-shot Android speech recognition and Android TextToSpeech replies, with selectable Hindi/English recognition and voice output.
- Basic VRM idle, blink, wave, dance, sleep and wake gesture states; TTS playback drives a simple mouth expression when the model supports it.
- Basic allowlisted commands: open YouTube, Chrome, Camera, Maps, Android Settings, Wi-Fi and Bluetooth settings; tell the time, greet, and stop the overlay.
- Optional private note stored locally in Android SharedPreferences; user can edit/clear it in the app and ask ANNU to recall it by voice. The note is not sent to a server.
- Incoming-call notification alert only (phone-state broadcast).

## Build an APK
GitHub Actions runs on pushes to `main`. Open **Actions → Build ANNU APK**, wait for the latest run to finish, then download the **ANNU-3D-debug-apk** artifact. The artifact is a ZIP containing `app-debug.apk`.

## Current limitations — please read
- The current VRM is the model file already committed in the repository; it may not visually match a separate anime reference image.
- The build workflow now bundles the Three.js, GLTFLoader and three-vrm module files into the APK so the 3D runtime itself can load without fetching those libraries. The build machine still needs internet access to download these files when producing a new APK.
- Character gestures are basic procedural bone rotations, not motion-captured animation clips. Walking, sitting, rich facial expressions, and phoneme-accurate lip-sync are not implemented. Lip movement is a simple mouth-open expression while Android TTS speaks and depends on the VRM model supporting the expression.
- Voice input is currently tap-to-talk using Android's speech recognizer. There is no always-on wake-word service. Screen-off wake-word operation is not guaranteed on Android 15 and device battery policies.
- The app does not yet contain a generative conversational AI brain. It uses a small offline command parser and saved private note; it is not an unrestricted phone-control agent.
- The incoming-call feature only detects ringing and shows a notification. It cannot hear the caller, answer/decline calls, or conduct a conversation. Android's call-audio and default-dialer restrictions apply.
- Imported audio is stored but is not yet wired into the character's speech engine.
- No private messages, contacts, call audio, or screen contents are read. Sensitive actions should require explicit user confirmation.

## Permissions and privacy
Overlay and notification permissions are requested for the floating companion. Phone-state permission is used only for the incoming-call alert. Do not grant permissions unless you want the corresponding feature. No call recording is performed.

This is a development prototype, not a finished commercial assistant.

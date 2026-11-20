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
- Optional user-started “Hey ANNU” wake-phrase foreground microphone service. It uses Android SpeechRecognizer (not an offline wake-word model), displays an ongoing notification, and posts a tap-to-talk alert when the phrase is recognized. The user can stop it from the notification or app.
- Imported audio files can be played back locally as samples; imported audio does not replace Android TextToSpeech.
- Basic procedural VRM idle, blink, wave, dance, walk, sit pose, nod, thinking, happy, sleep and wake gesture states; TTS playback drives a simple mouth expression when the model supports it.
- Basic allowlisted commands: open YouTube, Chrome, Camera, Maps, Android Settings, Wi-Fi and Bluetooth settings; tell the time, greet, and stop the overlay.
- Optional private note stored locally in Android SharedPreferences; user can edit/clear it in the app and ask ANNU to recall it by voice. The note is not sent to a server.
- Incoming-call notification alert only (phone-state broadcast).

## Build an APK
GitHub Actions runs on pushes to `main`. Open **Actions → Build ANNU APK**, wait for the latest run to finish, then download the **ANNU-3D-debug-apk** artifact. The artifact is a ZIP containing `app-debug.apk`.

## Current limitations — please read
- The current VRM is the model file already committed in the repository; it may not visually match a separate anime reference image.
- The build workflow now bundles the Three.js, GLTFLoader and three-vrm module files into the APK so the 3D runtime itself can load without fetching those libraries. The build machine still needs internet access to download these files when producing a new APK.
- Character gestures are basic procedural bone rotations, not motion-captured animation clips. Walking, sitting, rich facial expressions, and phoneme-accurate lip-sync are not implemented. Lip movement is a simple mouth-open expression while Android TTS speaks and depends on the VRM model supporting the expression.
- Wake listening is opt-in and requires microphone permission. It uses the phone’s Android speech-recognition provider, which may use network services; it is not a fully offline wake-word engine. The service must be started while the app is visible, shows an ongoing microphone notification, and may be stopped by Android/OEM battery management. Screen-off recognition is not guaranteed on Android 15. Stop it when not needed to save battery and protect privacy.
- Optional conversational AI connection is available through a user-configured Ollama server on a PC on the same Wi-Fi. It uses the Ollama `/api/chat` endpoint and does not require a paid API key. The PC must remain on and Ollama must be configured to accept LAN connections. Without that server configured, ANNU continues in basic offline command mode. This is not a built-in offline LLM and is not an unrestricted phone-control agent.
- The incoming-call feature only detects ringing and shows a notification. It cannot hear the caller, answer/decline calls, or conduct a conversation. Android's call-audio and default-dialer restrictions apply.
- Imported audio can be played as a sample, but it is not yet a custom TTS voice or voice-cloning model.
- No private messages, contacts, call audio, or screen contents are read. Sensitive actions should require explicit user confirmation.

## Permissions and privacy
Overlay and notification permissions are requested for the floating companion. Phone-state permission is used only for the incoming-call alert. Do not grant permissions unless you want the corresponding feature. No call recording is performed.

This is a development prototype, not a finished commercial assistant.

# Forge Stream

A Kotlin/Jetpack Compose Android downloader with bundled, app-managed download engines and DataStore-backed settings.

## Bundled components

yt-dlp and ffmpeg must be packaged inside the APK under ABI-specific assets. On first startup the app silently prepares them in executable app-private storage and validates them before use. The user does not install packages, use Termux, configure a shell path, or manage binaries manually.

The release input layout is:

```text
app/src/main/assets/forge-stream/arm64-v8a/yt-dlp
app/src/main/assets/forge-stream/arm64-v8a/ffmpeg
app/src/main/assets/forge-stream/x86_64/yt-dlp
app/src/main/assets/forge-stream/x86_64/ffmpeg
```

They are extracted to `filesDir/forge-stream/bin/` and all process execution uses
those resolved paths.

The app never assumes a Termux installation, shell `PATH`, shared storage path, or external package manager. Missing or invalid bundled components are surfaced as an actionable setup error instead of crashing.

## Settings

All settings are persisted with Android DataStore, including download folder, yt-dlp arguments, cookies, proxy, post-processing, appearance, debug logging, export, and reset actions. Tool versions and resolved locations are shown in Settings.

## Build

Install JDK 17, Android SDK platform 35, and Android Build Tools 35.0.1, then run:

```sh
./gradlew assembleDebug
```

The Android module pins `compileSdk`/`targetSdk` to 35 and `buildToolsVersion` to 35.0.1. Do not commit `local.properties`; point `ANDROID_SDK_ROOT` or Android Studio at the local SDK installation instead.

The checked-in launcher delegates to a locally installed Gradle 8.9+ if the binary wrapper JAR has not yet been generated. To generate a standard wrapper on a machine with Gradle installed, run `gradle wrapper --gradle-version 8.9`.

The app launches even when dependencies are missing, reports their state without crashing, and prevents a download from starting until yt-dlp is validated.

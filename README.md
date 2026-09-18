# Vid-Downloader

A Kotlin/Jetpack Compose Android yt-dlp frontend with app-private dependency management, DataStore-backed settings, and a process-backed download engine.

## Dependency setup

The app stores managed binaries below its private `files/binaries` directory and validates each executable with a version check before use. Open **Settings > Dependencies** to inspect status and install/remove managed components. ffmpeg can also be configured with an Android-compatible executable path in Settings; the app passes that path to yt-dlp with `--ffmpeg-location`.

The app never assumes a Termux installation, shell `PATH`, shared storage path, or external package manager. Missing or invalid dependencies are surfaced in the first-launch setup check and as actionable download errors.

## Settings

All settings are persisted with Android DataStore, including download folder, yt-dlp arguments, cookies, proxy, user agent, ffmpeg path, post-processing, appearance, debug logging, export, and reset actions.

## Build

Install JDK 17, Android SDK platform 35, and Android Build Tools 35.0.1, then run:

```sh
./gradlew assembleDebug
```

The Android module pins `compileSdk`/`targetSdk` to 35 and `buildToolsVersion` to 35.0.1. Do not commit `local.properties`; point `ANDROID_SDK_ROOT` or Android Studio at the local SDK installation instead.

The checked-in launcher delegates to a locally installed Gradle 8.9+ if the binary wrapper JAR has not yet been generated. To generate a standard wrapper on a machine with Gradle installed, run `gradle wrapper --gradle-version 8.9`.

The app launches even when dependencies are missing, reports their state without crashing, and prevents a download from starting until yt-dlp is validated.

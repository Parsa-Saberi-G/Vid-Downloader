This directory is populated at release time with verified Android-native executables:

forge-stream/<abi>/yt-dlp
forge-stream/<abi>/ffmpeg

Supported ABIs are arm64-v8a and x86_64. These files are intentionally not downloaded
at runtime and must be supplied by the release pipeline before publishing the APK.

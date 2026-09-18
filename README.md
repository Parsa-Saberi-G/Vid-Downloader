# Vid-Downloader

A Kotlin/Jetpack Compose Android frontend foundation for yt-dlp. The external yt-dlp executable is intentionally not bundled or downloaded automatically. Provision only a trusted, authorized executable before implementing the production backend.

## Build

Install JDK 17 and Android SDK platform 35, then run:

```sh
./gradlew assembleDebug
```

The checked-in launcher delegates to a locally installed Gradle 8.9+ if the binary wrapper JAR has not yet been generated. To generate a standard wrapper on a machine with Gradle installed, run `gradle wrapper --gradle-version 8.9`.

The initial app launches, accepts a URL, exposes the engine/repository seams, displays state with StateFlow, and reports the expected unavailable-backend error until yt-dlp provisioning is implemented.

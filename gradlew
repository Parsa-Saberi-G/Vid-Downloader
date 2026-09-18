# Placeholder wrapper launcher. Install Gradle 8.9+ or generate the wrapper with:
# gradle wrapper --gradle-version 8.9
#!/usr/bin/env sh
set -e
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle is not installed. Install Gradle 8.9+ or generate the Gradle wrapper." >&2
exit 1

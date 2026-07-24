#!/usr/bin/env sh
set -eu

ROOT_DIR=$(cd "$(dirname "$0")/.." >/dev/null 2>&1 && pwd -P)
SDK_DIR=${ANDROID_HOME:-/opt/homebrew/share/android-commandlinetools}
ADB="$SDK_DIR/platform-tools/adb"
PACKAGE="com.museerodin.companion"
REMOTE_DIR="/sdcard/Android/data/$PACKAGE/files/screenshots"
LOCAL_DIR="$ROOT_DIR/artifacts/screenshots"

if [ ! -x "$ADB" ]; then
  echo "adb not found at $ADB" >&2
  exit 1
fi

mkdir -p "$LOCAL_DIR"
rm -f "$LOCAL_DIR"/*.png

cd "$ROOT_DIR"
ANDROID_HOME="$SDK_DIR" ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.museerodin.companion.screenshots.ScreenshotCaptureTest

"$ADB" pull "$REMOTE_DIR/." "$LOCAL_DIR" >/dev/null
echo "Screenshots copied to $LOCAL_DIR"


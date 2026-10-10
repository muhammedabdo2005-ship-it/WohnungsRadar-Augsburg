#!/usr/bin/env bash
set -euo pipefail
# Preserve runtime diagnostics before the emulator runner shuts the device down.
trap 'adb logcat -d > android-logcat.txt; tail -n 250 android-logcat.txt' EXIT
adb logcat -c
./gradlew :app:connectedDebugAndroidTest --no-daemon

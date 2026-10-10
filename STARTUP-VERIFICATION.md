# Startup failure: network-constrained JobScheduler permission

Both Android manifests omitted `android.permission.ACCESS_NETWORK_STATE`, while
`FeedJobService.schedule()` calls `setRequiredNetworkType(NETWORK_TYPE_ANY)`.
With targetSdk 35, Android 14+ enforces this permission when scheduling and throws
`SecurityException` if it is absent. A successful APK compilation does not exercise
this framework runtime check. The permission is normal/install-time; no new
runtime permission dialog is needed.

Reference: https://developer.android.com/about/versions/14/behavior-changes-14#jobscheduler

The original `MainActivity.onCreate()` scheduled this job without a catch. Commit
`3f4d706` catches the exception in the root project, so on that exact version the
permission error should be logged rather than crash the launcher. It still breaks
RSS scheduling, and feed-settings saving and boot scheduling remain unguarded.
The nested `WohnungsRadar_Augsburg_Multiquellen` project retained the original
uncaught startup path. This fix adds the permission to both builds and brings the
nested activity's existing startup safeguards in sync with the root project.

No crash log or installed APK revision was supplied. This identifies a concrete
runtime defect, but does not prove that a crash from an APK already containing
`3f4d706` has the same cause. Do not attribute an additional crash to UI ordering
without its stack trace.

## Automated verification

The root GitHub Actions workflow builds the debug and instrumentation APKs on pull
requests, runs the existing Java helper tests, and runs `StartupTest` on an API 35
emulator. The tests check actual job registration (so swallowing an exception
cannot pass), launcher startup/recreation with notification permission ungranted,
and shared-link persistence without duplicate insertion after recreation.
The separately buildable project contains the same tests and workflow.

Commands from the chosen project root:

```sh
bash setup-wrapper.sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest --no-daemon
# With an Android emulator/device attached (fresh test installation):
./gradlew :app:connectedDebugAndroidTest --no-daemon
```

## Physical-device checks still required

1. Install the PR APK, first as an update over the old build with the same signing
   key (retain offers/settings), then test a fresh installation on a test device.
   Uninstalling deletes local offers and settings; do not uninstall the user's
   only copy just to test.
2. On Android 14/15, launch offline and online, deny then allow notification
   permission, return from settings, rotate, and close/reopen. Also smoke-test
   Android 8/API 26, the minimum supported version.
3. Save a valid HTTPS RSS feed and check manual fetch, background fetch and a new
   item notification. In `adb shell dumpsys jobscheduler`, confirm job 6481 for
   `de.wohnungsradar.augsburg`. Reboot after opening the app once and confirm the
   persisted job remains. Scheduling is not an exact 15-minute timer.
4. Enable notification-listener access and trigger an actual supported portal
   alert. Check import, duplicate suppression and opening the original listing.
5. Share a listing into the app, mark it contacted and verify it survives reopening.
6. If it still crashes, capture `adb logcat -b crash -d` immediately afterwards,
   together with the Android version and APK workflow/commit. Inspect the first
   `Caused by` line before making another speculative startup change.

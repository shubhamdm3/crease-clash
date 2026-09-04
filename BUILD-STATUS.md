# v0.2 validation

Local checks passed: 3,270 behavioral assertions, including 200 early assisted shots,
correct left/right velocity, retry after an early timed tap, pause/restart queue handling,
real controller coordinates, scoring, and a 360-shot outcome matrix across all difficulties.

The Android CI gate builds and lints the APK, verifies its signature, installs it on Android 15,
and requires actual LEFT and RIGHT touch inputs to produce logged contact and resolve both
balls. It also captures screenshots and checks background/resume stability.

The v0.2 Android run is pending at this source commit. Its run result and artifacts in GitHub
Actions are authoritative. The previous v0.1 smoke test checked launch and lifecycle but did
not assert successful contact; the new test specifically closes that gap.

Physical touch latency, haptic feel, and performance on the user's OnePlus still need a device pass.

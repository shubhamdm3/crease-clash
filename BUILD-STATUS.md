# v0.2 validation

Local checks passed: 3,270 behavioral assertions, including 200 early assisted shots,
correct left/right velocity, retry after an early timed tap, pause/restart queue handling,
real controller coordinates, scoring, and a 360-shot outcome matrix across all difficulties.

The Android CI gate builds and lints the APK, verifies its signature, installs it on Android 15,
and requires actual LEFT and RIGHT touch inputs to produce logged contact and resolve both
balls. It also captures screenshots and checks background/resume stability.

**Android v0.2 passed:** [verified CI run](https://github.com/shubhamdm3/crease-clash/actions/runs/33851929061),
source commit `d4adaad3c824d33eb978eb2e0fe1d9b44ecf01ea`.

The emulator installed versionCode 2 / versionName 0.2.0 at 1920x1080. An early LEFT touch
queued and hit a ground shot for 3 runs. An early RIGHT touch queued and hit a lofted shot
for 6 runs. Both deliveries completed; pause and background/resume survived. Actual Android
screenshots were inspected for direction labels, control layout, ball visibility, and contact.

APK SHA-256: `f1c8872fd5a6a950cca9b2d81928a41a6a0b475e896bf2cfa279ebf3f2791ebd`.
The complete build, lint, signature, behavioral checks, and emulator job succeeded. The previous v0.1 smoke test checked launch and lifecycle but did
not assert successful contact; the new test specifically closes that gap.

Physical touch latency, haptic feel, and performance on the user's OnePlus still need a device pass.

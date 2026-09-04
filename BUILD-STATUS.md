# Verified build — Crease Clash v0.1.0

[Successful Android build and runtime evidence](https://github.com/shubhamdm3/crease-clash/actions/runs/33849387442)

APK source commit: `adaab2e50a8f05fb7cb281e5133f1b3237090946`. Subsequent documentation-only changes do not alter the APK source.

## Verified

- Java 17 core, controller, renderer, desktop, LibGDX, and Android compilation.
- Android debug APK assembly and Android lint without errors.
- APK Signature Scheme v2 verification.
- 1,863 behavioral assertions, including 100 no-input innings, a 40-delivery practice session,
  and 360 shot scenarios across the three difficulties.
- One-swing enforcement, wickets, boundaries, automatic running, target completion,
  innings limits, pause/resume, frame-rate independence, settings, and best-score persistence.
- APK installation and launch on an Android 15 x86_64 emulator.
- Actual OpenGL screen rendering at 1920x1080; menu, game, and paused state visually inspected.
- Practice input, a completed delivery, and background/return without a crash.
- The app returns to a paused state after backgrounding. Android focus loss also requests a pause.
- ZIP integrity and expected manifest, DEX, font/audio assets, and ARM64 native library.

## APK

- Application ID: `com.shubham.creaseclash`
- Version: `0.1.0` (version code 1)
- Minimum Android: API 26 (Android 8.0)
- Target SDK: 35; compile SDK: 36
- Architectures: arm64-v8a, armeabi-v7a, x86_64
- Size: 4,657,534 bytes
- SHA-256: `c5f45b9cfc0bee45427b4e9413237818632124978f363d68f89ab6f6d177a39e`
- No internet permission; vibration and a generated internal receiver permission are present.

## Still requires a real phone

- Batting responsiveness and whether early/late timing feels fair.
- Physical hit, wicket, and boundary vibration strength.
- Audio volume, sustained performance, battery use, and both landscape orientations.
- OnePlus-specific behavior and Android 16 device validation.

The emulator test covers startup, basic input and lifecycle; it is not a full playthrough or a performance benchmark.
This debug build is intended for first-play testing. Its signing configuration is not a production release configuration.

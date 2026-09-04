# Crease Clash — first playable v0.1.0

An original offline, single-player stick-style cricket game, created for Shubham Mahendrakar.
Java gameplay and vector artwork are shared between a desktop test harness and a LibGDX Android launcher.

**Android build verified:** debug APK assembly, Android lint, APK signature verification,
and an Android 15 emulator smoke test passed. The emulator installed and launched the app,
exercised a delivery, and returned to a paused game after backgrounding.
Physical vibration feel, touch latency, and performance on the user's OnePlus remain to be tested.

[Verified build and APK artifact](https://github.com/shubhamdm3/crease-clash/actions/runs/33849387442) — download `crease-clash-debug-apk` and extract `android-debug.apk`.
This is the first playable debug build, not a Play Store release. See `BUILD-STATUS.md` for verification details.

## Play

- **Chase:** 12 deliveries, 3 wickets. Club target 24, Pro 32, Elite 42.
- **Practice:** unlimited deliveries and wickets; practice does not affect your best chase score.
- Choose **GROUND** or **LOFT**, then tap **BOWL**.
- Watch the ball bounce and tap **LEFT SHOT** or **RIGHT SHOT** as it reaches the batter.
- One swing per ball. Early/late shots lose power; batting across the line reduces control.
- Fours, sixes, catches, and pickups follow the ball simulation. Running is automatic, up to three runs.
- Sound and haptics can be switched off independently. Best score, difficulty, and settings persist.
- Backgrounding pauses the game. Resume explicitly so a delivery cannot play unseen.

Desktop shortcuts: **A / Left** = left shot, **D / Right** = right shot,
**Space** = start / bowl, **L** = ground / loft, **P / Escape** = pause / resume.

## Run the included desktop build

Install Java 17 or newer. Download and extract `crease-clash-preview-and-desktop` from the verified build above, then run from the extracted directory:

```sh
java -jar crease-clash-desktop.jar
```

The JAR is a desktop application. It does not install on Android.
It opens a real playable Swing window and uses the same gameplay and scene code as the Android implementation.

To compile from source without downloading any dependencies:

```sh
java tools/Build.java test
java tools/Build.java run
java tools/Build.java smoke
```

`test` compiles core, test, and desktop sources, builds `build/crease-clash-desktop.jar`,
and runs the behavioral checks. `smoke` writes real shared-renderer screenshots to `build/screenshots`.
This desktop path needs a JDK. Normal gameplay needs a display; smoke rendering works headlessly.

## Build the Android APK

### Android Studio

1. Open this directory as a Gradle project.
2. Use JDK 17 and install Android SDK Platform 36 / Build Tools 35.0.0.
3. Allow Gradle to download the pinned dependencies.
4. Build the `android` module or run:

```sh
./gradlew :core:check :android:assembleDebug :android:lintDebug
```

Windows: use `gradlew.bat` in place of `./gradlew`.

Expected output: `android/build/outputs/apk/debug/android-debug.apk`.
This is a debug APK for device testing, not a signed Play Store release.
The project targets Android 8.0+ (API 26), supports arm64-v8a, armeabi-v7a, and x86_64,
and declares vibration permission. It does not request internet permission.

### GitHub Actions

Push the project to the root of a repository on its `main` branch.
The included `.github/workflows/build.yml` runs the rules checks, compiles Android,
runs Android lint, renders desktop screenshots, and uploads an artifact named
`crease-clash-debug-apk`. Download and unzip that artifact to obtain the APK.
The workflow also verifies the APK signature and runs an Android 15 emulator smoke test. It can be started manually from the repository's Actions tab.

Repository: https://github.com/shubhamdm3/crease-clash. The initial Android workflow runs on `main`.

## Project layout

| Module / file | Responsibility |
| --- | --- |
| `core/.../CricketGame.java` | Deterministic delivery, batting, ball physics, fielding, running, and match state |
| `core/.../GameSession.java` | Touch input, pause, settings, and score persistence |
| `core/.../GameRenderer.java` | Original vector stadium, players, animations, and interface |
| `core/.../Draw.java` | Drawing interface shared by the platform adapters |
| `gdx/.../CreaseApp.java` | LibGDX lifecycle, touch conversion, audio, preferences |
| `gdx/.../GdxDraw.java` | OpenGL shapes and bitmap text |
| `android/.../AndroidLauncher.java` | Landscape Android activity and vibration patterns |
| `desktop/` | AWT renderer, playable window, headless scene verification |
| `core/src/test/` | Rules and controller behavioral checks |
| `tools/make_assets.py` | Reproducible original sounds and font atlas generation |
| `assets/` | Bundled font and original PCM sound effects |

## Scope and limitations

This is an arcade batting prototype, not a full cricket simulator.
It has one stadium, a fixed field, and pace variations. There is no manual bowling,
multiplayer, tournament, player roster, spin, swing, LBW, wides, no-balls, or run-outs.
There is no mid-match persistence after Android process termination; restarting the app returns to the club.

The simulation uses a fixed 120 Hz step. Rendering follows the device frame rate.
The 1440x810 interface is fitted without cropping, with margins on wider screens.
The desktop screenshots verify shared scene layout, not Android OpenGL rendering or physical touch latency.
Android compilation, lint, installation, screen rendering, and basic lifecycle were verified in CI. Physical audio/haptic feel, touch latency, and performance on a real phone still need testing.

## Next device pass

Install the debug APK and test on the OnePlus:

1. Finish one chase and one practice session.
2. Try early, perfect, late, missed, ground, and lofted shots.
3. Check that ball visibility and tap timing feel fair.
4. Confirm hit, wicket, and boundary vibrations feel distinct; switch vibration off.
5. Background during a delivery, return, and resume from the pause screen.
6. Check both landscape orientations for comfortable controls and safe screen margins.

## Development references

- [LibGDX starter classes and Android configuration](https://libgdx.com/wiki/app/starter-classes-and-configuration)
- [LibGDX application lifecycle](https://libgdx.com/wiki/app/the-life-cycle)
- [Android Gradle Plugin 8.10 compatibility](https://developer.android.com/build/releases/agp-8-10-0-release-notes)

The version combination follows the existing Last Lantern project: Gradle 8.11.1,
Android Gradle Plugin 8.10.1, LibGDX 1.14.2, JDK 17. These dependencies were resolved and the Android project built successfully in GitHub Actions.

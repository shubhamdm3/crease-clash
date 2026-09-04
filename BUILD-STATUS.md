# Build and verification status — 2026-09-04

## Completed locally

- Compiled the pure Java gameplay, session controller, shared renderer, tests, and desktop launcher with Java 17.
- Built and launched the desktop application in headless smoke mode.
- Ran 100 no-input innings, 40 consecutive practice deliveries, and a 360-shot matrix across three difficulties.
- Checked one-swing enforcement, pause/resume, target completion, innings limits, boundary scoring,
  catching versus ground collection, automatic 1/2/3-run scoring, frame-rate independence, settings, and best-score persistence.
- Parsed all Java source files, including Android and LibGDX adapters, for syntax errors.
- Rendered and visually reviewed menu, delivery, shot result, help, pause, and match-end screens.
- Rendered a 20:9 layout to confirm the shared scene fits without cutting off controls.
- Generated eight original WAV sound effects and a bitmap atlas from the bundled DejaVu font.

## Not yet verified

- Android and LibGDX type compilation, dependency resolution, lint, or APK assembly.
- Actual OpenGL text metrics and graphical performance on Android.
- APK installation and play on the user's phone.
- Physical vibration, device audio, real touch latency, and real Android background/resume behavior.
- The automated GitHub workflow; the first CI build is pending.

## Build blocker

There is no local Android SDK. Attempts to reach the Android SDK download host and Maven Central
timed out under the environment's network restrictions. The user has now created
`shubhamdm3/crease-clash`; the project is being uploaded to run its Android build in GitHub Actions.

The desktop JAR remains the locally verified build until Android CI completes.

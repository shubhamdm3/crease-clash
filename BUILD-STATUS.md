# v0.3 validation

Verified Android build: [CI run 33868341471](https://github.com/shubhamdm3/crease-clash/actions/runs/33868341471).
Source commit: `770b09aaa7dc81c1323212858bd46e76f5b1808d`.

- 22,015 behavioral checks passed, including 200 assisted ground shots limited to at most two runs, 200 early lofts with zero sixes, rapid-tap regression checks, and timing corrections.
- A 4,410-loft sweep scored six only from manually PERFECT timing. See [research and balance notes](BATTING-BALANCE.md) for the distribution and methodology.
- APK assembly, Android lint and v2 signature verification passed.
- Android 15 installed versionCode 3 / versionName 0.3.0 and rendered at 1920x1080.
- Real early LEFT and RIGHT touch inputs both connected as ASSISTED with sixEligible=false. The ground shot was collected for a dot; the loft was caught. Both deliveries resolved, and pause/background/resume checks passed.
- Actual Android screenshots were inspected for the close fielders, batting view, control labels and result history.

APK SHA-256: `eff58e5d1c63633602abc0ea5c09a53625885aecad179c326d5a325da6aa5a6e`.
Signing-certificate SHA-256: `1f44180f929324314d5be721ad1b68cdf86f1aca8d24d14e77b5c2d109716a00`.
The certificate differs from v0.2, so uninstall that test build before installing v0.3. This resets its local best score and settings.

These checks establish the scoring rule and input behavior, not subjective difficulty on a physical OnePlus. The emulator does not verify human perfect-timing accuracy, touch latency or haptic feel.

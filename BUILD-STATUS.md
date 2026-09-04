# v0.3 validation

Local behavioral tests and the balance sweep pass; see [BATTING-BALANCE.md](BATTING-BALANCE.md)
for methodology and per-grade six counts. Android CI at this source commit is pending.

The CI gate builds and lints the APK, verifies its signature, and uses real early LEFT and
RIGHT touch inputs on Android 15. Both must connect as ASSISTED, have no six eligibility,
never score six, resolve, and survive background/resume. Local tests cover manually perfect
hits and both early and late timing boundaries; the emulator test does not claim human
perfect-timing accuracy or physical OnePlus touch latency.

# v0.4 validation

Source changes are complete; Android CI verification is pending.

The required gate checks:

- a shot touch before BOWL must not start or queue a delivery;
- a premature touch after release must not produce contact;
- manually timed LEFT and RIGHT Android touches must connect;
- only the PERFECT loft may have six eligibility;
- all six named deliveries must exercise measurably different length/bounce/curve physics;
- striker and non-striker positions must swap together during a completed run;
- behavioral tests, Android lint, APK signature verification, installation, rendering and
  background/resume must pass.

Physical OnePlus latency and subjective timing feel still require a device pass.

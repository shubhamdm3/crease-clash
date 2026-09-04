# v0.3: earn the six

The v0.2 assist made every queued shot perfect internally: zero timing error, quality 1,
and maximum launch power. That removed the main batting skill. v0.3 separates keeping
contact accessible from earning boundary power.

## Research reviewed, 4 September 2026

These are official product descriptions and help pages, not hands-on tests of those apps.
No exact timing windows or proprietary scoring formulas were disclosed in these sources.

| Game | What the official source establishes | Design takeaway for this prototype |
| --- | --- | --- |
| [Stick Cricket / Stick Sports](https://www.sticksports.com/) | Simple controls, short sessions and satisfying mastery; explicitly highlights the well-timed cover drive. | Keep two buttons, but make timing meaningful. |
| [Stick Cricket Clash FAQ](https://www.sticksports.com/stickcricketclash-faq/) | Player attributes affect play; bowling upgrades unlock special deliveries. | Timing need not be the only determinant of an outcome. Existing line and pace variation remain meaningful. |
| [Cricket League](https://play.google.com/store/apps/details?hl=en&id=com.miniclip.cricketleague) and [official tutorial guide](https://support.miniclip.com/hc/en-us/articles/4409287497233--How-to-start-playing-Cricket-League) | Easy controls, a tutorial, short two-over games, and different delivery types. | Explain timing with a visible cue instead of making every successful contact a maximum-power shot. |
| [Real Cricket](https://play.google.com/store/apps/details?hl=en&id=com.nautilus.realcricket) | Shot Maps support batting strategy; a range of shot animations and fielding/catching are advertised. | Shot choice and the field still matter; perfect timing makes six possible, not guaranteed. |

The numerical tuning below is our own design, not a claimed reproduction of another game.

## Controls and outcomes

- Tap BOWL, choose ground or loft, and time HIT LEFT / HIT RIGHT as the marker enters gold.
- Club: a very early shot can still be saved as weak assisted contact. Another deliberate
  tap in the contact window replaces it; the full perfect window remains available.
- Assisted contact has at most 38% quality, retains its actual early input error, and cannot
  qualify for six. Repeated taps meet the outer contact window first, not the perfect window.
- Perfect: maximum timing quality. Only a lofted, manually perfect hit qualifies for six.
  Playing across the line reduces power; fielders can still catch it.
- Good: up to 78% quality. Early/late: progressively less power. Mistimed lofts can be caught,
  produce running runs or bounce for four; they cannot clear the rope on the full.
- Two close fielders collect weak shots sooner, preventing assisted ground shots from
  farming easy threes.
- No input still misses. Pro and Elite keep manual timing; premature taps can be retried.

| Difficulty | Contact window around arrival | Perfect window around arrival |
| --- | --- | --- |
| Club | +/-240 ms | +/-50 ms |
| Pro | +/-135 ms | +/-35 ms |
| Elite | +/-105 ms | +/-27 ms |

The meter uses these same simulation thresholds. It freezes the actual input timing after
contact and reports the grade and early/late offset. Physics caps non-perfect flight range
to land at least eight field units inside the rope; a score guard also requires perfect loft
eligibility for any six. Fours still require the ball to cross the rope after bouncing.

## Balance evidence

A deterministic 4,410-loft sweep across all difficulties, 30 spread-out seeds and 49 timing
offsets produced:

| Grade | Shots | Sixes |
| --- | ---: | ---: |
| Early | 901 | 0 |
| Good | 1,384 | 0 |
| Late | 1,005 | 0 |
| Missed (outside the window after simulation step rounding) | 27 | 0 |
| Perfect | 1,093 | 728 |

This is a controlled timing sweep, not a prediction of human six rates. Further regressions
cover 200 early queued lofts, 200 assisted ground shots, rapid tapping, timed corrections,
non-perfect landings, scoring and lifecycle behavior. Physical phone latency still affects
how the timing window feels; emulator checks do not establish that subjective balance.

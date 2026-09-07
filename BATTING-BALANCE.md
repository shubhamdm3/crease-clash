# v0.4 manual batting balance

v0.2 made queued Club shots perfect automatically. v0.3 reduced their power, but still retained
saved contact. v0.4 removes that mechanic completely.

## Researched direction

Official material for [Stick Sports](https://www.sticksports.com/) emphasizes simple controls
with satisfying mastery and specifically highlights timing. [Cricket League](https://play.google.com/store/apps/details?hl=en&id=com.miniclip.cricketleague)
uses quick two-over matches, easy controls and delivery variety, while its
[official guide](https://support.miniclip.com/hc/en-us/articles/4409287497233--How-to-start-playing-Cricket-League)
introduces mechanics through a tutorial. [Real Cricket](https://play.google.com/store/apps/details?hl=en&id=com.nautilus.realcricket)
emphasizes shot choice and field strategy. These sources do not disclose proprietary scoring
formulas; the thresholds below are original tuning for Crease Clash.

## Final rules

| Difficulty | Contact window | Perfect window |
| --- | ---: | ---: |
| Club | +/-240 ms | +/-70 ms |
| Pro | +/-135 ms | +/-35 ms |
| Elite | +/-105 ms | +/-27 ms |

- Every shot requires a fresh tap during the delivery. No shot is queued or played automatically.
- A tap outside the contact window displays EARLY/TOO LATE and does not create contact.
- The player may retry after an early attempt while the ball is still live.
- Only a manually PERFECT loft sets six eligibility. Perfect timing makes six possible, not
  guaranteed: line choice and fielders still matter.
- GOOD, EARLY and LATE lofts are physically limited to land inside the rope; they may be caught
  or bounce for four.
- Weak ground shots are collected by close fielders instead of producing easy threes.
- A visible timing meter uses the same simulation thresholds and freezes the actual offset after
  contact.

Automated rules tests exercise all difficulty windows, premature retries, scoring, physics,
fielding and the perfect-only-six invariant. Android testing additionally presses a shot before
bowling, presses too early after release, then uses actual manually timed left and right touches.

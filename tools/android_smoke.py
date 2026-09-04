"""Install and exercise the real Android APK on a booted CI emulator."""
from pathlib import Path
import struct
import subprocess
import time

PACKAGE = 'com.shubham.creaseclash'
ACTIVITY = PACKAGE + '.android.AndroidLauncher'
OUT = Path('build/android-smoke')
OUT.mkdir(parents=True, exist_ok=True)

def adb(*args, binary=False):
    return subprocess.check_output(['adb', *args], text=not binary, timeout=45)

def capture(name):
    data = adb('exec-out', 'screencap', '-p', binary=True)
    assert data[:8] == b'\x89PNG\r\n\x1a\n', 'Invalid screenshot'
    (OUT / f'{name}.png').write_bytes(data)
    return struct.unpack('>II', data[16:24])

def tap(x, y):
    scale = min(width/1440, height/810)
    physical_x = round((width-1440*scale)/2 + x*scale)
    physical_y = round((height-810*scale)/2 + y*scale)
    adb('shell', 'input', 'tap', str(physical_x), str(physical_y))

# Acknowledge the emulator's fullscreen onboarding tip before testing the game UI.
# This is a System UI tutorial preference, not an application permission.
adb('shell', 'settings', 'put', 'secure', 'immersive_mode_confirmations', 'confirmed')
adb('install', '-r', 'android/build/outputs/apk/debug/android-debug.apk')
adb('logcat', '-c')
launch = adb('shell', 'am', 'start', '-W', '-n', f'{PACKAGE}/{ACTIVITY}')
(OUT/'launch.txt').write_text(launch)
assert 'Status: ok' in launch, launch
time.sleep(3)
width, height = capture('01-menu')
assert width > height, f'Expected landscape, got {width}x{height}'

tap(400, 625)  # How to play
time.sleep(.5)
capture('02-help')
tap(720, 650)
tap(180, 625)  # Practice
time.sleep(.5)
capture('03-practice')
def wait_event(pattern, count=1, timeout=70):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        logs = adb('logcat', '-d', '-s', 'CreaseClash:I', '*:S')
        if logs.count(pattern) >= count:
            return logs
        time.sleep(.5)
    (OUT/'failed-events.txt').write_text(logs)
    raise AssertionError(f'Missing game event {pattern!r} (expected {count}): {logs}')

# Actual Android touch input: one EARLY press starts bowling and must hit on its own.
# No hidden autoplay, frame-perfect injection, or direct simulation calls.
tap(240, 730)
wait_event('event=queued side=-1')
capture('04-left-shot-queued')
wait_event('event=hit side=-1')
capture('05-left-contact')
wait_event('event=ready', count=2)
capture('06-left-result')
# Exercise opposite direction and loft selection on the same installed app.
tap(835, 730)
tap(1180, 730)
wait_event('event=queued side=1')
wait_event('event=hit side=1')
capture('07-right-contact')
wait_event('event=ready', count=3)
capture('08-two-balls-played')
events = adb('logcat', '-d', '-s', 'CreaseClash:I', '*:S')
hits = [line for line in events.splitlines() if 'event=hit ' in line]
assert len(hits) == 2, events
assert all('grade=ASSISTED' in line and 'sixEligible=false' in line for line in hits), events
assert 'lastRuns=6' not in events, 'Early queued shots must never score six'
assert 'event=run side=-1 runs=3 balls=1' not in events, 'Close fielders must stop easy assisted threes'

tap(1359, 55)  # Pause
time.sleep(.4)
capture('09-pause')

adb('shell', 'input', 'keyevent', 'KEYCODE_HOME')
time.sleep(.5)
adb('shell', 'am', 'start', '-W', '-n', f'{PACKAGE}/{ACTIVITY}')
time.sleep(1)
capture('10-return-from-background')
pid = adb('shell', 'pidof', PACKAGE).strip()
assert pid, 'Game process stopped during the smoke test'
logs = adb('logcat', '-d', f'--pid={pid}')
(OUT/'app-logcat.txt').write_text(logs)
assert 'FATAL EXCEPTION' not in logs, logs
(OUT/'result.txt').write_text(f'PASS: APK installed and launched; real early LEFT and RIGHT touch inputs each connected, both were ASSISTED with no six eligibility, neither scored six, both balls resolved, and the app survived background/resume.\nResolution: {width}x{height}\nPhysical haptic feel and OnePlus latency are not tested by an emulator.\n')
print((OUT/'result.txt').read_text())

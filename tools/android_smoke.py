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

# Shot buttons must not auto-bowl or queue a future hit.
tap(240, 730)
time.sleep(.4)
events = adb('logcat', '-d', '-s', 'CreaseClash:I', '*:S')
assert 'event=release' not in events and 'event=hit' not in events, events

# Bowl, deliberately tap too early, and prove that it does not connect.
tap(590, 730)
wait_event('event=release')
tap(240, 730)
time.sleep(.3)
events = adb('logcat', '-d', '-s', 'CreaseClash:I', '*:S')
assert 'event=hit' not in events, 'Premature touch must not create contact'

# Follow a near-arrival runtime cue with a real Android touch.
wait_event('event=timing-cue')
tap(240, 730)
wait_event('event=hit side=-1')
capture('04-left-manual-contact')
wait_event('event=ready', count=2)
capture('05-left-result')

# Exercise a manually timed loft in the opposite direction.
tap(835, 730)
tap(590, 730)
wait_event('event=release', count=2)
wait_event('event=timing-cue', count=2)
tap(1180, 730)
wait_event('event=hit side=1')
capture('06-right-manual-contact')
wait_event('event=ready', count=3)
capture('07-two-manual-balls')

events = adb('logcat', '-d', '-s', 'CreaseClash:I', '*:S')
hits = [line for line in events.splitlines() if 'event=hit ' in line]
assert len(hits) == 2, events
assert all('grade=PERFECT' in line for line in hits), events
assert 'event=hit side=-1' in hits[0] and 'sixEligible=false' in hits[0], hits
assert 'event=hit side=1' in hits[1] and 'sixEligible=true' in hits[1], hits
tap(1359, 55)  # Pause
time.sleep(.4)
capture('08-pause')

adb('shell', 'input', 'keyevent', 'KEYCODE_HOME')
time.sleep(.5)
adb('shell', 'am', 'start', '-W', '-n', f'{PACKAGE}/{ACTIVITY}')
time.sleep(1)
capture('09-return-from-background')
pid = adb('shell', 'pidof', PACKAGE).strip()
assert pid, 'Game process stopped during the smoke test'
logs = adb('logcat', '-d', f'--pid={pid}')
(OUT/'app-logcat.txt').write_text(logs)
assert 'FATAL EXCEPTION' not in logs, logs
(OUT/'result.txt').write_text(f'PASS: APK installed and launched; shot buttons did not auto-bowl, a premature touch did not connect, and real manually timed LEFT and RIGHT touches connected as PERFECT. Both balls resolved and the app survived background/resume.\nResolution: {width}x{height}\nPhysical haptic feel and OnePlus latency are not tested by an emulator.\n')
print((OUT/'result.txt').read_text())

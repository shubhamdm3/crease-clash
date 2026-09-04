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
tap(780, 725)  # Bowl
time.sleep(1.8)
capture('04-delivery')
tap(482, 725)  # One real shot input, timing deliberately unconstrained
time.sleep(3.5)
capture('05-after-delivery')
tap(1359, 55)  # Pause
time.sleep(.4)
capture('06-pause')

adb('shell', 'input', 'keyevent', 'KEYCODE_HOME')
time.sleep(.5)
adb('shell', 'am', 'start', '-W', '-n', f'{PACKAGE}/{ACTIVITY}')
time.sleep(1)
capture('07-return-from-background')
pid = adb('shell', 'pidof', PACKAGE).strip()
assert pid, 'Game process stopped during the smoke test'
logs = adb('logcat', '-d', f'--pid={pid}')
(OUT/'app-logcat.txt').write_text(logs)
assert 'FATAL EXCEPTION' not in logs, logs
(OUT/'result.txt').write_text(f'PASS: APK installed, launched, accepted input, stayed alive through a delivery and background/resume.\nResolution: {width}x{height}\nPhysical haptic feel and OnePlus latency are not tested by an emulator.\n')
print((OUT/'result.txt').read_text())

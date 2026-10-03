"""Capture real app UI on an explicitly selected disposable Codex_ emulator.

Seeds synthetic records ONLY in that emulator's debug database. Never use on a
physical device or the user's regular AVD. Does not publish images to Play.
"""
import argparse
import json
import html
import os
from pathlib import Path
import sqlite3
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET
import zipfile

PACKAGE = 'com.nullpointer.nourseCompose'
ROOT = Path(__file__).resolve().parents[2]
parser = argparse.ArgumentParser()
parser.add_argument('--serial', required=True)
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()
ADB = str(Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platform-tools/adb.exe')

def adb(*command, binary=False):
    result = subprocess.run([ADB, '-s', args.serial, *command], capture_output=True, check=True)
    return result.stdout if binary else result.stdout.decode('utf-8', errors='replace').strip()

if not args.serial.startswith('emulator-') or not adb('emu', 'avd', 'name').startswith('Codex_'):
    raise SystemExit('Requires disposable Codex_ emulator')
args.output.mkdir(parents=True, exist_ok=True)
original_locale = adb('shell', 'cmd', 'locale', 'get-app-locales', PACKAGE)
original_night = adb('shell', 'cmd', 'uimode', 'night')
results = []

def ui():
    for _ in range(3):
        try:
            adb('shell', 'uiautomator', 'dump', '/sdcard/store-capture.xml')
            return ET.fromstring(adb('shell', 'cat', '/sdcard/store-capture.xml'))
        except (ET.ParseError, subprocess.CalledProcessError):
            time.sleep(1)
    raise RuntimeError('UI hierarchy unavailable')

def tap(label):
    import re
    nodes = [n for n in ui().iter('node') if label in (n.get('text'), n.get('content-desc'))]
    if not nodes:
        raise RuntimeError(f'Missing label: {label}')
    bounds = list(map(int, re.findall(r'\d+', nodes[-1].get('bounds'))))
    adb('shell', 'input', 'tap', str((bounds[0]+bounds[2])//2), str((bounds[1]+bounds[3])//2))
    time.sleep(.6)

def reset(strings):
    adb('shell', 'am', 'force-stop', PACKAGE)
    adb('shell', 'am', 'start', '-n', PACKAGE+'/.MainActivity')
    time.sleep(1.5)
    if any(n.get('text') == strings['intro_skip'] for n in ui().iter('node')):
        tap(strings['intro_skip'])

def capture(combo, name):
    time.sleep(.8)
    tree = ui()
    path = args.output / f'{combo}-{name}.png'
    path.write_bytes(adb('exec-out', 'screencap', '-p', binary=True))
    (args.output / f'{combo}-{name}.xml').write_bytes(ET.tostring(tree, encoding='utf-8'))
    results.append({'combination': combo, 'screen': name, 'path': str(path), 'synthetic_data': True})
    print(f'Captured {combo}/{name}', flush=True)

def seed():
    adb('shell', 'am', 'force-stop', PACKAGE)
    with tempfile.TemporaryDirectory() as folder:
        dbpath = Path(folder) / 'measure.db'
        dbpath.write_bytes(adb('exec-out', 'run-as', PACKAGE, 'cat', 'databases/measure.db', binary=True))
        # Copy WAL too: force-stop does not guarantee checkpointing.
        for suffix in ('-wal', '-shm'):
            result = subprocess.run([ADB, '-s', args.serial, 'exec-out', 'run-as', PACKAGE, 'cat', 'databases/measure.db'+suffix], capture_output=True)
            if result.returncode == 0 and result.stdout:
                Path(str(dbpath)+suffix).write_bytes(result.stdout)
        db = sqlite3.connect(dbpath)
        now = int(time.time()*1000)
        db.execute('DELETE FROM measures')
        db.execute('DELETE FROM medication_reminders')
        db.execute('DELETE FROM alarm_logs')
        samples = {'GLUCOSE': [92,96,94,101,98,95], 'PRESSURE': [118,120,117,122,119,121],
                   'TEMPERATURE': [36.5,36.6,36.4,36.7,36.5,36.6], 'OXYGEN': [98,97,98,99,98,97]}
        for kind, values in samples.items():
            for index, value in enumerate(values):
                db.execute('INSERT INTO measures(value1,value2,type,createAt) VALUES(?,?,?,?)',
                           (value, 78 if kind == 'PRESSURE' else None, kind, now-(5-index)*86400000))
        for index in (1,2):
            db.execute('''INSERT INTO medication_reminders(id,name,dosage,comment,photoUri,startAt,endAt,
                       intervalHours,intervalMinutes,isActive,useExactAlarm,notificationMode,
                       vibrationEnabled,soundEnabled,fullScreenAlarm) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)''',
                       (index, f'Demo {chr(64+index)}', None, 'Demo', None, now+index*3600000, None,
                        8,480,1,0,'NOTIFICATION',0,0,0))
        for index, event in enumerate(['MEDICATION_TAKEN','MEDICATION_NOT_TAKEN','ALARM_DISMISSED']):
            db.execute('''INSERT INTO alarm_logs(reminderId,reminderName,eventType,occurredAt,success,
                       details,isFirstReminder,category,severity,stackTrace) VALUES(?,?,?,?,?,?,?,?,?,?)''',
                       (1,'Demo A',event,now-index*3600000,1,None,0,'ALARM','INFO',None))
        db.commit()
        db.execute('PRAGMA wal_checkpoint(TRUNCATE)')
        db.close()
        adb('push', str(dbpath), '/data/local/tmp/nurse-store-demo.db')
        adb('shell', 'run-as', PACKAGE, 'cp', '/data/local/tmp/nurse-store-demo.db', 'databases/measure.db')
        adb('shell', 'run-as', PACKAGE, 'rm', '-f', 'databases/measure.db-wal', 'databases/measure.db-shm')
        adb('shell', 'rm', '/data/local/tmp/nurse-store-demo.db')

try:
    seed()
    adb('shell', 'settings', 'put', 'global', 'sysui_demo_allowed', '1')
    adb('shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'clock', '-e', 'hhmm', '0941')
    for language in ('es', 'en'):
        resource = ROOT / 'app/src/main/res' / ('values-es' if language == 'es' else 'values') / 'strings.xml'
        strings = {s.get('name'): ''.join(s.itertext()) for s in ET.parse(resource).getroot().findall('string')}
        adb('shell', 'cmd', 'locale', 'set-app-locales', PACKAGE, '--locales', language)
        for night in ('no', 'yes'):
            combo = language + ('-dark' if night == 'yes' else '-light')
            adb('shell', 'cmd', 'uimode', 'night', night)
            reset(strings)
            capture(combo, '01-glucose')
            for key, name in [('pressure','02-pressure'),('temperature','03-temperature'),('oxygen','04-oxygen'),('medications','05-reminders')]:
                tap(strings['title_'+key]); capture(combo, name)
            tap(strings['action_add_medication'])
            capture(combo, '06-new-reminder')
            adb('shell', 'input', 'swipe', '540', '1700', '540', '700', '450')
            capture(combo, '07-reminder-schedule')
            tap(strings['interval_unit_minutes'])
            capture(combo, '07b-reminder-minutes')
            tap(strings['schedule_single_dose'])
            capture(combo, '08-single-dose')
            reset(strings)
            tap(strings['action_open_menu']); capture(combo, '09-menu')
            tap(strings['title_medication_history']); capture(combo, '10-medication-history')
            reset(strings)
            tap(strings['action_open_menu']); tap(strings['title_reports']); capture(combo, '11-reports')
finally:
    import re
    match = re.search(r'\[(.*?)\]', original_locale)
    command = ['shell', 'cmd', 'locale', 'set-app-locales', PACKAGE]
    if match and match.group(1): command += ['--locales', match.group(1)]
    adb(*command)
    match = re.search(r'\b(yes|no|auto|custom_schedule|custom_bedtime)\b', original_night)
    if match: adb('shell', 'cmd', 'uimode', 'night', match.group(1))
    adb('shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'exit')
    (args.output/'manifest.json').write_text(json.dumps(results, indent=2), encoding='utf-8')
    cards = ''.join(f'<figure><a href="{html.escape(Path(r["path"]).name)}"><img loading="lazy" src="{html.escape(Path(r["path"]).name)}" alt="{html.escape(r["combination"]+" — "+r["screen"])}"></a><figcaption>{html.escape(r["combination"]+" — "+r["screen"])}</figcaption></figure>' for r in results)
    gallery = '<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>NurseApp — capturas nuevas</title><style>body{font:16px system-ui;background:#171317;color:#fff;margin:24px}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:20px}figure{margin:0;background:#2b2329;padding:12px;border-radius:16px}img{width:100%;height:auto}figcaption{padding-top:12px}a{color:#ffb3c8}</style><h1>NurseApp — capturas reales</h1><p>3 de octubre de 2026 · Android 14 · datos de ejemplo · español e inglés · claro y oscuro. Pulsa una imagen para ver el PNG original. No se han publicado en Google Play.</p><main>'+cards+'</main></html>'
    (args.output/'index.html').write_text(gallery, encoding='utf-8')
    for language in ('es', 'en'):
        with zipfile.ZipFile(args.output/f'capturas-{language}.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
            for result in results:
                if result['combination'].startswith(language+'-'):
                    path = Path(result['path'])
                    archive.write(path, path.name)
    print(f'{len(results)} screenshots; original locale and theme restored.', flush=True)

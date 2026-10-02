# Fiabilidad de alarmas — 2026-10-02

## Objetivo

Prioridad 1 solicitada por el usuario: permisos, reinicio, ahorro de batería, bloqueo con PIN, alarmas simultáneas, Posponer y timeout. Alcance ejecutado: Android 13 y 14. Android 15 y 16 pendientes de instalación de imágenes por el usuario; no se consideran aprobados.

## Entornos

- Android 13 / API 33: emulator-5554, Medium_Phone_API_33. Usuario autorizó tratar sus datos como desechables. No se desinstaló ni se borró la app en esta ronda; solo adb install -r y limpieza de fixtures QA.
- Android 14 / API 34: emulator-5556, AVD separado Codex_Alarm_API_34, creado con imagen oficial Google APIs x86_64 ya instalada. Sin cuenta Google ni datos del usuario. PIN de QA temporal 2468, retirado al terminar.
- Nuevas pruebas con prefijo __QA_RELIABILITY__; limpieza por ID y nombre exactos, sin deleteAll ni pm clear.
- No se usa connectedDebugAndroidTest. La suite requiere -e reliabilitySuite true; por defecto se omite para no modificar permisos ni ejecutar esperas largas en un pipeline general. El ejecutor explícito rechaza pruebas omitidas como aprobación.

## Fallos reproducidos y corregidos

### Canal de notificaciones bloqueado

Antes: showNotification informaba posted=true y arrancaba MediaPlayer aunque el canal tuviera IMPORTANCE_NONE. La prueba falló con «Blocked channel must not be reported as posted».

Ahora: comprobar importancia del canal después de crearlo/consultarlo; devolver posted=false con reason=channel blocked antes de publicar o iniciar servicio. El receiver registra ALARM_FAILED en lugar de éxito. No se altera el canal ni se intenta sobrepasar la elección del usuario.

### Alarmas coincidentes

Antes: al llegar una segunda alarma, el servicio cancelaba la notificación anterior. La prueba falló con «First reminder controls must not disappear».

Ahora: un solo reproductor sigue atendiendo la ocurrencia más reciente, pero se conserva la anterior como notificación normal, silenciosa y con controles independientes. No se vuelve a disparar su full-screen intent al conservarla. Tomado, Posponer o descartar una ocurrencia anterior no detienen la diferente que está sonando. Posponer la anterior la reprograma y retira únicamente sus controles. Se registra ALARM_RINGING_REPLACED y las acciones llevan ID/nombre correctos.

Decisión: conservar todos los avisos pendientes sin reproducir varios sonidos a la vez. No se añadió una cola automática de reproducción ni se marca ninguna toma por la llegada de otra alarma. No hay cambios de esquema de base de datos.

### Precisión al posponer

Antes: snooze usaba siempre setAndAllowWhileIdle, aunque la app tuviera acceso a alarmas exactas.

Ahora: setExactAndAllowWhileIdle (setExact antes de API 23) cuando el acceso está permitido, con fallback inexacto si falta permiso o la llamada exacta falla. No se promete puntualidad cuando el usuario niega acceso a alarmas exactas.

[Alarmas exactas y permisos Android](https://developer.android.com/develop/background-work/services/alarms).

## Resultados verificados

| Caso | Android | Resultado |
| --- | --- | --- |
| Canal bloqueado: no publica éxito ni arranca sonido | 14 | Aprobado, después de reproducir fallo |
| Dos alarmas: conserva ambos controles; Posponer la anterior no detiene la actual | 14 | Aprobado, después de reproducir fallo |
| POST_NOTIFICATIONS denegado: registra fallo y no inicia sonido | 14 | Aprobado |
| Acceso exacto denegado: registra programación inexacta, sin crash | 14 | Aprobado; no prueba puntualidad de entrega |
| Full-screen denegado: mantiene notificación/servicio, no abre automáticamente actividad en segundo plano | 14 | Aprobado |
| PIN real: actividad visible sobre bloqueo; dispositivo sigue protegido | 14 | Aprobado |
| Doze forzado: entrega de alarma completa | 13 y 14 | Aprobado |
| Reinicio real: restaura programación y vuelve a entregar | 13 | Aprobado |
| Timeout real de cinco minutos: termina aviso foreground | 13 | Aprobado; ejecución 311.666 s |
| Posponer real diez minutos: segunda ocurrencia vuelve a sonar en ventana de 590–660 s | 14 | Aprobado; ejecución 608.371 s |

Suite corta API 34: 9/9 aprobados, incluidos dos casos de recuperación/limpieza. Los siete escenarios funcionales son los restantes. Tiempos son de la ejecución completa de cada test; no equivalen exactamente al tiempo entre pulsación y nueva alarma.

Verificación final: assembleDebug y assembleDebugAndroidTest aprobaron; tres casos básicos de interfaz/servicio en API 33 aprobaron con el APK actualizado (Tomado, Posponer y entrega con pantalla apagada). Tras introducir el opt-in, se repitieron simultaneidad y recuperación con reliabilitySuite=true en API 34: 2/2, sin pruebas omitidas. Parser de PowerShell y git diff --check sin errores. Emulador principal restaurado a fuente 1.0, oscuro yes, locales de app [], Doze ACTIVE; APK actualizado instalado.

Evidencia en D:/Documents/Projects/Android/NurseCompose2/build:

- qa-api34-regression-before.log — dos fallos previos.
- qa-api34-regression-after.log y qa-api34-final-regressions.log — dos casos corregidos aprobados.
- qa-api34-reliability-suite/results.json y logs por caso — suite corta completa.
- qa-api33-timeout.log y qa-api34-real-snooze.log — esperas reales, sin adelantar reloj ni reducir tiempos productivos.
- qa-api33-after-reboot-3.log — restauración y entrega real tras reinicio.
- qa-api33-doze.log — Doze en API 33.
- qa-api34-denied-fullscreen.log y qa-api34-pin-lock.log — restricciones y PIN.
- qa-api33-final-basic.log y qa-api34-opt-in-final.log — verificaciones finales con APK/tests actualizados.

## Particularidad del ejecutor tras reiniciar

Dos primeros intentos fallaron al comenzar instrumentación antes de la entrega diferida de BOOT_COMPLETED. am instrument fuerza la detención del paquete al arrancar y puede retirar una entrega pendiente. sys.boot_completed=1 no significa que todos los receivers hayan finalizado: se observó la cola de boot despachándose y terminando más de 40 s después.

El ejecutor confirma cambio de /proc/sys/kernel/random/boot_id, espera desbloqueo/boot y otros 90 s antes de iniciar la verificación. Se observó la alarma programada en dumpsys alarm antes de iniciar instrumentación, y después aprobaron tanto el registro de reprogramación como la entrega. No se modificó el receiver productivo para compensar un problema del runner.

## Repetir y ampliar a API 35/36

Compilar e instalar app y androidTest con adb install -r sobre un AVD de QA dedicado. Usar siempre serial explícito cuando hay varios dispositivos.

Ejemplo:

```powershell
./tools/qa/Test-AlarmReliability.ps1 -Serial emulator-5556 -DisposableEmulator -OutputDirectory build/qa-api34-reliability -IncludePin -IncludeReboot -IncludeRealTiming
```

IncludeRealTiming añade al menos quince minutos reales; IncludeReboot reinicia el emulador y IncludePin configura una credencial temporal exclusivamente en un AVD llamado Codex_Alarm_API_. El script restaura permisos/AppOps iniciales, quita PIN que él mismo puso, sale de Doze, restablece batería simulada y recupera solo fixtures QA.

La descarga CLI de imágenes 35/36 se bloqueó en lectura de red. Se detuvieron únicamente los procesos de descarga propios antes de que el usuario continúe desde SDK Manager. No se instaló una imagen sin verificación de checksum. El usuario confirmó que instalará Google APIs x86_64 para API 35/36.

Android 15+ restringe iniciar mediaPlayback desde BOOT_COMPLETED: el receiver actual solo reprograma alarmas en esa rama, no inicia ese servicio directamente. Aun así, requiere prueba real en esos SDK; no asumir equivalencia con API 34. [Restricciones oficiales Android 15](https://developer.android.com/about/versions/15/changes/foreground-service-types).

## Pendientes y límites

- Repetir suite en Android 15 y 16 cuando estén disponibles.
- Repetir reinicio con PIN: con base credential-encrypted, la restauración se produce después de desbloquear; no se promete entrega antes de primer desbloqueo.
- La prueba de fallo por permiso exacto valida fallback/registro, no precisión sin permiso.
- AudioPlaying demuestra estado del reproductor, no audibilidad física; vibración/volumen y restricciones de fabricantes necesitan dispositivo real.
- TalkBack queda para prioridad 2.
- No se hizo commit/push en esta ronda.

## Archivos relevantes

- app/src/main/java/com/nullpointer/nourseCompose/notifications/MedicationReminderScheduler.kt — canal bloqueado, precisión de snooze y metadata de acciones.
- app/src/main/java/com/nullpointer/nourseCompose/notifications/MedicationAlarmService.kt — conservación de controles y aislamiento entre ocurrencias.
- app/src/androidTest/java/com/nullpointer/nourseCompose/medication/MedicationAlarmReliabilityTest.kt — regresiones y escenarios reales.
- tools/qa/Test-AlarmReliability.ps1 — runner explícito, serial, permisos y restauración.

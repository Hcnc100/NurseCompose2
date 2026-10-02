# Alarmas y accesibilidad — 2026-10-02

**Informe histórico antes de la corrección:** la pantalla completa se rediseñó posteriormente. Ver `D:/Documents/Projects/Android/NurseCompose2/docs/qa/ALARM_REDESIGN_2026-10-02.md` para cambios y resultados actuales.

## Entorno y límites

Emulador Medium Phone API 33 (Android 13). Notificaciones, alarmas exactas y full-screen intent permitidos al comenzar. TalkBack (`com.google.android.marvin.talkback`) no instalado; servicios de accesibilidad activos: ninguno. La inspección de `AccessibilityNodeInfo` valida etiquetas/acciones expuestas, **no** la voz, gestos ni orden efectivo de TalkBack.

TalkBack requiere [Android Accessibility Suite oficial](https://play.google.com/store/apps/details?id=com.google.android.marvin.talkback) y [activación en Accesibilidad](https://support.google.com/accessibility/android/answer/6007100). Pendiente prueba real con el servicio instalado. Android 14+ añade acceso especial a pantalla completa: [documentación oficial](https://developer.android.com/about/versions/14/behavior-changes-14). No extrapolar resultados API 33 a API 34–36.

## Incidente del ejecutor (importante)

**Aclaración posterior del usuario:** es un emulador desechable y sus datos no importan. Se puede continuar la verificación en este emulador; esta autorización no se extiende a teléfonos reales ni a otros entornos.

La invocación inicial de `connectedDebugAndroidTest` desinstaló al finalizar el paquete debug `com.nullpointer.nourseCompose`. Se confirmó mediante `pm list packages` y `run-as`; después de reinstalar el APK con `adb install -r`, no existía la carpeta de bases de datos. **La base local anterior del paquete debug ya no está disponible.** Se informó al usuario; no se recrearon sus mediciones ni recordatorios a partir de capturas. Los otros paquetes presentes, `com.nullpointer.noursecompose` y `com.nullpointer.nourseapp2`, no fueron desinstalados por estas acciones.

El primer intento falló por un EntryPoint Hilt de androidTest no incluido en el componente de producción; se reemplazó por proveedores/repositorios reales sobre la base usada por la app, sin modificar código productivo. El segundo intento omitió las pruebas porque la reinstalación dejó las notificaciones sin permiso. Un `BUILD SUCCESSFUL` con tests omitidos **no cuenta como aprobación**.

Se reinstaló el APK debug y el APK de pruebas. Las siguientes ejecuciones usan `adb shell am instrument` directamente, sin tareas de instalación/desinstalación de Gradle, y el permiso de notificaciones se devolvió al estado concedido observado antes. Para futuras rondas: usar emulador desechable y respaldo verificado antes de instrumentación, nunca el entorno con datos del usuario.

## Pruebas añadidas

`MedicationAlarmInstrumentedTest.kt`:

- Entrega normal por AlarmManager → receiver → notificación (sin full-screen intent).
- Pantalla completa en primer plano, inicio de MediaPlayer y botón Tomado.
- Posponer: registro, detención del servicio y cierre de controles. No espera los diez minutos; elimina la alarma de prueba después.
- Entrega con pantalla apagada y app en segundo plano.
- Nombre largo y fuente 2.0: etiquetas, botones visibles y altura táctil de 48dp.

Fixtures identificados como `__QA_ALARM__`; limpieza solo por ID/nombre exactos. Nunca `deleteAll()` ni `pm clear`. Captura PNG y nodos en `getExternalFilesDir("qa-alarm")`; extraerlos antes de cualquier desinstalación. MediaPlayer activo demuestra inicio de reproducción, no audibilidad física ni vibración percibida en un teléfono real.

## Hallazgos estáticos verificados

- `MedicationAlarmActivity` usa `MaterialTheme` predeterminado y un gradiente oscuro fijo, no el tema claro/oscuro de la aplicación. La alarma no está homologada con el resto del diseño.
- Su columna centrada no tiene scroll ni adaptación explícita para nombres largos/fuente ampliada. Requiere prueba de visibilidad real de los botones, no asumir que `fillMaxWidth()` la garantiza.
- La acción STOP de la notificación tiene texto español fijo («Detener») en `showNotification`, incluso con locale inglés.
- La actividad no maneja Atrás para detener la alarma; el servicio controla el sonido de forma independiente. Revisar que salir no marque una toma ni deje sin acceso a los controles.

## Resultados

Primera ejecución directa: **5 pruebas, 4 aprobadas y 1 fallida** (inglés/oscuro). Entrega normal, pantalla completa en primer plano, pantalla apagada y Posponer aprobadas. La prueba de fuente 2.0 y nombre largo falla porque «Tomado» desaparece del árbol accesible y de la pantalla. La dosis también se recorta; no hay scroll para recuperar los botones. No se han corregido aún estos hallazgos de UI.

Medición de contraste en `screen-off-full-screen.png`: píxeles sólidos del texto Posponer `#6750A4`, fondo local `#170D19`, contraste **2.94:1**. Es inferior incluso al umbral de 3:1 para texto grande y al de 4.5:1 para texto normal. Método: histogramar el rectángulo de texto `(384,1995)-(696,2048)`, obtener el color sólido dominante del texto y calcular luminancia relativa sRGB contra un píxel de fondo contiguo. [Umbrales Android](https://developer.android.com/guide/topics/ui/accessibility/apps).

Evidencia local: `C:/Users/ricar/.codex/visualizations/2026/10/01/01a0f788-ef05-7a12-81b3-bb73c72cd7ed/alarm-en-dark`. La notificación heads-up puede cubrir temporalmente el encabezado al comenzar la alarma; se conservan también capturas sin esa superposición (pantalla apagada).

Matriz definitiva terminada: `Test-AlarmMatrix.ps1`, **24 pruebas: 20 aprobadas y 4 fallidas**. Incluye Atrás, que no registra una toma y conserva los controles de notificación. La salida de `am instrument` puede tener código de proceso 0 aun con tests fallidos: el script analiza `FAILURES`/conteos en lugar de confiar solo en `$LASTEXITCODE`.

| Idioma / tema | Casos normales | Fuente 200 % + nombre largo |
| --- | --- | --- |
| Inglés / claro | 5 aprobados | Fallo: acción Mark as taken ausente |
| Inglés / oscuro | 5 aprobados | Fallo: acción Mark as taken ausente |
| Español / claro | 5 aprobados | Fallo: acción Marcar como tomada ausente |
| Español / oscuro | 5 aprobados | Fallo: acción Marcar como tomada ausente |

Se verificó al terminar: app instalada, fuente restaurada a 1.0, modo oscuro `yes`, locales de app `[]` (configuración original). Evidencia definitiva y conteos: `C:/Users/ricar/.codex/visualizations/2026/10/01/01a0f788-ef05-7a12-81b3-bb73c72cd7ed/alarm-matrix-verified/results.json` y logs de cada combinación. Los nodos de español contienen «Marcar como tomada» y «Posponer 10 minutos». **Limitación de capturas:** se detectó un PNG de `es-light/screen-off-full-screen.png` en inglés mientras sus nodos correspondientes son españoles; el directorio remoto acumula capturas y no se garantiza sincronía PNG/nodos. Los conteos y aserciones provienen de instrumentación; no considerar todos los PNG como validación visual inequívoca del idioma. Repetir capturas con sincronización y directorios limpios antes de aprobar visualmente cada combinación.

Se intentó abrir la ficha oficial de TalkBack en Play Store; requiere iniciar sesión. No se introdujeron credenciales ni se descargaron APKs de terceros. Se solicitó al usuario instalar la suite oficial si desea completar la prueba real de TalkBack.

Dos intentos de matriz se interrumpieron por un bloqueo de lanzamiento sincrónico con `ActivityScenario` en la alarma animada/fuente 2.0. No se cuentan esas ejecuciones como completas ni como fallos funcionales adicionales. Se separó la prueba de fuente 2.0: el script configura la escala **antes** de iniciar instrumentación y la restaura al terminar. El caso inicia la actividad en el hilo principal y espera el estado accesible observable, en vez de esperar sincronía de reposo de toda la actividad animada. Se añadió timeout de 60 s y una espera de 700 ms antes de capturar transiciones de entrada; no se quitaron ni relajaron las aserciones de visibilidad de botones. Captura con `screencap` en lugar de depender de una captura sincrónica UiAutomation.

Verificación aislada del ejecutor corregido: caso grande termina en 23 s con **el fallo esperado de botones inaccesibles**, no un bloqueo. Las cinco pruebas normales terminaron en una ejecución separada con 0 fallos. Matriz definitiva: `alarm-matrix-verified`.

El test `recoverOnlyAbandonedQaFixtures` elimina exclusivamente fixtures con prefijo `__QA_ALARM__`, cancelando sus alarms y sus logs por ID/nombre. Se ejecutó y aprobó después de interrumpir la matriz; no se eliminan registros ajenos al prefijo.

## Siguiente corrección recomendada

1. Tema de aplicación también en la alarma, sin gradiente/colores de controles ajenos a sus tokens.
2. Zona de contenido desplazable/adaptable y acciones persistentes; reducir ilustración antes de sacrificar nombre, dosis o botones. Márgenes de sistema seguros.
3. Texto Posponer con contraste suficiente y acciones de notificación localizadas.
4. Probar TalkBack real con sonido de alarma activo: navegación por título/nombre/dosis/acciones, identificación de botones, activación de Tomado/Posponer y audibilidad del lector frente al sonido de alarma.
5. Completar Android 14–16, permisos denegados, canal deshabilitado, Doze, reinicio, varias alarmas coincidentes, timeout de cinco minutos y repetición tras posponer diez minutos. Emulador con pantalla apagada no equivale a bloqueo con PIN ni a un teléfono físico.

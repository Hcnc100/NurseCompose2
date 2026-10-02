# Pantalla de alarma homologada — 2026-10-02

## Objetivo y alcance

Prioridad solicitada: corregir la pantalla completa de alarma y revisar su apariencia con capturas reales. Se conserva el comportamiento de Tomado, Posponer, Atrás y el servicio de alarma. No se modifica el resto de las pantallas ni se afirma haber probado TalkBack real.

## Cambios

- Usa MyApplicationTheme: mismos tokens rosados, tipografía y radios que el resto de la app, tanto claro como oscuro.
- Elimina el gradiente fijo, controles morados y animación decorativa grande. Icono estático decorativo; foto opcional compacta de 120dp.
- Nombre y dosis en tarjeta con texto que puede envolver y contenido desplazable.
- Acciones en zona inferior independiente del scroll, sin alturas fijas que recorten texto; mínimo 56dp y padding adaptable.
- Título y nombre expuestos como encabezados; icono/foto decorativos no generan anuncios duplicados.
- Respeta insets de sistema y ajusta iconos de barras al tema. Ancho máximo de contenido 600dp.
- La actividad declara Theme.MyApplication en el manifiesto, en vez de heredar el tema de splash de la aplicación; desaparece el encabezado nativo NurseApp.

Contraste calculado de Posponer (tokens sRGB): **6.26:1 claro**, **10.91:1 oscuro**. Antes: 2.94:1 en la captura de alarma oscura. Son cálculos de los pares de colores usados, no de píxeles suavizados de los glifos.

## Evidencia visual

Captura independiente con adb durante una pausa de prueba, español/claro y fuente 200%:

`C:/Users/ricar/.codex/visualizations/2026/10/01/01a0f788-ef05-7a12-81b3-bb73c72cd7ed/alarm-redesign/es-light-large-verified.png`

Se revisó la imagen: nombre largo y dosis visibles, dos acciones completas, texto alineado, sin encabezado nativo ni superposición con navegación de sistema. El contenido sigue admitiendo scroll para nombres/dosis aún mayores. Los datos son fixtures de QA, no instrucciones de medicación.

## Verificación

- assembleDebug y assembleDebugAndroidTest compilan.
- Primera matriz del rediseño: 23/24; único fallo en cierre de ActivityScenario tras pantalla apagada, no en entrega ni acciones. Los cuatro casos de fuente 200% aprobaron. Se corrigió teardown para terminar también actividades pausadas/detenidas.
- Se añadió pausa visual opcional y se captura desde UiAutomation con escritura desde el proceso de pruebas; se espera la existencia de acciones antes de capturar el caso grande.
- Captura independiente anterior con versión final del manifiesto: caso grande español/claro aprobado (1 test).
- Matriz definitiva: **23 aprobados de 24**, con un error del capturador UiAutomation (Screenshot unavailable) en Posponer español/claro. No se cuenta ese caso como aprobado. Inglés/claro, inglés/oscuro y español/oscuro: 6/6 cada uno; español/claro: 5/6. **Los cuatro casos de texto 200% aprobaron**, frente a los cuatro fallos originales.
- Se añadió reintento acotado de captura (tres intentos) y espera de acciones accesibles antes de capturar Posponer. **Reejecución aislada de español/claro aprobada: OK (1 test), 22.63 s**, en `D:/Documents/Projects/Android/NurseCompose2/build/qa-alarm-snooze-retry.log`. Así quedan verificados los 24 casos funcionales entre matriz y reejecución; la matriz original conserva su error de captura, no se reescribe como 24/24. Idioma y modo originales restaurados al terminar la reejecución.
- Resultados conservados sin alteración en `C:/Users/ricar/.codex/visualizations/2026/10/01/01a0f788-ef05-7a12-81b3-bb73c72cd7ed/alarm-redesign-final/results.json`.
- Se revisaron visualmente capturas finales de alarma sin heads-up en inglés/claro y español/oscuro: temas adecuados, alineaciones coherentes, controles rosados con texto legible y barras de sistema sin el título nativo anterior. El tema explícito del manifiesto permite capturar la alarma correcta, en vez del splash/pantalla previa que se observó en la primera ronda.
- Escala de fuente 1.0, modo oscuro y locales de app originales restaurados por el ejecutor de matriz.

## Pendientes / límites

- TalkBack no instalado: pruebas de nodos accesibles no sustituyen voz, gestos ni mezcla de audio.
- Android 13 solamente; no valida Android 14–16, bloqueo con PIN, Doze ni un dispositivo físico.
- La acción «Detener» de la notificación permanece sin localizar; es ajena al rediseño de la actividad y requiere corrección posterior.
- No se verificó repetición real tras diez minutos, timeout de cinco minutos ni alarmas simultáneas.
- Publicación solicitada por el usuario en la rama feature/closed-testing-pipeline. Instalación directa con adb install -r, sin connectedDebugAndroidTest ni borrado de datos.

## Archivos relevantes

- app/src/main/java/com/nullpointer/nourseCompose/notifications/MedicationAlarmActivity.kt — composición y tema de alarma.
- app/src/main/AndroidManifest.xml — tema propio de la actividad.
- app/src/androidTest/java/com/nullpointer/nourseCompose/medication/MedicationAlarmInstrumentedTest.kt — regresión y captura.
- tools/qa/Test-AlarmMatrix.ps1 — matriz con restauración de idioma/tema/fuente.

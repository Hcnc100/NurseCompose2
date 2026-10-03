# Recordatorios en minutos u horas — 3 de octubre de 2026

## Cambio
Selector Minutos/Horas en el formulario, oculto para una sola toma junto con el intervalo. Los nuevos recordatorios comienzan en 1 hora. Al editar, los intervalos múltiplos de 60 se muestran en horas; los demás, en minutos. Cambiar de unidad convierte el valor, no reinterpreta silenciosamente el horario.

La persistencia continúa usando `intervalMinutes`: no se modificó el esquema Room ni el planificador. Las horas admiten coma/punto decimal y se redondean al minuto más cercano, indicado en el formulario. La selección y el valor se restauran tras recreación. Valores inválidos, cero y desbordamientos impiden guardar una recurrencia; no bloquean una toma única con el campo oculto.

## Verificación
- Compilación de APK debug y APK de instrumentación completada.
- 8 pruebas unitarias enfocadas: conversión/unidades y horarios; 0 fallos.
- Suite unitaria completa: 45 pruebas, 0 fallos y 0 errores.
- 14 pruebas de `MedicationEditorUiTest` en Android 14: 0 fallos. Incluyen conversión 8 h ↔ 480 min, guardado de 1,5 h como 90 minutos, restauración y ocultación para toma única.
- 48 capturas reales: 12 estados × español/inglés × claro/oscuro. PNG 1080 × 2340; dos ZIP de 24 imágenes, integridad comprobada.
- Evidencia: `C:/Users/ricar/.codex/visualizations/2026/10/01/01a0f788-ef05-7a12-81b3-bb73c72cd7ed/play-store-units-2026-10-03/`.

Datos de ejemplo únicamente en `Codex_Alarm_API_34`, serial `emulator-5556`; no se modificó el dispositivo físico ni `emulator-5554`. El emulador de prueba se cerró. Las capturas proceden del APK debug, no certifican el AAB firmado ni entrega de alarmas. No incluyen PDF abierto ni alarma completa.

## Ficha
Los textos ES/EN en `docs/google-play-store-listing.md` incluyen minutos/horas. Se trabaja con «Guardar como borrador» en Play Console: esto no publica una versión de producción ni sustituye las declaraciones pendientes de privacidad, datos, audiencia o permisos.

Se actualizaron nombre/descripciones en español latinoamericano (`es-419`) y se añadió traducción inglesa (`en-US`). Ocho capturas nuevas por idioma fueron aceptadas y guardadas como borrador. No se pulsó envío a revisión ni lanzamiento de producción. La política pública y Seguridad de datos no se modificaron.

## Handoff
### Goal
Añadir las unidades, probar, hacer push y renovar capturas/ficha.
### Instructions
Mantener Internal testing como destino del pipeline y no publicar producción automáticamente.
### Discoveries
Unidades de presentación pueden cambiar sin migrar los horarios guardados. La ficha tenía solo español latinoamericano; la traducción inglesa se prepara como borrador adicional.
### Accomplished
Selector, conversión validada, regresiones y 48 capturas reales.
### Next Steps
Verificar el nuevo pipeline después del push y completar los bloqueos de privacidad antes de enviar producción.
### Relevant Files
`ReminderIntervalUnit.kt`, `MedicationScreen.kt`, recursos ES/EN, pruebas, script de capturas y ficha documentada.

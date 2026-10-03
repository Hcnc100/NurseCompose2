# Corrección del resumen de una sola toma

## Causa y solución
- MedicationReminderCard mostraba siempre label_every_minutes, incluso cuando endAt == startAt. El editor usa esa igualdad para representar una sola toma y guarda un intervalo interno de respaldo de 60 minutos; ese valor no debe presentarse como repetición.
- La tarjeta ahora reutiliza schedule_single_dose («Una sola toma» / «One dose only») en ese caso. Los recordatorios repetidos conservan su intervalo.
- Sin cambios de base de datos, programación, alarmas ni historial.
- Ambas etiquetas usan el ancho disponible de la columna, permitiendo ajuste de línea. El test inicial encontró didOverflowWidth en las 8 configuraciones; fillMaxWidth eliminó el desbordamiento sin relajar la comprobación.

## Verificación
- Build debug, APK de instrumentación y 45 pruebas unitarias correctos.
- MedicationCardLayoutTest: 32 pruebas instrumentadas pasaron en Codex_Alarm_API_34 (emulator-5556): español/inglés, claro/oscuro, escala de fuente 1×/2×.
- El nuevo test verifica etiqueta de una sola toma visible, ausencia de «Cada 60 minutos» y ausencia de desbordamiento. El test existente de recordatorio repetido verifica que mantiene «Cada 60 minutos».
- El emulador tenía el release de QA 1044; se necesitó instalar el debug local con -r -d. Se instalaron APK de app y test compatibles antes de la ejecución definitiva. No se operó el dispositivo físico.

## Pendiente
- Corrección local todavía no incluida en el AAB 1044 de Internal ni enviada por push.
- La revisión de la ficha existente sigue independiente de este cambio de código.
- Los cambios locales previos del script de capturas y documentos de lanzamiento se preservaron.

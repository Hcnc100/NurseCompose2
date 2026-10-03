# Fallo de CI del selector de unidades — 3 de octubre de 2026

## Causa verificada
La ejecución `37132166791`, commit `6573d24`, falló en API 33 y 34. En ambos casos hubo una sola prueba fallida de las 38: `intervalUnitsPreserveScheduleAndSaveDecimalHours`. La aserción encontraba el campo con valor «480», pero no estaba visible en el viewport del emulador de CI.

El test desplazaba la pantalla hasta el selector, no hasta el campo ubicado debajo. El pase local anterior en una pantalla más alta no cubría esta condición.

## Corrección
Antes de comprobar el valor, volver a Horas o editar el intervalo, la prueba ejecuta `performScrollTo()` sobre el control correspondiente. Se conservan las aserciones de visibilidad, conversión, restauración, guardado y ocultación para toma única. No se omitieron tests ni se cambió el pipeline, el formulario o el destino Internal.

## Entorno de comprobación
Resultado final: **38 pruebas aprobadas, 0 fallos**, ejecutando las mismas dos clases del pipeline en Android 14 y pantalla reducida. APK de pruebas compilado correctamente.
Emulador desechable `Codex_Alarm_API_34`, `emulator-5556`, viewport 1080 × 1920. Instalar juntos el APK actual y su APK de instrumentación: el snapshot del AVD puede restaurar un APK antiguo, lo que provoca `NoSuchFieldError` de recursos y no es el fallo original de CI.

## Handoff
### Goal
Corregir la regresión de CI y permitir nuevamente el bundle de Internal.
### Discoveries
Desplazarse hasta un selector no garantiza visibilidad del campo siguiente en viewports pequeños. Los snapshots pueden restaurar una versión instalada anterior.
### Accomplished
Error identificado en ambos jobs y corrección mínima de la prueba sin debilitar verificaciones.
### Next Steps
Confirmar la nueva ejecución remota en API 33/34 y el upload a Internal; las 38 pruebas locales ya pasaron. No confundir un push con publicación exitosa en Internal.
### Relevant Files
`app/src/androidTest/java/com/nullpointer/nourseCompose/medication/MedicationEditorUiTest.kt` y este informe. Workflow sin cambios.

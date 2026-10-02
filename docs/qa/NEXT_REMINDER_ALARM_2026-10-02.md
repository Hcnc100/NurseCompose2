# Próxima alarma en la lista de recordatorios

## Objetivo
Mostrar la próxima alarma en cada tarjeta, sin estrechar el texto junto al interruptor y al botón de eliminar.

## Cambios
- Bloque de ancho completo bajo los controles: etiqueta y fecha/hora en líneas separadas, altura adaptable, sin truncado ni altura fija.
- Textos en español e inglés y colores semánticos existentes para claro/oscuro.
- Fecha local; estado En pausa / Sin próximas alarmas; actualización del reloj cada segundo mientras la lista está compuesta.
- El scheduler conserva su próxima hora registrada, también para Posponer, y elimina el registro al cancelar. Recordatorios anteriores a este cambio usan la próxima ocurrencia de su pauta como respaldo.
- Padding inferior de la lista para que el botón Agregar no tape la última tarjeta.
- Al programar se admite una primera toma lejana y se cancelan alarmas obsoletas de pautas inactivas o terminadas.

## Verificación y límites
- `testDebugUnitTest assembleDebug` aprobados. Las cuatro nuevas regresiones pasaron: posposición, avance de pauta, comienzo lejano y estados pausa/finalizado (4 pruebas, 0 fallos).
- No se garantiza sonido ni entrega exacta: la etiqueta dice **programada**, porque permisos, canal y sistema afectan la entrega.
- Instalación en API 33 realizada; inspección visual bloqueada por diálogos repetidos de ANR de `Process system`. No afirmar validación visual en las cuatro combinaciones todavía.
- No se hizo push. No se ejecutaron Android 15/16.

## Archivos
- MedicationScreen.kt: bloque visual y actualización temporal.
- MedicationReminderScheduler.kt: tiempos persistidos y cancelación.
- MedicationReminderViewModel.kt: expone los tiempos a la pantalla.
- ReminderNextAlarm.kt / ReminderNextAlarmTest.kt: selección y regresiones.
- values/strings.xml y values-es/strings.xml: etiquetas localizadas.

## Siguiente paso
Validar capturas claro/oscuro, español/inglés y fuentes grandes cuando el emulador responda normalmente.

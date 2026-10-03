# Video demostrativo de alarmas

- Archivo local: `artifacts/alarm-video-2026-10-03/nurseapp-alarm-demo.mp4`.
- Emulador desechable: Codex_Alarm_API_34, emulator-5556. No se operó el emulador del usuario ni su dispositivo físico.
- Datos ficticios: recordatorio «Demo alarm». Interfaz en inglés, tema claro.
- Duración verificada: 122,31 segundos; H.264, 720 × 1560; decodificación completa sin errores.
- Secuencia: sonido/vibración/pantalla completa habilitados → guardar → próxima alarma → aviso real → abrir pantalla de alarma → marcar toma → notificación retirada.
- Durante el aviso, Android confirmó MedicationAlarmService con `isForeground=true`, tipo mediaPlayback. Después de marcar la toma, el servicio ya no aparecía y la notificación había desaparecido.
- Limitación: `adb screenrecord` no captura sonido. El MP4 no tiene pista de audio; no demuestra audiblemente la reproducción. No se añadió sonido artificial.
- Se revisaron fotogramas intermedios y el último fotograma (panel sin notificación de alarma).
- El titular alojó el video en https://youtube.com/shorts/7xIYdMkLLXs?feature=share. Se verificó la página titulada «nurseapp alarm demo» con reproductor disponible.
- Declaración FGS guardada en Play Console: reproducción de contenido multimedia y enlace de evidencia. Console confirmó «Se guardó el cambio. Envíalo a revisión». Pendiente de envío y aprobación; no se publicó ni envió a revisión.
- Comprobante: `artifacts/alarm-video-2026-10-03/play-console-fgs-saved.jpg`.

## Hallazgo adicional
La tarjeta muestra «Every 60 minutes» aunque el formulario estaba seleccionado en «One dose only». Revisar el texto/resumen de la tarjeta por separado; no inferir recurrencia del comportamiento a partir de esa etiqueta.
